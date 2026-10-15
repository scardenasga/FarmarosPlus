import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';
import { FilterChipComponent } from '../../../shared/components/filter-chip/filter-chip.component';

export interface SupplierFilterOptions {
  states: string[];
}

@Component({
  selector: 'app-supplier-filter',
  standalone: true,
  imports: [
    CommonModule, 
    BottomSheetComponent, 
    FilterChipComponent
  ],
  templateUrl: './supplier-filter.component.html',
  styles: [`
    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--space-l);
    }
    
    .filter-header h2 {
      font-size: 20px;
      color: var(--on-surface);
      margin: 0;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--on-surface-variant);
      margin: var(--space-l) 0 var(--space-s) 0;
    }

    .chips-container {
      display: flex;
      flex-wrap: wrap;
      gap: var(--space-s);
    }

    .actions {
      display: flex;
      gap: var(--space-m);
      margin-top: var(--space-xl);
    }

    .btn-clear {
      flex: 1;
      height: 48px;
      border: 1px solid var(--primary);
      color: var(--primary);
      border-radius: var(--radius-full);
      font-weight: 600;
      background: none;
      transition: background-color 0.2s;
    }

    .btn-clear:hover {
      background-color: rgba(131, 213, 197, 0.1);
    }

    .btn-apply {
      flex: 1;
      height: 48px;
      background-color: var(--primary);
      color: var(--on-primary);
      border-radius: var(--radius-full);
      font-weight: 600;
      box-shadow: var(--shadow-1);
    }
  `]
})
export class SupplierFilterComponent {
  isVisible = input<boolean>(false);
  close = output<void>();
  apply = output<SupplierFilterOptions>();

  states = ['ACTIVO', 'INACTIVO'];
  selectedStates = signal<string[]>([]);

  toggleState(state: string) {
    this.selectedStates.update(current => 
      current.includes(state) ? current.filter(s => s !== state) : [...current, state]
    );
  }

  clearFilters() {
    this.selectedStates.set([]);
  }

  applyFilters() {
    const options: SupplierFilterOptions = {
      states: this.selectedStates(),
    };
    this.apply.emit(options);
    this.close.emit();
  }
}
