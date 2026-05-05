import { Component, output } from '@angular/core';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-inventory-search',
  standalone: true,
  imports: [SearchBarComponent],
  template: `
    <div class="search-row">
      <app-search-bar
        [placeholder]="'Buscar Productos'"
        (search)="search.emit($event)"
      ></app-search-bar>

      <button class="filter-btn flex-center" (click)="toggleFilter.emit()" aria-label="Filtros">
        <svg width="30" height="30" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M10 18H14V16H10V18ZM3 6V8H21V6H3ZM6 13H18V11H6V13Z" fill="var(--on-secondary-container)"/>
        </svg>
      </button>
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
    .filter-btn {
      width: 40px;
      height: 40px;
      background-color: var(--secondary);
      border-radius: var(--radius-full);
      color: var(--on-secondary);
      flex-shrink: 0;
    }
  `]
})
export class InventorySearchComponent {
  search = output<string>();
  toggleFilter = output<void>();
}
