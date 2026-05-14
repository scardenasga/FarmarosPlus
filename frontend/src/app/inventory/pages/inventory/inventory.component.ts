import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { BottomNavBarComponent } from '../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { InventorySearchComponent } from '../../components/inventory-search/inventory-search.component';
import { InventorySummaryComponent } from '../../components/inventory-summary/inventory-summary.component';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { InventoryFilterComponent, InventoryFilterOptions } from '../../components/inventory-filter/inventory-filter.component';
import { Product, Categoria } from '../../models/product.model';
import { InventoryService } from '../../services/inventory.service';
import { CategoriaService } from '../../services/categoria.service';
import { ActiveFiltersComponent, ActiveFilter } from '../../components/active-filters/active-filters.component';
import { InventorySortComponent, SortOption, SortField, SortOrder } from '../../components/inventory-sort/inventory-sort.component';
import { InventoryActionsComponent } from '../../components/inventory-actions/inventory-actions.component';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    TopBarComponent,
    FabButtonComponent,
    InventorySearchComponent,
    InventorySummaryComponent,
    ProductListComponent,
    InventoryFilterComponent,
    ActiveFiltersComponent,
    InventorySortComponent,
    InventoryActionsComponent
  ],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css'
})
export class InventoryComponent implements OnInit {
  private router = inject(Router);
  private inventoryService = inject(InventoryService);
  private categoriaService = inject(CategoriaService);
  
  private allProducts = signal<Product[]>([]);
  private categories = signal<Categoria[]>([]);

  searchTerm = signal<string>('');
  isFilterVisible = signal<boolean>(false);
  isSortVisible = signal<boolean>(false);
  isActionsVisible = signal<boolean>(false);
  filterOptions = signal<InventoryFilterOptions | null>(null);
  
  sortField = signal<SortField>('nombre');
  sortOrder = signal<SortOrder>('asc');

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  loadProducts(): void {
    this.inventoryService.getActiveProducts().subscribe({
      next: (products) => this.allProducts.set(products),
      error: (err) => console.error('Error loading products', err)
    });
  }

  loadCategories(): void {
    this.categoriaService.listar().subscribe(cats => this.categories.set(cats));
  }

  activeFilterCount = computed(() => {
    const options = this.filterOptions();
    if (!options) return 0;
    
    let count = 0;
    if (options.categories.length > 0) count++;
    if (options.states.length > 0) count++;
    if (options.minPrice !== null || options.maxPrice !== null) count++;
    if (options.minMargin !== null || options.maxMargin !== null) count++;
    if (options.expirationDate !== null) count++;
    
    return count;
  });

  activeFilterChips = computed<ActiveFilter[]>(() => {
    const options = this.filterOptions();
    if (!options) return [];

    const chips: ActiveFilter[] = [];

    // Categorías
    options.categories.forEach(id => {
      const cat = this.categories().find(c => c.id === id);
      if (cat) {
        chips.push({ id: `category_${id}`, label: cat.nombre, type: 'category' });
      }
    });

    // Estados
    options.states.forEach(state => {
      chips.push({ id: `state_${state}`, label: state, type: 'state' });
    });

    // Precios
    if (options.minPrice !== null) chips.push({ id: 'minPrice', label: `Min: $${options.minPrice}`, type: 'price' });
    if (options.maxPrice !== null) chips.push({ id: 'maxPrice', label: `Max: $${options.maxPrice}`, type: 'price' });

    // Márgenes
    if (options.minMargin !== null) chips.push({ id: 'minMargin', label: `Margen > ${options.minMargin}%`, type: 'margin' });
    if (options.maxMargin !== null) chips.push({ id: 'maxMargin', label: `Margen < ${options.maxMargin}%`, type: 'margin' });

    // Fecha
    if (options.expirationDate) chips.push({ id: 'expirationDate', label: `Antes de: ${options.expirationDate}`, type: 'expiration' });

    return chips;
  });

