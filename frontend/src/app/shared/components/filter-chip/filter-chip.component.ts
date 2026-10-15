import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-filter-chip',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button 
      class="chip" 
      [class.selected]="selected()" 
      (click)="toggle.emit()"
      type="button"
      role="switch"
      [attr.aria-checked]="selected()"
      [attr.aria-label]="label()"
    >
      <span class="label">{{ label() }}</span>
      @if (selected()) {
        <span class="icon" aria-hidden="true">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 16.17L4.83 12L3.41 13.41L9 19L21 7L19.59 5.59L9 16.17Z" fill="currentColor"/>
          </svg>
        </span>
      }
    </button>
  `,
  styles: [`
    .chip {
      display: inline-flex;
      align-items: center;
      padding: var(--space-xs) var(--space-l);
      height: 32px;
      border-radius: var(--radius-full);
      background-color: var(--surface-container-high);
      color: var(--on-surface-variant);
      border: 1px solid var(--outline);
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      gap: var(--space-xs);
      white-space: nowrap;
      user-select: none;
      outline: none;
    }

    .chip:focus-visible {
      box-shadow: 0 0 0 2px var(--primary);
      border-color: var(--primary);
    }

    .chip.selected {
      background-color: var(--secondary-container);
      color: var(--on-secondary-container);
      border-color: transparent;
    }

    .chip:hover {
      background-color: var(--surface-container-highest);
    }

    .chip.selected:hover {
      opacity: 0.9;
    }

    .icon {
      display: flex;
      align-items: center;
      animation: scaleIn 0.2s ease-out;
    }

    @keyframes scaleIn {
      from { transform: scale(0); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }
  `]
})
export class FilterChipComponent {
  label = input.required<string>();
  selected = input<boolean>(false);
  toggle = output<void>();
}
