import { Component, output, input } from '@angular/core';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { FilterButtonComponent } from '../../../shared/components/filter-button/filter-button.component';

@Component({
  selector: 'app-supplier-search',
  standalone: true,
  imports: [SearchBarComponent, FilterButtonComponent],
  template: `
    <div class="search-row">
      <app-search-bar
        [placeholder]="'Buscar Proveedores'"
        (search)="search.emit($event)"
      ></app-search-bar>

      <app-filter-button
        [activeCount]="activeFilters()"
        (clicked)="toggleFilter.emit()"
      ></app-filter-button>
    </div>
  `,
  styles: [`
    .search-row {
      display: flex;
      gap: var(--space-m);
      align-items: center;
      width: 100%;
      padding: var(--space-l);
      background-color: var(--background);
    }
    app-search-bar {
      flex: 1;
    }
  `]
})
export class SupplierSearchComponent {
  search = output<string>();
  toggleFilter = output<void>();
  activeFilters = input<number>(0);
}
