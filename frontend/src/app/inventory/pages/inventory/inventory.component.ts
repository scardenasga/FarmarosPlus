import { Component, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { BottomNavBarComponent } from '../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { InventorySearchComponent } from '../../components/inventory-search/inventory-search.component';
import { InventorySummaryComponent } from '../../components/inventory-summary/inventory-summary.component';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { InventoryFilterComponent, InventoryFilterOptions } from '../../components/inventory-filter/inventory-filter.component';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    TopBarComponent,
    FabButtonComponent,
    InventorySearchComponent,
    InventorySummaryComponent,
    ProductListComponent,
    InventoryFilterComponent
  ],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css'
})
export class InventoryComponent {
  private router = inject(Router);
  private allProducts = signal<Product[]>([
    { id: 1, name: 'Acetaminofen 90mL', price: 7500, stock: 26, unit: 'und.', category: 'Jarabe', trend: 1.2 },
    { id: 2, name: 'Ibuprofeno 400mg', price: 12000, stock: 15, unit: 'und.', category: 'Tableta', trend: -0.5 },
    { id: 3, name: 'Amoxicilina 500mg', price: 15000, stock: 40, unit: 'und.', category: 'Cápsula', trend: 2.1 },
    { id: 4, name: 'Loratadina 10mg', price: 5000, stock: 50, unit: 'und.', category: 'Tableta', trend: 0.0 },
    { id: 5, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
  ]);

  searchTerm = signal<string>('');
  isFilterVisible = signal<boolean>(false);
  filterOptions = signal<InventoryFilterOptions | null>(null);

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

  filteredProducts = computed(() => {
    let products = this.allProducts();
    const term = this.searchTerm().toLowerCase();
    const options = this.filterOptions();

    if (term) {
      products = products.filter(p =>
        p.name.toLowerCase().includes(term) ||
        p.category.toLowerCase().includes(term)
      );
    }

    if (options) {
      if (options.categories.length > 0) {
        // Mock: categories in Product are strings, in filter are IDs. 
        // We'd need a mapping, but for now let's assume category name matches.
        // products = products.filter(p => options.categories.includes(p.categoryId));
      }
      
      if (options.minPrice !== null) {
        products = products.filter(p => p.price >= (options.minPrice ?? 0));
      }
      if (options.maxPrice !== null) {
        products = products.filter(p => p.price <= (options.maxPrice ?? Infinity));
      }
    }

    return products;
  });

  handleSearch(term: string): void {
    this.searchTerm.set(term);
  }

  toggleFilter(): void {
    this.isFilterVisible.update(v => !v);
  }

  handleFilterApply(options: InventoryFilterOptions): void {
    this.filterOptions.set(options);
  }

  handleBack(): void {
    console.log('Regresando...');
  }

  handleAlerts(): void {
    console.log('Abriendo alertas...');
  }

  handleAddProduct(): void {
    this.router.navigate(['/inventario/crear']);
  }

  handleProductSelect(product: Product): void {
    this.router.navigate(['/inventario', product.id]);
  }
}

