import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { PurchasingService } from '../../services/purchasing.service';
import { OrdenCompraResumen } from '../../models/purchasing.model';
import { Supplier } from '../../../supplier/models/supplier.model';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { CompraFormularioPanelComponent } from '../../components/compra-formulario-panel/compra-formulario-panel.component';
import { RecepcionPanelComponent } from '../../components/recepcion-panel/recepcion-panel.component';
import { OrdenDetallePanelComponent } from '../../components/orden-detalle-panel/orden-detalle-panel.component';

@Component({
  selector: 'app-purchase-history',
  standalone: true,
  imports: [CommonModule, ConfirmationDialogComponent, SearchBarComponent, CompraFormularioPanelComponent, RecepcionPanelComponent, OrdenDetallePanelComponent],
  templateUrl: './purchase-history.component.html',
  styleUrl: './purchase-history.component.css'
})
export class PurchaseHistoryComponent implements OnInit {
  private service = inject(PurchasingService);
  private suppliers = inject(SupplierService);
  private router = inject(Router);
  private notificacion = inject(NotificacionService);

  compras = signal<OrdenCompraResumen[]>([]);
  proveedores = signal<Supplier[]>([]);
  cargando = signal(true);
  error = signal('');
  termino = signal('');
  filtroProveedor = signal<number | null>(null);
  fechaDesde = signal('');
  fechaHasta = signal('');
  filtroEstado = signal('TODOS');
  filtroEstadoPago = signal('TODOS');
  aCancelar = signal<OrdenCompraResumen | null>(null);
  cancelando = signal(false);

  estados = ['TODOS', 'PENDIENTE', 'CERRADA', 'NO_RECIBIDA', 'CANCELADA'];
  estadosPago = ['TODOS', 'PAGADA', 'PENDIENTE_PAGO', 'SIN_RECEPCION'];

  filtradas = computed(() =>
    this.compras().filter(c => {
      const q = this.termino().trim().toLowerCase();
      const f = c.fechaPedido.slice(0, 10);
      return (!q || c.proveedorNombre.toLowerCase().includes(q) || ('oc-' + c.idOrden).includes(q)) &&
        (!this.filtroProveedor() || c.proveedorId === this.filtroProveedor()) &&
        (!this.fechaDesde() || f >= this.fechaDesde()) &&
        (!this.fechaHasta() || f <= this.fechaHasta()) &&
        (this.filtroEstado() === 'TODOS' || c.estado === this.filtroEstado()) &&
        (this.filtroEstadoPago() === 'TODOS' || c.estadoPago === this.filtroEstadoPago());
    })
  );

  filtrosActivos = computed(() =>
    Number(!!this.filtroProveedor()) +
    Number(this.filtroEstado() !== 'TODOS') +
    Number(this.filtroEstadoPago() !== 'TODOS') +
    Number(!!this.fechaDesde() || !!this.fechaHasta())
  );

  /* ---------- Paginacion ---------- */
  readonly TAMANOS_PAGINA = [5, 10, 25, 50];
  tamanoPagina = signal(10);
  pagina = signal(1);

  totalPaginas = computed(() => Math.max(1, Math.ceil(this.filtradas().length / this.tamanoPagina())));

