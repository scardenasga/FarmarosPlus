import { Component, input } from '@angular/core';

@Component({
  selector: 'app-inventory-summary',
  standalone: true,
  template: `
    <section class="summary-container">
      <div class="summary-row">
        <p class="count">
          <span class="highlight">{{ totalProducts() }}</span> productos encontrados
        </p>

        <button class="sort-btn flex-center" aria-label="Filtrar">
          <span>Filtrar</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5.83L15.17 9.17L16.58 7.76L12 3.17L7.41 7.76L8.83 9.17L12 5.83ZM12 18.17L8.83 14.83L7.42 16.24L12 20.83L16.59 16.24L15.17 14.83L12 18.17Z" fill="currentColor"/>
          </svg>
        </button>
      </div>

      <div class="chips-row">
        <div class="chip">
          <span>selección</span>
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M13.5 4.5L4.5 13.5M4.5 4.5L13.5 13.5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .summary-container {
      padding: 0 var(--space-l) var(--space-l);
      display: flex;
      flex-direction: column;
      gap: var(--space-l);
    }
    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .count {
      margin: 0;
      font-size: 14px;
      color: var(--on-background);
    }
    .highlight {
      font-weight: 700;
    }
    .sort-btn {
      display: flex;
      align-items: center;
      gap: var(--space-xs);
      font-size: 14px;
      color: var(--on-background);
    }
    .chips-row {
      display: flex;
      gap: var(--space-s);
      flex-wrap: wrap;
    }
    .chip {
      display: flex;
      align-items: center;
      gap: var(--space-xs);
      padding: var(--space-xs) var(--space-s);
      background-color: var(--secondary);
      color: var(--on-secondary);
      border-radius: var(--radius-l);
      font-size: 12px;
    }
  `]
})
export class InventorySummaryComponent {
  totalProducts = input<number>(0);
}
