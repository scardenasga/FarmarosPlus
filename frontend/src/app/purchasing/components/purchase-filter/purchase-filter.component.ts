import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';
import { FilterChipComponent } from '../../../shared/components/filter-chip/filter-chip.component';
import { Supplier } from '../../../supplier/models/supplier.model';

export interface PurchaseFilterOptions {
  proveedorId: number | null;
  estado: string;
  estadoPago: string;
  desde: string;
  hasta: string;
}

@Component({
  selector: 'app-purchase-filter',
  standalone: true,
  imports: [CommonModule, FormsModule, BottomSheetComponent, FilterChipComponent],
  template: `
    <app-bottom-sheet [isOpen]="isVisible()" title="Filtrar órdenes" (close)="close.emit()">
      <div class="filter-header"><div><p class="eyebrow">Refinar resultados</p><h2>Filtrar órdenes</h2></div><button type="button" class="close-button" (click)="close.emit()">Cerrar</button></div>

      <section class="filter-section">
        <p class="section-title">Proveedor</p>
        <div class="chips-container">
          <app-filter-chip label="Todos" [selected]="selectedProveedor() === null" (toggle)="selectedProveedor.set(null)"></app-filter-chip>
          @for (proveedor of proveedores(); track proveedor.idProveedor) {
            <app-filter-chip [label]="proveedor.nombre" [selected]="selectedProveedor() === proveedor.idProveedor" (toggle)="seleccionarProveedor(proveedor.idProveedor)"></app-filter-chip>
          }
        </div>
      </section>

      <section class="filter-section">
        <p class="section-title">Estado de la orden</p>
        <div class="chips-container">
          @for (estado of estados; track estado) {
            <app-filter-chip [label]="nombreEstado(estado)" [selected]="selectedEstado() === estado" (toggle)="selectedEstado.set(estado)"></app-filter-chip>
          }
        </div>
      </section>

      <section class="filter-section">
        <p class="section-title">Estado del pago</p>
        <div class="chips-container">
          @for (estado of estadosPago; track estado) {
            <app-filter-chip [label]="nombreEstadoPago(estado)" [selected]="selectedEstadoPago() === estado" (toggle)="selectedEstadoPago.set(estado)"></app-filter-chip>
          }
        </div>
      </section>

      <section class="filter-section date-section">
        <p class="section-title">Fecha del pedido</p>
        <div class="date-grid">
          <label>Desde<input type="date" [ngModel]="desde()" (ngModelChange)="desde.set($event)"></label>
          <label>Hasta<input type="date" [ngModel]="hasta()" (ngModelChange)="hasta.set($event)"></label>
        </div>
      </section>

      <div class="actions"><button type="button" class="btn-clear" (click)="limpiar()">Limpiar</button><button type="button" class="btn-apply" (click)="aplicar()">Aplicar filtros</button></div>
    </app-bottom-sheet>
  `,
  styles: [`
    .filter-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:var(--space-l)}
    .eyebrow{margin:0 0 4px;color:var(--primary);font-size:11px;font-weight:800;letter-spacing:1px;text-transform:uppercase}.filter-header h2{margin:0;color:var(--on-surface);font-size:21px}.close-button{border:0;background:transparent;color:var(--primary);font-weight:700;padding:6px 0}.filter-section{margin-bottom:var(--space-l)}.section-title{margin:0 0 var(--space-s);font-size:14px;font-weight:700;color:var(--on-surface-variant)}.chips-container{display:flex;flex-wrap:wrap;gap:8px}.date-grid{display:grid;grid-template-columns:1fr 1fr;gap:10px}.date-grid label{display:flex;flex-direction:column;gap:5px;color:var(--on-surface-variant);font-size:12px;font-weight:700}.date-grid input{width:100%;box-sizing:border-box;background:var(--surface-container-high);border:1px solid var(--outline);border-radius:var(--radius-m);padding:11px;color:var(--on-surface)}.actions{display:flex;gap:10px;margin-top:var(--space-xl)}.actions button{flex:1;height:48px;border-radius:var(--radius-full);font-weight:700}.btn-clear{border:1px solid var(--primary);background:transparent;color:var(--primary)}.btn-apply{border:0;background:var(--primary);color:var(--on-primary);box-shadow:var(--shadow-1)}
    @media(max-width:420px){.date-grid{grid-template-columns:1fr}.filter-header h2{font-size:19px}}
  `]
})
export class PurchaseFilterComponent {
  isVisible = input(false); proveedores = input<Supplier[]>([]); close = output<void>(); apply = output<PurchaseFilterOptions>();
  selectedProveedor = signal<number | null>(null); selectedEstado = signal('TODOS'); selectedEstadoPago = signal('TODOS'); desde = signal(''); hasta = signal('');
  estados = ['TODOS', 'PENDIENTE', 'CERRADA', 'NO_RECIBIDA', 'CANCELADA'];
  estadosPago = ['TODOS', 'PAGADA', 'PENDIENTE_PAGO', 'SIN_RECEPCION'];
  seleccionarProveedor(id: number): void { this.selectedProveedor.set(this.selectedProveedor() === id ? null : id); }
  nombreEstado(estado: string): string { return ({ TODOS: 'Todos', PENDIENTE: 'Pendiente', CERRADA: 'Cerrada', NO_RECIBIDA: 'No recibida', CANCELADA: 'Cancelada' } as Record<string, string>)[estado]; }
  nombreEstadoPago(estado: string): string { return ({ TODOS: 'Todos', PAGADA: 'Pagada', PENDIENTE_PAGO: 'Pendiente de pago', SIN_RECEPCION: 'Sin recepción' } as Record<string, string>)[estado]; }
  limpiar(): void { this.selectedProveedor.set(null); this.selectedEstado.set('TODOS'); this.selectedEstadoPago.set('TODOS'); this.desde.set(''); this.hasta.set(''); }
  aplicar(): void { this.apply.emit({ proveedorId: this.selectedProveedor(), estado: this.selectedEstado(), estadoPago: this.selectedEstadoPago(), desde: this.desde(), hasta: this.hasta() }); this.close.emit(); }
}
