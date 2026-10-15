import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupplierProductRel } from '../../models/supplier.model';

@Component({
  selector: 'app-supplier-product-item',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './supplier-product-item.component.html',
  styleUrl: './supplier-product-item.component.css'
})
export class SupplierProductItemComponent {
  product = input.required<SupplierProductRel>();
  
  toggleStatus = output<SupplierProductRel>();
  remove = output<SupplierProductRel>();
}
