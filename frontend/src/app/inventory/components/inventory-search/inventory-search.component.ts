import { Component, output, input } from '@angular/core';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { FilterButtonComponent } from '../../../shared/components/filter-button/filter-button.component';

@Component({
  selector: 'app-inventory-search',
  standalone: true,
  imports: [SearchBarComponent, FilterButtonComponent],
  templateUrl: './inventory-search.component.html',
  styleUrl: './inventory-search.component.css'
})
export class InventorySearchComponent {
  search = output<string>();
  toggleFilter = output<void>();
  activeFilters = input<number>(0);
}
