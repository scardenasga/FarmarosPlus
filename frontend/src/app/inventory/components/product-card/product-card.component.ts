import {Component, effect, input, output, signal} from '@angular/core';
import { Product } from '../../models/product.model';
import { CommonModule, CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: `./product-card.component.html`,
  styleUrl: './product-card.component.css'
})
export class ProductCardComponent {
  product = input.required<Product>();
  clicked = output<void>();

  imagenError = signal(false);

  constructor() {
    effect(() => {
      // resetea error cuando cambia el producto (imagenUrl o id)
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
}
