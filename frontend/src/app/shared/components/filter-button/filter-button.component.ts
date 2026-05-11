import { Component, output, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-filter-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button 
      class="filter-btn flex-center" 
      (click)="clicked.emit()" 
      [attr.aria-label]="label()"
      type="button"
    >
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M10 18H14V16H10V18ZM3 6V8H21V6H3ZM6 13H18V11H6V13Z" fill="currentColor"/>
      </svg>
      @if (activeCount() > 0) {
        <span class="badge">{{ activeCount() }}</span>
      }
    </button>
    `,
    styles: [`
    .filter-btn {
      position: relative;
      width: 48px;
      height: 48px;
      background-color: var(--secondary-container);
      color: var(--on-secondary-container);
      border-radius: var(--radius-full);
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid transparent;
      flex-shrink: 0;
      box-shadow: var(--shadow-1);
    }


    .filter-btn:hover {
      background-color: var(--secondary);
      color: var(--on-secondary);
      box-shadow: var(--shadow-1);
    }

    .filter-btn:active {
      transform: scale(0.95);
    }

    .filter-btn:focus-visible {
      outline: 2px solid var(--primary);
      outline-offset: 2px;
    }

    .badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background-color: var(--primary);
      color: var(--on-primary);
      font-size: 11px;
      font-weight: bold;
      min-width: 18px;
      height: 18px;
      padding: 0 4px;
      border-radius: var(--radius-full);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid var(--background);
      animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    @keyframes popIn {
      from { transform: scale(0); }
      to { transform: scale(1); }
    }

    .flex-center {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  `]
})
export class FilterButtonComponent {
  label = input<string>('Filtrar');
  activeCount = input<number>(0);
  clicked = output<void>();
}
