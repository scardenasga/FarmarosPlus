import { Component, input, output, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, FormControl } from '@angular/forms';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';
import { FilterChipComponent } from '../../../shared/components/filter-chip/filter-chip.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { CategoriaService } from '../../services/categoria.service';
import { Categoria } from '../../models/product.model';

export interface InventoryFilterOptions {
  categories: number[];
  states: string[];
  minPrice: number | null;
  maxPrice: number | null;
  minMargin: number | null;
  maxMargin: number | null;
  expirationDate: string | null;
}

@Component({
  selector: 'app-inventory-filter',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    BottomSheetComponent, 
    FilterChipComponent, 
    FormInputComponent
  ],
  templateUrl: './inventory-filter.component.html',
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

    .range-inputs {
      display: flex;
      gap: var(--space-m);
    }

    .range-inputs app-form-input {
      flex: 1;
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
export class InventoryFilterComponent implements OnInit {
  isVisible = input<boolean>(false);
  close = output<void>();
  apply = output<InventoryFilterOptions>();

  private fb = inject(FormBuilder);
  private categoriaService = inject(CategoriaService);

  categories = signal<Categoria[]>([]);
  selectedCategories = signal<number[]>([]);
  
  states = ['ACTIVO', 'INACTIVO', 'STOCK BAJO'];
  selectedStates = signal<string[]>([]);

  filterForm = this.fb.group({
    minPrice: [null as number | null],
    maxPrice: [null as number | null],
    minMargin: [null as number | null],
    maxMargin: [null as number | null],
    expirationDate: [null as string | null],
  });

  get minPriceControl() { return this.filterForm.get('minPrice') as FormControl; }
  get maxPriceControl() { return this.filterForm.get('maxPrice') as FormControl; }
  get minMarginControl() { return this.filterForm.get('minMargin') as FormControl; }
  get maxMarginControl() { return this.filterForm.get('maxMargin') as FormControl; }
  get expirationDateControl() { return this.filterForm.get('expirationDate') as FormControl; }

  ngOnInit() {
    this.categoriaService.listar().subscribe(cats => {
      this.categories.set(cats);
    });
  }

  toggleCategory(id: number) {
    this.selectedCategories.update(current => 
      current.includes(id) ? current.filter(c => c !== id) : [...current, id]
    );
  }

  toggleState(state: string) {
    this.selectedStates.update(current => 
      current.includes(state) ? current.filter(s => s !== state) : [...current, state]
    );
  }

  clearFilters() {
    this.selectedCategories.set([]);
    this.selectedStates.set([]);
    this.filterForm.reset();
  }

  applyFilters() {
    const options: InventoryFilterOptions = {
      categories: this.selectedCategories(),
      states: this.selectedStates(),
      minPrice: this.filterForm.value.minPrice ?? null,
      maxPrice: this.filterForm.value.maxPrice ?? null,
      minMargin: this.filterForm.value.minMargin ?? null,
      maxMargin: this.filterForm.value.maxMargin ?? null,
      expirationDate: this.filterForm.value.expirationDate ?? null,
    };
    this.apply.emit(options);
    this.close.emit();
  }
}
