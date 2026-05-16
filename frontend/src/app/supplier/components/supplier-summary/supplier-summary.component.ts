import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-supplier-summary',
  standalone: true,
  template: `
    <section class="summary-container">
      <div class="summary-row">
        <p class="count">
          <span class="highlight">{{ totalSuppliers() }}</span> proveedores encontrados
        </p>

        <button class="sort-btn flex-center" aria-label="Ordenar" (click)="openSort.emit()">
          <span>Ordenar</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5.83L15.17 9.17L16.58 7.76L12 3.17L7.41 7.76L8.83 9.17L12 5.83ZM12 18.17L8.83 14.83L7.42 16.24L12 20.83L16.59 16.24L15.17 14.83L12 18.17Z" fill="currentColor"/>
          </svg>
        </button>
      </div>
    </section>
  `,
  styles: [`
    .summary-container {
      padding: 0 var(--space-l) var(--space-s);
      display: flex;
      flex-direction: column;
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
  `]
})
export class SupplierSummaryComponent {
  totalSuppliers = input<number>(0);
  openSort = output<void>();
}
