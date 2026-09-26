import { Component, input, output, computed, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product-search-card',
  standalone: true,
  imports: [CommonModule],
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

  imagenError = signal(false);

  constructor() {
    effect(() => {
      this.product().id;
      this.product().imagenUrl;
      this.imagenError.set(false);
    });
  }

  imagenSrc(): string | null {
    const p = this.product();
    if (this.imagenError() || !p.imagenUrl) return null;
    return `/api/productos/${p.id}/imagen`;
  }

  onImagenError(): void {
    this.imagenError.set(true);
  }

  /** Un clic en cualquier parte de la tarjeta agrega el producto a la venta. */
  seleccionar(): void {
    if (!this.isSelected()) {
      this.add.emit();
    }
  }
}
