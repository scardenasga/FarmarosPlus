import { Component, OnInit, inject, signal, computed, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { PurchasingService } from '../../services/purchasing.service';
import { DevolucionClienteResponse, DevolucionResponse } from '../../models/purchasing.model';
import { SesionService } from '../../../shared/services/sesion.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { ReturnDetailPanelComponent } from '../../components/return-detail-panel/return-detail-panel.component';
import { ReturnFormPanelComponent } from '../../components/return-form-panel/return-form-panel.component';

type TipoDevolucion = 'proveedor' | 'cliente';

@Component({
  selector: 'app-return-history',
  standalone: true,
  imports: [CommonModule, RouterModule, SearchBarComponent, ConfirmationDialogComponent, ReturnDetailPanelComponent, ReturnFormPanelComponent],
  templateUrl: './return-history.component.html',
  styleUrl: './return-history.component.css'
})
export class ReturnHistoryComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private router = inject(Router);
  private sesion = inject(SesionService);
  private notificacion = inject(NotificacionService);
  @ViewChild(ReturnDetailPanelComponent) detailPanel?: ReturnDetailPanelComponent;

  readonly estadosDevolucion: string[] = ['PENDIENTE', 'ENVIADA', 'ACEPTADA', 'RECHAZADA', 'CERRADA'];
  isAdmin = computed(() => this.sesion.esAdminORegente());

  estadosVisiblesPara(dev: DevolucionResponse | DevolucionClienteResponse): string[] {
    if (this.isAdmin()) return this.estadosDevolucion;
    const actual = (dev as any).estado ?? 'PENDIENTE';
    if (actual === 'PENDIENTE') return ['PENDIENTE', 'ENVIADA', 'CERRADA'];
    if (actual === 'ENVIADA') return ['ENVIADA', 'PENDIENTE'];
    return [actual];
  }

  tipoDevolucion = signal<TipoDevolucion>('proveedor');
  devoluciones = signal<(DevolucionResponse | DevolucionClienteResponse)[]>([]);
  cargando = signal<boolean>(true);
  error = signal<string>('');

  devolucionAEliminar = signal<DevolucionResponse | DevolucionClienteResponse | null>(null);
  eliminando = signal<boolean>(false);

  searchTerm = signal<string>('');
  vista = signal<'grid' | 'tabla'>((localStorage.getItem('devoluciones.vista') as 'grid' | 'tabla') || 'grid');
  pagina = signal<number>(1);
  tamanoPagina = signal<number>(10);
  readonly TAMANOS_PAGINA = [5, 10, 25, 50];

  esProveedor = computed(() => this.tipoDevolucion() === 'proveedor');
  esCliente = computed(() => this.tipoDevolucion() === 'cliente');

  devolucionesFiltradas = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    if (!term) return this.devoluciones();
    return this.devoluciones().filter(d => {
      const principal = this.nombrePrincipal(d).toLowerCase();
      const secundario = (this.detalleSecundario(d) ?? '').toLowerCase();
      const motivo = (d.motivo ?? '').toLowerCase();
      const obs = ('observaciones' in d ? (d as any).observaciones ?? '' : '').toString().toLowerCase();
      const estado = ('estado' in d ? (d as any).estado ?? '' : '').toString().toLowerCase();
      return principal.includes(term) || secundario.includes(term) || motivo.includes(term) || obs.includes(term) || estado.includes(term);
    });
  });
  totalPaginas = computed(() => Math.max(1, Math.ceil(this.devolucionesFiltradas().length / this.tamanoPagina())));
  devolucionesPaginadas = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamanoPagina();
    return this.devolucionesFiltradas().slice(inicio, inicio + this.tamanoPagina());
  });
  rangoMostrado = computed(() => {
    const total = this.devolucionesFiltradas().length;
    if (!total) return '0 de 0';
    const ini = (this.pagina() - 1) * this.tamanoPagina() + 1;
    const fin = Math.min(this.pagina() * this.tamanoPagina(), total);
    return `${ini}–${fin} de ${total}`;
  });
  paginasVisibles = computed(() => {
    const total = this.totalPaginas(); const actual = this.pagina();
    if (total <= 7) return Array.from({length: total}, (_,i)=>i+1);
    const pag:(number|'...')[]=[1]; const desde=Math.max(2, actual-1); const hasta=Math.min(total-1, actual+1);
    if(desde>2) pag.push('...'); for(let i=desde;i<=hasta;i++) pag.push(i); if(hasta<total-1) pag.push('...'); pag.push(total); return pag;
  });

  ngOnInit(): void {
    this.cargar();
  }

  onTipoChange(tipo: TipoDevolucion): void {
    if (this.tipoDevolucion() === tipo) return;
    this.tipoDevolucion.set(tipo);
    this.pagina.set(1);
    this.searchTerm.set('');
    this.cargar();
  }
  onBuscar(term: string): void { this.searchTerm.set(term); this.pagina.set(1); }
  cambiarVista(v: 'grid' | 'tabla'): void { this.vista.set(v); localStorage.setItem('devoluciones.vista', v); }
  cambiarPagina(n: number): void { const d = Math.min(Math.max(1,n), this.totalPaginas()); if(d!==this.pagina()) this.pagina.set(d); }
  cambiarTamanoPagina(t: string|number): void { this.tamanoPagina.set(Number(t)); this.pagina.set(1); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    if (this.esProveedor()) {
      this.purchasingService.listarDevoluciones().subscribe({
        next: (data: DevolucionResponse[]) => {
          this.devoluciones.set(data);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el historial.');
          this.cargando.set(false);
        }
      });
      return;
    }

    this.purchasingService.listarDevolucionesClientes().subscribe({
      next: (data: DevolucionClienteResponse[]) => {
        this.devoluciones.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial.');
        this.cargando.set(false);
      }
    });
  }

  nueva(): void { this.abrirCrearPanel(); }

  verDetalle(dev: DevolucionResponse | DevolucionClienteResponse): void { this.abrirDetallePanel(dev); }

  editar(dev: DevolucionResponse | DevolucionClienteResponse): void {
    this.formModo.set('editar');
    this.formId.set(dev.id);
    this.formTipo.set(this.tipoDevolucion());
    this.formAbierto.set(true);
  }

  solicitarEliminar(dev: DevolucionResponse | DevolucionClienteResponse): void {
    this.devolucionAEliminar.set(dev);
  }

  cancelarEliminar(): void {
    if (this.eliminando()) return;
    this.devolucionAEliminar.set(null);
  }

  confirmarEliminar(): void {
    const dev = this.devolucionAEliminar();
    if (!dev || this.eliminando()) return;

    this.eliminando.set(true);
    this.error.set('');

    const obs = this.esProveedor()
      ? this.purchasingService.eliminarDevolucion(dev.id, this.sesion.username())
      : this.purchasingService.eliminarDevolucionCliente(dev.id, this.sesion.username());

    obs.subscribe({
      next: () => {
        this.devoluciones.update(prev => prev.filter(d => d.id !== dev.id));
        this.devolucionAEliminar.set(null);
        this.eliminando.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'No se pudo eliminar la devolución.');
        this.devolucionAEliminar.set(null);
        this.eliminando.set(false);
      }
    });
  }

  // Panels
  detalleAbierto = signal<boolean>(false);
  detalleId = signal<number | null>(null);
  detalleTipo = signal<TipoDevolucion>('proveedor');
  formAbierto = signal<boolean>(false);
  formModo = signal<'crear' | 'editar'>('crear');
  formId = signal<number | null>(null);
  formTipo = signal<TipoDevolucion>('proveedor');

  abrirDetallePanel(dev: DevolucionResponse | DevolucionClienteResponse): void {
    this.detalleId.set(dev.id);
    this.detalleTipo.set(this.tipoDevolucion());
    this.detalleAbierto.set(true);
    setTimeout(() => (this as any).detailPanel?.cargar(), 0);
  }

  cerrarDetalle(): void { this.detalleAbierto.set(false); this.detalleId.set(null); }
  alEliminarDesdePanel(): void { this.cargar(); this.cerrarDetalle(); }
  editarDesdePanel(e: {id:number, tipo:TipoDevolucion}): void {
    this.detalleAbierto.set(false);
    this.formModo.set('editar');
    this.formId.set(e.id);
    this.formTipo.set(e.tipo);
    this.formAbierto.set(true);
  }

  abrirCrearPanel(): void {
    this.formModo.set('crear');
    this.formId.set(null);
    this.formTipo.set(this.tipoDevolucion());
    this.formAbierto.set(true);
  }
  cerrarForm(): void { this.formAbierto.set(false); this.formId.set(null); }
  alGuardarForm(): void { this.formAbierto.set(false); this.cargar(); }

  cambiarEstadoRapido(dev: DevolucionResponse | DevolucionClienteResponse, nuevoEstado: string): void {
    if (!this.esProveedor()) {
      this.notificacion.advertencia('Solo las devoluciones a proveedor tienen control de estado.');
      return;
    }
    const estadoActual = (dev as any).estado;
    if (estadoActual === nuevoEstado) return;
    this.purchasingService.cambiarEstadoDevolucion(dev.id, nuevoEstado, this.sesion.username()).subscribe({
      next: (actualizada) => {
        this.devoluciones.update(prev => prev.map(d => d.id === dev.id ? { ...d, ...actualizada } : d));
        this.notificacion.exito(`Estado cambiado a ${nuevoEstado}`);
      },
      error: (err) => this.notificacion.error(err?.error?.message ?? 'No se pudo cambiar el estado.')
    });
  }

  volver(): void {
    this.router.navigate(['/compras-gestion']);
  }

  totalProductos(dev: DevolucionResponse | DevolucionClienteResponse): number {
    return dev.detalles.reduce((sum, d) => sum + d.cantidad, 0);
  }

  nombrePrincipal(dev: DevolucionResponse | DevolucionClienteResponse): string {
    return 'nombreProveedor' in dev
      ? dev.nombreProveedor
      : (dev.nombreCliente || 'Cliente sin identificar');
  }

  detalleSecundario(dev: DevolucionResponse | DevolucionClienteResponse): string | null {
    if ('idVenta' in dev) {
      return `Venta #${dev.idVenta} · ${dev.documentoCliente || 'Sin documento'}`;
    }
    return null;
  }
}
