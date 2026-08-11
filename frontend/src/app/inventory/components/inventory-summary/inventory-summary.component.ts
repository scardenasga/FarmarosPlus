import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventory-summary.component.html',
  styleUrl: './inventory-summary.component.css'
})
export class InventorySummaryComponent {
  @Input() totalProducts: number = 0;
  @Input() viewMode: 'grid' | 'lotes' = 'grid';
  @Output() openSort = new EventEmitter<void>();
  @Output() toggleView = new EventEmitter<void>();
}
