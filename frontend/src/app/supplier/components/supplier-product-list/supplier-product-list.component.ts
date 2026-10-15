import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupplierProductRel } from '../../models/supplier.model';
import { SupplierProductItemComponent } from '../supplier-product-item/supplier-product-item.component';

@Component({
  selector: 'app-supplier-product-list',
  standalone: true,
  imports: [CommonModule, SupplierProductItemComponent],
  templateUrl: './supplier-product-list.component.html',
  styleUrl: './supplier-product-list.component.css'
})
export class SupplierProductListComponent {
  products = input.required<SupplierProductRel[]>();
  isOpen = signal<boolean>(false);

  toggleStatus = output<SupplierProductRel>();
  remove = output<SupplierProductRel>();
  addProduct = output<void>();

  toggleAccordion(): void {
    this.isOpen.update(v => !v);
  }
}
