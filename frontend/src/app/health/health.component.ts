import {Component, computed, signal} from '@angular/core';
import {Product} from '../inventory/models/product.model';
import {ProductListComponent} from '../inventory/components/product-list/product-list.component';
import {ProductCardComponent} from '../inventory/components/product-card/product-card.component';
import {TopBarComponent} from '../shared/components/top-bar/top-bar.component';
import {SearchBarComponent} from '../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-health',
  imports: [
    TopBarComponent,
    SearchBarComponent
  ],
  templateUrl: './health.component.html',
  styleUrl: './health.component.css'
})
export class HealthComponent {

  handleBack(): void {
    console.log('Regresando...');
  }

  handleAlerts(): void {
    console.log('Abriendo alertas...');
  }

}
