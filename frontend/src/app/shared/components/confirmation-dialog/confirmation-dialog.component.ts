import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.css'
})
export class ConfirmationDialogComponent {
  title = input.required<string>();
  content = input.required<string>();
  cancelText = input<string>('Cancelar');
  confirmText = input<string>('Confirmar');
  showCancel = input<boolean>(true);

  onCancel = output<void>();
  onConfirm = output<void>();

  cancel(): void {
    this.onCancel.emit();
  }

  confirm(): void {
    this.onConfirm.emit();
  }
}
