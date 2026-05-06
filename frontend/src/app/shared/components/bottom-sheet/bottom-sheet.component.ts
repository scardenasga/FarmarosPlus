import { Component, input, output, effect, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-bottom-sheet',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="bottom-sheet-overlay" 
      [class.active]="isOpen()" 
      (click)="close.emit()"
      aria-hidden="true"
    ></div>
    <div 
      class="bottom-sheet-container" 
      [class.active]="isOpen()"
      role="dialog"
      [attr.aria-modal]="true"
      [attr.aria-label]="title()"
    >
      <div class="drag-handle" (click)="close.emit()" role="button" aria-label="Cerrar"></div>
      <div class="bottom-sheet-content">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [`
    .bottom-sheet-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(2px);
      z-index: 1000;
      opacity: 0;
      visibility: hidden;
      transition: opacity 0.3s ease, visibility 0.3s;
    }

    .bottom-sheet-overlay.active {
      opacity: 1;
      visibility: visible;
    }

    .bottom-sheet-container {
      position: fixed;
      bottom: 0;
      left: 0;
      width: 100%;
      max-height: 92vh;
      background-color: var(--surface-container-low);
      border-radius: 28px 28px 0 0;
      z-index: 1001;
      transform: translateY(100%);
      transition: transform 0.4s cubic-bezier(0.1, 0.9, 0.2, 1);
      display: flex;
      flex-direction: column;
      box-shadow: 0 -8px 24px rgba(0,0,0,0.2);
    }

    .bottom-sheet-container.active {
      transform: translateY(0);
    }

    .drag-handle {
      width: 32px;
      height: 4px;
      background-color: var(--outline-variant, #89938f);
      border-radius: var(--radius-full);
      margin: var(--space-m) auto;
      cursor: pointer;
      opacity: 0.5;
    }

    .bottom-sheet-content {
      padding: 0 var(--space-l) calc(var(--space-xl) + 20px) var(--space-l);
      overflow-y: auto;
      flex: 1;
    }
  `]
})
export class BottomSheetComponent {
  isOpen = input<boolean>(false);
  title = input<string>('Opciones');
  close = output<void>();
}