  filteredProducts = computed(() => {
    let products = [...this.allProducts()];
    const term = this.searchTerm().toLowerCase();
    const options = this.filterOptions();

    if (term) {
      products = products.filter(p =>
        p.nombre.toLowerCase().includes(term) ||
        p.categoria?.nombre.toLowerCase().includes(term) ||
        p.codigoBarras.includes(term)
      );
    }

    if (options) {
      if (options.categories.length > 0) {
        products = products.filter(p => p.categoria && options.categories.includes(p.categoria.id));
      }
      
      if (options.states.length > 0) {
        products = products.filter(p => options.states.includes(p.estado));
      }

      if (options.minPrice !== null) {
        products = products.filter(p => p.precioVenta >= (options.minPrice ?? 0));
      }
      if (options.maxPrice !== null) {
        products = products.filter(p => p.precioVenta <= (options.maxPrice ?? Infinity));
      }

      if (options.minMargin !== null) {
        products = products.filter(p => (p.margenGanancia ?? 0) >= (options.minMargin ?? 0));
      }
      if (options.maxMargin !== null) {
        products = products.filter(p => (p.margenGanancia ?? 0) <= (options.maxMargin ?? 100));
      }
    }

    // Aplicar Ordenamiento
    const field = this.sortField();
    const order = this.sortOrder();

    products.sort((a, b) => {
      let valA: any = (a as any)[field];
      let valB: any = (b as any)[field];

      if (typeof valA === 'string') {
        valA = valA.toLowerCase();
        valB = valB.toLowerCase();
      }

      if (valA < valB) return order === 'asc' ? -1 : 1;
      if (valA > valB) return order === 'asc' ? 1 : -1;
      return 0;
    });

    return products;
  });

  handleSearch(term: string): void {
    this.searchTerm.set(term);
  }

  toggleFilter(): void {
    this.isFilterVisible.update(v => !v);
  }

  toggleSort(): void {
    this.isSortVisible.update(v => !v);
  }

  toggleActions(): void {
    this.isActionsVisible.update(v => !v);
  }

  handleActionSelection(action: string): void {
    if (action === 'categorias') {
      this.router.navigate(['/inventario/categorias']);
    } else if (action === 'ingreso') {
      this.router.navigate(['/inventario/ingreso']);
    } else if (action === 'crear') {
      this.handleAddProduct();
    }
  }

  handleFilterApply(options: InventoryFilterOptions): void {
    this.filterOptions.set(options);
  }

  handleSortApply(option: SortOption): void {
    this.sortField.set(option.field);
    this.sortOrder.set(option.order);
  }

  handleRemoveFilter(filter: ActiveFilter): void {
    const current = this.filterOptions();
    if (!current) return;

    const next: InventoryFilterOptions = { ...current };

    if (filter.id.startsWith('category_')) {
      const id = parseInt(filter.id.replace('category_', ''));
      next.categories = next.categories.filter(c => c !== id);
    } else if (filter.id.startsWith('state_')) {
      const state = filter.id.replace('state_', '');
      next.states = next.states.filter(s => s !== state);
    } else if (filter.id === 'minPrice') {
      next.minPrice = null;
    } else if (filter.id === 'maxPrice') {
      next.maxPrice = null;
    } else if (filter.id === 'minMargin') {
      next.minMargin = null;
    } else if (filter.id === 'maxMargin') {
      next.maxMargin = null;
    } else if (filter.id === 'expirationDate') {
      next.expirationDate = null;
    }

    this.filterOptions.set(next);
  }

  handleBack(): void {
    this.router.navigate(['/']);
  }

  handleAlerts(): void {
    this.router.navigate(['/alertas']);
  }

  handleCategorias(): void {
    this.router.navigate(['/inventario/categorias']);
  }

  handleAddProduct(): void {
    this.router.navigate(['/inventario/crear']);
  }

  handleProductSelect(product: Product): void {
    this.router.navigate(['/inventario', product.id]);
  }
}
