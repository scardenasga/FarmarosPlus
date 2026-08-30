import { Component, inject, input, output, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PurchasingService } from '../../services/purchasing.service';
import { SesionService } from '../../../shared/services/sesion.service';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { DevolucionResponse, DevolucionClienteResponse } from '../../models/purchasing.model';

type TipoDevolucion = 'proveedor' | 'cliente';

@Component({
  selector: 'app-return-detail-panel',
  standalone: true,
  imports: [CommonModule, ConfirmationDialogComponent],
  templateUrl: './return-detail-panel.component.html',
  styleUrl: './return-detail-panel.component.css'
})
export class ReturnDetailPanelComponent {
  private purchasingService = inject(PurchasingService);
  private sesion = inject(SesionService);
  private notificacion = inject(NotificacionService);

  constructor() {
    effect(() => {
      if (this.abierto() && this.devolucionId() != null) {
        this.cargar();
      } else if (!this.abierto()) {
        this.devolucion.set(null);
        this.error.set('');
      }
    });
  }

  devolucionId = input<number | null>(null);
  tipo = input<TipoDevolucion>('proveedor');
  abierto = input<boolean>(false);

  cerrado = output<void>();
  editado = output<{id:number, tipo:TipoDevolucion}>();
  eliminado = output<void>();

  devolucion = signal<DevolucionResponse | DevolucionClienteResponse | null>(null);
  cargando = signal<boolean>(false);
  error = signal<string>('');
  showDelete = signal<boolean>(false);
  eliminando = signal<boolean>(false);
  estadoEdit = signal<string>('PENDIENTE');
  cambiandoEstado = signal<boolean>(false);
  readonly estadosDevolucion: string[] = ['PENDIENTE', 'ENVIADA', 'ACEPTADA', 'RECHAZADA', 'CERRADA'];
  isAdmin = computed(() => this.sesion.esAdminORegente());

  estadosVisibles(): string[] {
    if (this.isAdmin()) return this.estadosDevolucion;
    const actual = (this.devolucion() as any)?.estado ?? 'PENDIENTE';
    if (actual === 'PENDIENTE') return ['PENDIENTE', 'ENVIADA', 'CERRADA'];
    if (actual === 'ENVIADA') return ['ENVIADA', 'PENDIENTE'];
    return [actual];
  }

  esProveedor = computed(() => this.tipo() === 'proveedor');

  patronBarras(codigo: string): number[] {
    let semilla = 0;
    for (let i=0;i<codigo.length;i++) semilla = (semilla*31 + codigo.charCodeAt(i)) % 100000;
    const barras:number[]=[];
    for(let i=0;i<18;i++){ semilla = (semilla*1103515245 + 12345) % 2147483648; barras.push(1 + ((semilla>>8)%3));}
    return barras;
  }

  cargar(): void {
    const id = this.devolucionId();
    if (id==null) return;
    this.cargando.set(true);
    this.error.set('');
    if (this.esProveedor()) {
      this.purchasingService.obtenerDevolucion(id).subscribe({
        next: (d: any) => { this.devolucion.set(d as any); this.estadoEdit.set(d.estado ?? 'PENDIENTE'); this.cargando.set(false); },
        error: () => { this.error.set('No se pudo cargar la devolución.'); this.cargando.set(false); }
      });
    } else {
      this.purchasingService.obtenerDevolucionCliente(id).subscribe({
        next: (d: any) => { this.devolucion.set(d as any); this.cargando.set(false); },
        error: () => { this.error.set('No se pudo cargar la devolución.'); this.cargando.set(false); }
      });
    }
  }

  cerrar(): void { this.cerrado.emit(); }

  solicitarEditar(): void {
    const id = this.devolucionId();
    if (id!=null) this.editado.emit({id, tipo: this.tipo()});
  }

  cambiarEstadoRapido(nuevo: string): void {
    const id = this.devolucionId();
    if (id==null || !this.esProveedor()) return;
    const actual = (this.devolucion() as any)?.estado;
    if (actual === nuevo) return;
    this.cambiandoEstado.set(true);
    this.purchasingService.cambiarEstadoDevolucion(id, nuevo, this.sesion.username()).subscribe({
      next: (actualizada: any) => {
        this.devolucion.set(actualizada as any);
        this.estadoEdit.set(actualizada.estado);
        this.cambiandoEstado.set(false);
        this.notificacion.exito(`Estado cambiado a ${nuevo}`);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'No se pudo cambiar el estado.');
        this.notificacion.error(err?.error?.message ?? 'No se pudo cambiar el estado.');
        this.cambiandoEstado.set(false);
      }
    });
  }

  solicitarEliminar(): void { this.showDelete.set(true); }
  cancelarEliminar(): void { this.showDelete.set(false); }

  confirmarEliminar(): void {
    const id = this.devolucionId();
    if (id==null || this.eliminando()) return;
    this.eliminando.set(true);
    const obs = this.esProveedor()
      ? this.purchasingService.eliminarDevolucion(id, this.sesion.username())
      : this.purchasingService.eliminarDevolucionCliente(id, this.sesion.username());
    obs.subscribe({
      next: () => { this.eliminando.set(false); this.showDelete.set(false); this.eliminado.emit(); this.cerrar(); },
      error: (err) => { this.error.set(err?.error?.message ?? 'No se pudo eliminar.'); this.eliminando.set(false); this.showDelete.set(false); }
    });
  }

  nombrePrincipal(): string {
    const dev = this.devolucion();
    if (!dev) return '';
    return 'nombreProveedor' in dev ? dev.nombreProveedor : ((dev as DevolucionClienteResponse).nombreCliente || 'Cliente sin identificar');
  }

  totalProductos(): number {
    const dev = this.devolucion();
    return dev ? dev.detalles.reduce((s,d)=>s+d.cantidad,0) : 0;
  }
}
