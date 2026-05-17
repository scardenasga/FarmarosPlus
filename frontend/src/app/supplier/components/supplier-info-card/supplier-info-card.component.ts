import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Supplier } from '../../models/supplier.model';

@Component({
  selector: 'app-supplier-info-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './supplier-info-card.component.html',
  styleUrl: './supplier-info-card.component.css'
})
export class SupplierInfoCardComponent {
  supplier = input.required<Supplier>();
}
