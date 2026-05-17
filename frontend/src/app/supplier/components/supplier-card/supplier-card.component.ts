import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Supplier } from '../../models/supplier.model';

@Component({
  selector: 'app-supplier-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './supplier-card.component.html',
  styleUrl: './supplier-card.component.css'
})
export class SupplierCardComponent {
  supplier = input.required<Supplier>();
  clicked = output<void>();
}
