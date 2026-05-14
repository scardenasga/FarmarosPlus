import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';

@Component({
  selector: 'app-inventory-actions',
  standalone: true,
  imports: [CommonModule, BottomSheetComponent],
  template: `
    <app-bottom-sheet 
      [isOpen]="isVisible()" 
      (close)="close.emit()"
      title="Acciones de Inventario"
    >
      <div class="actions-header">
        <h2>Gestión de Inventario</h2>
      </div>

      <div class="actions-list">
        <button class="action-item" (click)="navigate('categorias')">
          <div class="icon-circle category">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82zM7 7h.01"/></svg>
          </div>
          <div class="text-container">
            <span class="action-title">Gestionar Categorías</span>
            <span class="action-desc">Crea, edita o elimina categorías de productos</span>
          </div>
        </button>

        <button class="action-item" (click)="navigate('ingreso')">
          <div class="icon-circle stock">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 8V21H3V8M1 3H23V8H1V3M10 12H14"/></svg>
          </div>
          <div class="text-container">
            <span class="action-title">Registrar Ingreso de Stock</span>
            <span class="action-desc">Abastece productos mediante código de barras</span>
          </div>
        </button>

        <button class="action-item" (click)="navigate('crear')">
          <div class="icon-circle product">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          </div>
          <div class="text-container">
            <span class="action-title">Nuevo Producto</span>
            <span class="action-desc">Registra un nuevo producto en el catálogo</span>
          </div>
        </button>
      </div>
    </app-bottom-sheet>
  `,
  styles: [`
    .actions-header {
      margin-bottom: var(--space-l);
    }
    .actions-header h2 {
      font-size: 20px;
      color: var(--on-surface);
      margin: 0;
    }
    .actions-list {
      display: flex;
      flex-direction: column;
      gap: var(--space-s);
    }
    .action-item {
      display: flex;
      align-items: center;
      gap: var(--space-l);
      padding: var(--space-l);
      border-radius: var(--radius-l);
      background-color: var(--surface-container);
      transition: all 0.2s;
      text-align: left;
      width: 100%;
    }
    .action-item:hover {
      background-color: var(--surface-container-high);
      transform: translateX(4px);
    }
    .icon-circle {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-circle.category { background-color: #e3f2fd; color: #1976d2; }
    .icon-circle.stock { background-color: #f1f8e9; color: #388e3c; }
    .icon-circle.product { background-color: #fff3e0; color: #f57c00; }

    .text-container {
      display: flex;
      flex-direction: column;
    }
    .action-title {
      font-size: 16px;
      font-weight: 700;
      color: var(--on-surface);
    }
    .action-desc {
      font-size: 13px;
      color: var(--outline);
    }
  `]
})
export class InventoryActionsComponent {
  isVisible = input<boolean>(false);
  close = output<void>();
  actionSelected = output<string>();

  navigate(path: string) {
    this.actionSelected.emit(path);
    this.close.emit();
  }
}
