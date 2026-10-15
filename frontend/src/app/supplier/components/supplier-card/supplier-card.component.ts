import { Component, input, output, signal, effect } from '@angular/core';
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

  imagenError = signal(false);

  constructor() {
    effect(() => {
      this.supplier().idProveedor;
      this.supplier().imagenUrl;
      this.imagenError.set(false);
    });
  }

  imagenSrc(): string | null {
    const s = this.supplier();
    if (this.imagenError() || !s.imagenUrl) return null;
    return `/api/proveedores/${s.idProveedor}/imagen`;
  }

  onImagenError(): void {
    this.imagenError.set(true);
  }
}
