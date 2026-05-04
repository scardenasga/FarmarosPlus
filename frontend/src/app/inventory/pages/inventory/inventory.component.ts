import { Component, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { BottomNavBarComponent } from '../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { InventorySearchComponent } from '../../components/inventory-search/inventory-search.component';
import { InventorySummaryComponent } from '../../components/inventory-summary/inventory-summary.component';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    TopBarComponent,
    FabButtonComponent,
    InventorySearchComponent,
    InventorySummaryComponent,
    ProductListComponent
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
   /*
    { id: 6, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 7, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 8, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 9, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 10, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 11, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 12, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 13, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 14, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 15, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 16, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 17, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 },
    { id: 18, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 }*/
  ]);

  searchTerm = signal<string>('');

  filteredProducts = computed(() => {
    const term = this.searchTerm().toLowerCase();
    if (!term) return this.allProducts();
    return this.allProducts().filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.category.toLowerCase().includes(term)
    );
  });

  handleSearch(term: string): void {
    this.searchTerm.set(term);
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
