import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { BottomNavBarComponent } from '../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { InventorySearchComponent } from '../../components/inventory-search/inventory-search.component';
import { InventorySummaryComponent } from '../../components/inventory-summary/inventory-summary.component';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { InventoryFilterComponent, InventoryFilterOptions } from '../../components/inventory-filter/inventory-filter.component';
import { Product } from '../../models/product.model';
import { InventoryService } from '../../services/inventory.service';

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
export class InventoryComponent implements OnInit {
  private router = inject(Router);
  private inventoryService = inject(InventoryService);
  
  private allProducts = signal<Product[]>([]);

  searchTerm = signal<string>('');
  isFilterVisible = signal<boolean>(false);
  filterOptions = signal<InventoryFilterOptions | null>(null);

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.inventoryService.getActiveProducts().subscribe({
      next: (products) => this.allProducts.set(products),
      error: (err) => console.error('Error loading products', err)
    });
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

  filteredProducts = computed(() => {
    let products = this.allProducts();
    const term = this.searchTerm().toLowerCase();
    const options = this.filterOptions();

    if (term) {
      products = products.filter(p =>
        p.nombre.toLowerCase().includes(term) ||
        p.categoria?.nombre.toLowerCase().includes(term)
      );
    }

    if (options) {
      if (options.categories.length > 0) {
        products = products.filter(p => p.categoria && options.categories.includes(p.categoria.id));
      }
      
      if (options.minPrice !== null) {
        products = products.filter(p => p.precioVenta >= (options.minPrice ?? 0));
      }
      if (options.maxPrice !== null) {
        products = products.filter(p => p.precioVenta <= (options.maxPrice ?? Infinity));
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

