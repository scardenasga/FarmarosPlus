import {Component, computed, signal} from '@angular/core';
import {Product} from '../inventory/models/product.model';
import {ProductListComponent} from '../inventory/components/product-list/product-list.component';
import {ProductCardComponent} from '../inventory/components/product-card/product-card.component';
import {TopBarComponent} from '../shared/components/top-bar/top-bar.component';
import {SearchBarComponent} from '../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-health',
  imports: [
    ProductListComponent,
    ProductCardComponent,
    TopBarComponent,
    SearchBarComponent
  ],
  templateUrl: './health.component.html',
  styleUrl: './health.component.css'
})
export class HealthComponent {
  private allProducts = signal<Product[]>([
    { id: 1, name: 'Acetaminofen 90mL', price: 7500, stock: 26, unit: 'und.', category: 'Jarabe', trend: 1.2 },
    { id: 2, name: 'Ibuprofeno 400mg', price: 12000, stock: 15, unit: 'und.', category: 'Tableta', trend: -0.5 },
    { id: 3, name: 'Amoxicilina 500mg', price: 15000, stock: 40, unit: 'und.', category: 'Cápsula', trend: 2.1 },
    { id: 4, name: 'Loratadina 10mg', price: 5000, stock: 50, unit: 'und.', category: 'Tableta', trend: 0.0 },
    { id: 5, name: 'Vitamina C 500mg', price: 8000, stock: 100, unit: 'und.', category: 'Efervescente', trend: 5.4 }
  ]);

  mostrarProductos = computed(() =>{
    return this.allProducts();
  });
  getProducto = computed(() =>{
    return this.allProducts()[2];
  })
  handleBack(): void {
    console.log('Regresando...');
  }

  handleAlerts(): void {
    console.log('Abriendo alertas...');
  }

}
