import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { PurchasingService } from '../../services/purchasing.service';
import { OrdenCompraResumen } from '../../models/purchasing.model';
import { Supplier } from '../../../supplier/models/supplier.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { FilterButtonComponent } from '../../../shared/components/filter-button/filter-button.component';
import { PurchaseFilterComponent, PurchaseFilterOptions } from '../../components/purchase-filter/purchase-filter.component';

@Component({ selector: 'app-purchase-history', standalone: true, imports: [CommonModule, FormsModule, TopBarComponent, FabButtonComponent, ConfirmationDialogComponent, SearchBarComponent, FilterButtonComponent, PurchaseFilterComponent], templateUrl: './purchase-history.component.html', styleUrl: './purchase-history.component.css' })
export class PurchaseHistoryComponent implements OnInit {
  private service = inject(PurchasingService); private suppliers = inject(SupplierService); private router = inject(Router);
  compras = signal<OrdenCompraResumen[]>([]); proveedores = signal<Supplier[]>([]); cargando = signal(true); error = signal('');
  termino = signal(''); filtroProveedor = signal<number | null>(null); fechaDesde = signal(''); fechaHasta = signal(''); filtroEstado = signal('TODOS'); filtroEstadoPago = signal('TODOS'); aCancelar = signal<OrdenCompraResumen | null>(null); cancelando = signal(false);
  filtroVisible = signal(false);
  estados = ['TODOS', 'PENDIENTE', 'CERRADA', 'NO_RECIBIDA', 'CANCELADA'];
  filtradas = computed(() => this.compras().filter(c => { const q = this.termino().trim().toLowerCase(); const f = c.fechaPedido.slice(0, 10); return (!q || c.proveedorNombre.toLowerCase().includes(q) || `oc-${c.idOrden}`.includes(q)) && (!this.filtroProveedor() || c.proveedorId === this.filtroProveedor()) && (!this.fechaDesde() || f >= this.fechaDesde()) && (!this.fechaHasta() || f <= this.fechaHasta()) && (this.filtroEstado() === 'TODOS' || c.estado === this.filtroEstado()) && (this.filtroEstadoPago() === 'TODOS' || c.estadoPago === this.filtroEstadoPago()); }));
  filtrosActivos = computed(() => Number(!!this.filtroProveedor()) + Number(this.filtroEstado() !== 'TODOS') + Number(this.filtroEstadoPago() !== 'TODOS') + Number(!!this.fechaDesde() || !!this.fechaHasta()));
  ngOnInit(): void { this.cargar(); this.suppliers.listActive().subscribe({ next: p => this.proveedores.set(p) }); }
  cargar(): void { this.cargando.set(true); forkJoin({ ordenes: this.service.listarOrdenes(), recepciones: this.service.listarRecepciones() }).subscribe({ next: ({ ordenes, recepciones }) => { const pagos = new Map<number, string[]>(); recepciones.forEach(r => { const id = r.orden?.idOrden; if (id) pagos.set(id, [...(pagos.get(id) ?? []), r.estadoPago]); }); const enriquecidas = ordenes.map(o => ({ ...o, estadoPago: this.estadoPago(pagos.get(o.idOrden) ?? []) })); this.compras.set(enriquecidas.sort((a,b) => b.fechaPedido.localeCompare(a.fechaPedido))); this.cargando.set(false); }, error: () => { this.error.set('No se pudo cargar el historial de órdenes.'); this.cargando.set(false); } }); }
  limpiar(): void { this.termino.set(''); this.filtroProveedor.set(null); this.fechaDesde.set(''); this.fechaHasta.set(''); this.filtroEstado.set('TODOS'); this.filtroEstadoPago.set('TODOS'); }
  aplicarFiltros(options: PurchaseFilterOptions): void { this.filtroProveedor.set(options.proveedorId); this.filtroEstado.set(options.estado); this.filtroEstadoPago.set(options.estadoPago); this.fechaDesde.set(options.desde); this.fechaHasta.set(options.hasta); }
  nueva(): void { this.router.navigate(['/purchasing/register-purchase']); }
  volver(): void { this.router.navigate(['/compras-gestion']); }
  ver(c: OrdenCompraResumen): void { this.router.navigate(['/purchasing/purchase-detail', c.idOrden], { queryParams: { tipo: 'orden' } }); }
  editar(c: OrdenCompraResumen): void { this.router.navigate(['/purchasing/purchase-edit', c.idOrden]); }
  solicitarCancelar(c: OrdenCompraResumen): void { this.aCancelar.set(c); }
  cancelar(): void { const c = this.aCancelar(); if (!c || this.cancelando()) return; this.cancelando.set(true); this.service.cancelarOrden(c.idOrden).subscribe({ next: () => { this.compras.update(xs => xs.map(x => x.idOrden === c.idOrden ? { ...x, estado: 'CANCELADA' } : x)); this.aCancelar.set(null); this.cancelando.set(false); }, error: e => { this.error.set(e?.error?.message ?? 'No se pudo cancelar la orden.'); this.aCancelar.set(null); this.cancelando.set(false); } }); }
  textoEstado(e: string): string { return ({ PENDIENTE: 'Pendiente', CERRADA: 'Cerrada', NO_RECIBIDA: 'No recibida', CANCELADA: 'Cancelada' } as any)[e] ?? e; }
  estadoPago(estados: string[]): 'PAGADA' | 'PENDIENTE_PAGO' | 'SIN_RECEPCION' { if (!estados.length) return 'SIN_RECEPCION'; return estados.every(e => e === 'PAGADO') ? 'PAGADA' : 'PENDIENTE_PAGO'; }
  textoEstadoPago(estado?: string): string { return ({ PAGADA: 'Pagada', PENDIENTE_PAGO: 'Pendiente de pago', SIN_RECEPCION: 'Sin recepción' } as any)[estado ?? 'SIN_RECEPCION'] ?? 'Sin recepción'; }
}
