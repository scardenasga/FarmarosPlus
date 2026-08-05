import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';

export type SortField = 'nombre' | 'precioVenta' | 'stockActual' | 'margenGanancia';
export type SortOrder = 'asc' | 'desc';

export interface SortOption {
  field: SortField;
  order: SortOrder;
  label: string;
}

@Component({
  selector: 'app-inventory-sort',
  standalone: true,
  imports: [CommonModule, BottomSheetComponent],
  templateUrl: './inventory-sort.component.html',
  styleUrl: './inventory-sort.component.css'
})
export class InventorySortComponent {
  isVisible = input<boolean>(false);
  currentField = input<SortField>('nombre');
  currentOrder = input<SortOrder>('asc');
  
  close = output<void>();
  sort = output<SortOption>();

  options: SortOption[] = [
    { field: 'nombre', order: 'asc', label: 'Nombre (A-Z)' },
    { field: 'nombre', order: 'desc', label: 'Nombre (Z-A)' },
    { field: 'precioVenta', order: 'asc', label: 'Menor Precio' },
    { field: 'precioVenta', order: 'desc', label: 'Mayor Precio' },
    { field: 'stockActual', order: 'asc', label: 'Menor Stock' },
    { field: 'stockActual', order: 'desc', label: 'Mayor Stock' },
    { field: 'margenGanancia', order: 'desc', label: 'Mayor Margen' },
  ];

  selectOption(option: SortOption) {
    this.sort.emit(option);
    this.close.emit();
  }
}
