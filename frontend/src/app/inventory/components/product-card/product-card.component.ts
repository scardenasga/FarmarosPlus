import {Component, input, output} from '@angular/core';
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
}
