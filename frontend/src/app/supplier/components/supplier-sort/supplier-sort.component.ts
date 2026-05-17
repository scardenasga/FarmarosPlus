import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';

export type SupplierSortField = 'nombre' | 'nit';
export type SupplierSortOrder = 'asc' | 'desc';

export interface SupplierSortOption {
  field: SupplierSortField;
  order: SupplierSortOrder;
  label: string;
}

@Component({
  selector: 'app-supplier-sort',
  standalone: true,
  imports: [CommonModule, BottomSheetComponent],
  template: `
    <app-bottom-sheet 
      [isOpen]="isVisible()" 
      (close)="close.emit()"
      title="Ordenar por"
    >
      <div class="sort-header">
        <h2>Ordenar por</h2>
      </div>

      <div class="sort-options">
        @for (option of options; track option.field + option.order) {
          <button 
            class="sort-item" 
            [class.active]="currentField() === option.field && currentOrder() === option.order"
            (click)="selectOption(option)"
          >
            <span class="label">{{ option.label }}</span>
            @if (currentField() === option.field && currentOrder() === option.order) {
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 16.17L4.83 12L3.41 13.41L9 19L21 7L19.59 5.59L9 16.17Z" fill="var(--primary)"/>
              </svg>
            }
          </button>
        }
      </div>
    </app-bottom-sheet>
  `,
  styles: [`
    .sort-header {
      margin-bottom: var(--space-l);
    }
    .sort-header h2 {
      font-size: 20px;
      color: var(--on-surface);
      margin: 0;
    }
    .sort-options {
      display: flex;
      flex-direction: column;
      gap: var(--space-xs);
    }
    .sort-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: var(--space-l);
      border-radius: var(--radius-m);
      background: none;
      transition: background-color 0.2s;
      width: 100%;
      text-align: left;
    }
    .sort-item:hover {
      background-color: var(--surface-container-high);
    }
    .sort-item.active {
      background-color: var(--primary-container);
    }
    .sort-item.active .label {
      color: var(--on-primary-container);
      font-weight: 700;
    }
    .label {
      font-size: 16px;
      color: var(--on-surface);
    }
  `]
})
export class SupplierSortComponent {
  isVisible = input<boolean>(false);
  currentField = input<SupplierSortField>('nombre');
  currentOrder = input<SupplierSortOrder>('asc');
  
  close = output<void>();
  sort = output<SupplierSortOption>();

  options: SupplierSortOption[] = [
    { field: 'nombre', order: 'asc', label: 'Nombre (A-Z)' },
    { field: 'nombre', order: 'desc', label: 'Nombre (Z-A)' },
    { field: 'nit', order: 'asc', label: 'NIT (Menor a Mayor)' },
    { field: 'nit', order: 'desc', label: 'NIT (Mayor a Menor)' },
  ];

  selectOption(option: SupplierSortOption) {
    this.sort.emit(option);
    this.close.emit();
  }
}
