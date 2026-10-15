import { Component, input, output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, FormControl } from '@angular/forms';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';

export interface SalesFilterOptions {
  fechaInicio: string | null;
  fechaFin: string | null;
  vendedor: string | null;
  estado: string | null;
}

@Component({
  selector: 'app-sales-filter',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BottomSheetComponent, FormInputComponent],
  templateUrl: './sales-filter.component.html',
  styles: [`
    .filter-header {
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

    .date-range {
      display: flex;
      flex-direction: column;
      gap: var(--space-m);
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
export class SalesFilterComponent {
  isVisible = input<boolean>(false);
  close = output<void>();
  apply = output<SalesFilterOptions>();

  private fb = inject(FormBuilder);

  filterForm = this.fb.group({
    fechaInicio: [null as string | null],
    fechaFin: [null as string | null],
    vendedor: [null as string | null],
    estado: [null as string | null],
  });

  get startFormControl() { return this.filterForm.get('fechaInicio') as FormControl; }
  get endFormControl() { return this.filterForm.get('fechaFin') as FormControl; }
  get vendorFormControl() { return this.filterForm.get('vendedor') as FormControl; }
  get estadoFormControl() { return this.filterForm.get('estado') as FormControl; }

  estados = [
    { id: 'COMPLETADA', nombre: 'Completada' },
    { id: 'ANULADA', nombre: 'Anulada' },
  ];

  clearFilters() {
    this.filterForm.reset();
  }

  applyFilters() {
    const options: SalesFilterOptions = {
      fechaInicio: this.filterForm.value.fechaInicio ?? null,
      fechaFin: this.filterForm.value.fechaFin ?? null,
      vendedor: this.filterForm.value.vendedor ?? null,
      estado: this.filterForm.value.estado ?? null,
    };
    this.apply.emit(options);
    this.close.emit();
  }
}
