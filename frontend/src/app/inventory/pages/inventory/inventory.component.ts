import { Component } from '@angular/core';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-inventory',
  imports: [SearchBarComponent],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css'
})
export class InventoryComponent {

  handleSearch(term: string): void {
    console.log('Buscando:', term);
    // Aquí iría la lógica para filtrar los productos
  }

}
