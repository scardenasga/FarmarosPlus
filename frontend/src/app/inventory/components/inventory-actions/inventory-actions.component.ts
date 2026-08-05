import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';

@Component({
  selector: 'app-inventory-actions',
  standalone: true,
  imports: [CommonModule, BottomSheetComponent],
  templateUrl: './inventory-actions.component.html',
  styleUrl: './inventory-actions.component.css'
})
export class InventoryActionsComponent {
  isVisible = input<boolean>(false);
  close = output<void>();
  actionSelected = output<string>();

  navigate(path: string) {
    this.actionSelected.emit(path);
    this.close.emit();
  }
}