  comprasPaginadas = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamanoPagina();
    return this.filtradas().slice(inicio, inicio + this.tamanoPagina());
  });

  rangoMostrado = computed(() => {
    const total = this.filtradas().length;
    if (!total) return '0 de 0';
    return `${(this.pagina() - 1) * this.tamanoPagina() + 1}-${Math.min(this.pagina() * this.tamanoPagina(), total)} de ${total}`;
  });

  paginasVisibles = computed(() => {
    const total = this.totalPaginas();
    const actual = this.pagina();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const paginas: (number | '...')[] = [1];
    const desde = Math.max(2, actual - 1);
    const hasta = Math.min(total - 1, actual + 1);
    if (desde > 2) paginas.push('...');
    for (let i = desde; i <= hasta; i++) paginas.push(i);
    if (hasta < total - 1) paginas.push('...');
    paginas.push(total);
    return paginas;
  });

  cambiarPagina(nueva: number): void {
    this.pagina.set(Math.min(Math.max(1, nueva), this.totalPaginas()));
  }

  cambiarTamanoPagina(valor: unknown): void {
    this.tamanoPagina.set(Number(valor));
    this.pagina.set(1);
  }

  panelCompra = signal<{ modo: 'crear' | 'editar'; ordenId?: number } | null>(null);
  panelRecepcion = signal<OrdenCompraResumen | null>(null);
  detalleOrdenId = signal<number | null>(null);
  detalleAbierto = signal<boolean>(false);

  abrirCrear(): void { this.panelCompra.set({ modo: 'crear' }); }

  abrirEditar(c: OrdenCompraResumen): void { this.panelCompra.set({ modo: 'editar', ordenId: c.idOrden }); }

  abrirRecepcion(c: OrdenCompraResumen): void { this.panelRecepcion.set(c); }

  alGuardarPanel(): void {
    this.panelCompra.set(null);
    this.panelRecepcion.set(null);
    this.cargar();
  }

  ngOnInit(): void {
    this.cargar();
    this.suppliers.listActive().subscribe({ next: p => this.proveedores.set(p) });
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({
      ordenes: this.service.listarOrdenes(),
      recepciones: this.service.listarRecepciones()
    }).subscribe({
      next: ({ ordenes, recepciones }) => {
        const pagos = new Map<number, string[]>();
        recepciones.forEach(r => {
          const id = r.orden?.idOrden;
          if (id) pagos.set(id, [...(pagos.get(id) ?? []), r.estadoPago]);
        });
        const enriquecidas = ordenes.map(o => ({
          ...o,
          estadoPago: this.estadoPago(pagos.get(o.idOrden) ?? [])
        }));
        this.compras.set(enriquecidas.sort((a, b) => b.fechaPedido.localeCompare(a.fechaPedido)));
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial de ordenes.');
        this.notificacion.error(this.error());
        this.cargando.set(false);
      }
    });
  }

  onBuscar(term: string): void {
    this.termino.set(term);
    this.pagina.set(1);
  }

  limpiar(): void {
    this.termino.set('');
    this.filtroProveedor.set(null);
    this.fechaDesde.set('');
    this.fechaHasta.set('');
    this.filtroEstado.set('TODOS');
    this.filtroEstadoPago.set('TODOS');
    this.pagina.set(1);
  }

  volver(): void { this.router.navigate(['/compras-gestion']); }

  ver(c: OrdenCompraResumen): void {
    this.detalleOrdenId.set(c.idOrden);
    this.detalleAbierto.set(true);
  }

  solicitarCancelar(c: OrdenCompraResumen): void {
    this.aCancelar.set(c);
  }

  cancelar(): void {
    const c = this.aCancelar();
    if (!c || this.cancelando()) return;
    this.cancelando.set(true);
    this.service.cancelarOrden(c.idOrden).subscribe({
      next: () => {
        this.compras.update(xs => xs.map(x => x.idOrden === c.idOrden ? { ...x, estado: 'CANCELADA' } : x));
        this.notificacion.exito(`Orden OC-${c.idOrden} cancelada`);
        this.aCancelar.set(null);
        this.cancelando.set(false);
      },
      error: e => {
        const msg = e?.error?.message ?? 'No se pudo cancelar la orden.';
        this.error.set(msg);
        this.notificacion.error(msg);
        this.aCancelar.set(null);
        this.cancelando.set(false);
      }
    });
  }

  textoEstado(e: string): string {
    return ({ PENDIENTE: 'Pendiente', CERRADA: 'Cerrada', NO_RECIBIDA: 'No recibida', CANCELADA: 'Cancelada' } as any)[e] ?? e;
  }

  estadoPago(estados: string[]): 'PAGADA' | 'PENDIENTE_PAGO' | 'SIN_RECEPCION' {
    if (!estados.length) return 'SIN_RECEPCION';
    return estados.every(e => e === 'PAGADO') ? 'PAGADA' : 'PENDIENTE_PAGO';
  }

  textoEstadoPago(estado?: string): string {
    return ({ PAGADA: 'Pagada', PENDIENTE_PAGO: 'Pendiente de pago', SIN_RECEPCION: 'Sin recepcion' } as any)[estado ?? 'SIN_RECEPCION'] ?? 'Sin recepcion';
  }
}