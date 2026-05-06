import { Component, input, output, computed } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-product-search-card',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './product-search-card.component.html',
  styleUrl: './product-search-card.component.css'
})
export class ProductSearchCardComponent {
  product = input.required<any>();
  quantity = input<number>(0);
  isSelected = computed(() => this.quantity() > 0);

  add = output<void>();
  increase = output<void>();
  decrease = output<void>();

  get avatarText(): string {
    return this.product().nombre?.substring(0, 3).toUpperCase() || 'PRO';
  }
}
