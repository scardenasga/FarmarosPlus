import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { SupplierCardComponent } from '../../components/supplier-card/supplier-card.component';
import { SupplierSearchComponent } from '../../components/supplier-search/supplier-search.component';
import { SupplierSummaryComponent } from '../../components/supplier-summary/supplier-summary.component';
import { SupplierFilterComponent, SupplierFilterOptions } from '../../components/supplier-filter/supplier-filter.component';
import { SupplierSortComponent, SupplierSortOption, SupplierSortField, SupplierSortOrder } from '../../components/supplier-sort/supplier-sort.component';
import { ActiveFiltersComponent, ActiveFilter } from '../../../inventory/components/active-filters/active-filters.component';
import { Supplier } from '../../models/supplier.model';
import { SupplierService } from '../../services/supplier.service';

@Component({
  selector: 'app-supplier-list',
  standalone: true,
  imports: [
    CommonModule,
    TopBarComponent,
    FabButtonComponent,
    SupplierCardComponent,
    SupplierSearchComponent,
    SupplierSummaryComponent,
    SupplierFilterComponent,
    SupplierSortComponent,
    ActiveFiltersComponent
  ],
  templateUrl: './supplier-list.component.html',
  styleUrl: './supplier-list.component.css'
})
export class SupplierListComponent implements OnInit {
  private router = inject(Router);
  private supplierService = inject(SupplierService);

  private allSuppliers = signal<Supplier[]>([]);
  
  searchTerm = signal<string>('');
  isFilterVisible = signal<boolean>(false);
  isSortVisible = signal<boolean>(false);
  filterOptions = signal<SupplierFilterOptions | null>(null);
  
  sortField = signal<SupplierSortField>('nombre');
  sortOrder = signal<SupplierSortOrder>('asc');

  activeFilterCount = computed(() => {
    const options = this.filterOptions();
    if (!options) return 0;
    
    let count = 0;
    if (options.states.length > 0) count++;
    return count;
  });

  activeFilterChips = computed<ActiveFilter[]>(() => {
    const options = this.filterOptions();
    if (!options) return [];

    const chips: ActiveFilter[] = [];

    // Estados
    options.states.forEach(state => {
      chips.push({ id: `state_${state}`, label: state, type: 'state' });
    });

    return chips;
  });

  filteredSuppliers = computed(() => {
    let suppliers = [...this.allSuppliers()];
    const term = this.searchTerm().toLowerCase();
    const options = this.filterOptions();

    // Filtro por búsqueda
    if (term) {
      suppliers = suppliers.filter(s =>
        s.nombre.toLowerCase().includes(term) ||
        (s.nit && s.nit.toLowerCase().includes(term)) ||
        (s.contacto && s.contacto.toLowerCase().includes(term))
      );
    }

    // Filtro por estado
    if (options && options.states.length > 0) {
      suppliers = suppliers.filter(s => options.states.includes(s.estado));
    }

    // Aplicar Ordenamiento
    const field = this.sortField();
    const order = this.sortOrder();

    suppliers.sort((a, b) => {
      let valA: any = (a as any)[field] || '';
      let valB: any = (b as any)[field] || '';

      if (typeof valA === 'string') {
        valA = valA.toLowerCase();
        valB = valB.toLowerCase();
      }

      if (valA < valB) return order === 'asc' ? -1 : 1;
      if (valA > valB) return order === 'asc' ? 1 : -1;
      return 0;
    });

    return suppliers;
  });

  ngOnInit(): void {
    this.loadSuppliers();
  }

  loadSuppliers(): void {
    this.supplierService.listAll().subscribe({
      next: (suppliers) => this.allSuppliers.set(suppliers),
      error: (err) => console.error('Error loading suppliers', err)
    });
  }

  handleSearch(term: string): void {
    this.searchTerm.set(term);
  }

  toggleFilter(): void {
    this.isFilterVisible.update(v => !v);
  }

  toggleSort(): void {
    this.isSortVisible.update(v => !v);
  }

  handleFilterApply(options: SupplierFilterOptions): void {
    this.filterOptions.set(options);
  }

  handleSortApply(option: SupplierSortOption): void {
    this.sortField.set(option.field);
    this.sortOrder.set(option.order);
  }

  handleRemoveFilter(filter: ActiveFilter): void {
    const current = this.filterOptions();
    if (!current) return;

    const next: SupplierFilterOptions = { ...current };

    if (filter.id.startsWith('state_')) {
      const state = filter.id.replace('state_', '');
      next.states = next.states.filter(s => s !== state);
    }

    this.filterOptions.set(next);
  }

  handleBack(): void {
    this.router.navigate(['/']);
  }

  handleAddSupplier(): void {
    this.router.navigate(['/proveedores/nuevo']);
  }

  handleSupplierClick(supplier: Supplier): void {
    console.log('Supplier clicked:', supplier);
  }
}
