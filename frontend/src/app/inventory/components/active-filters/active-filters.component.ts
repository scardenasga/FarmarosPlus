import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ActiveFilter {
  id: string; // e.g., 'category_1', 'minPrice', etc.
  label: string;
  type: 'category' | 'state' | 'price' | 'margin' | 'expiration' | 'search';
}

@Component({
  selector: 'app-active-filters',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (filters().length > 0) {
      <div class="active-filters-container">
        <div class="chips-scroll">
          @for (filter of filters(); track filter.id) {
            <div class="active-chip">
              <span>{{ filter.label }}</span>
              <button (click)="remove.emit(filter)" aria-label="Quitar filtro">
                <svg width="14" height="14" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M13.5 4.5L4.5 13.5M4.5 4.5L13.5 13.5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .active-filters-container {
      padding: 0 var(--space-l) var(--space-s);
      display: flex;
      align-items: center;
      gap: var(--space-s);
      animation: fadeIn 0.3s ease-out;
    }

    .chips-scroll {
      display: flex;
      gap: var(--space-s);
      overflow-x: auto;
      padding-bottom: 4px;
      scrollbar-width: none; /* Firefox */
    }

    .chips-scroll::-webkit-scrollbar {
      display: none; /* Chrome, Safari, Opera */
    }

    .active-chip {
      display: flex;
      align-items: center;
      gap: var(--space-xs);
      padding: 6px 10px;
      background-color: var(--secondary-container);
      color: var(--on-secondary-container);
      border-radius: var(--radius-m);
      font-size: 12px;
      font-weight: 600;
      white-space: nowrap;
      box-shadow: var(--shadow-1);
    }

    .active-chip button {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2px;
      border-radius: 50%;
      color: var(--on-secondary-container);
      transition: background-color 0.2s;
    }

    .active-chip button:hover {
      background-color: rgba(0, 0, 0, 0.1);
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class ActiveFiltersComponent {
  filters = input<ActiveFilter[]>([]);
  remove = output<ActiveFilter>();
}
