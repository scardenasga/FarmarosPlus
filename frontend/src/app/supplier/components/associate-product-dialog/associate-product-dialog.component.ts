import { Component, input, output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { SupplierService } from '../../services/supplier.service';
import { Product } from '../../../inventory/models/product.model';

@Component({
  selector: 'app-associate-product-dialog',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    BottomSheetComponent, 
    SearchBarComponent, 
    FormInputComponent
  ],
  templateUrl: './associate-product-dialog.component.html',
  styleUrl: './associate-product-dialog.component.css'
})
export class AssociateProductDialogComponent {
  private inventoryService = inject(InventoryService);
  private supplierService = inject(SupplierService);
  private fb = inject(FormBuilder);

  isVisible = input.required<boolean>();
  supplierId = input.required<number>();
  
  close = output<void>();
  associated = output<void>();

  searchResults = signal<Product[]>([]);
  selectedProduct = signal<Product | null>(null);
  
  assocForm: FormGroup = this.fb.group({
    codigoProductoProveedor: [''],
    precioReferencia: [null, [Validators.min(0)]]
  });

  getControl(name: string): FormControl {
    return this.assocForm.get(name) as FormControl;
  }

  handleSearch(term: string): void {
    if (!term.trim()) {
      this.searchResults.set([]);
      return;
    }

    this.inventoryService.searchProducts(term).subscribe({
      next: (products) => this.searchResults.set(products),
      error: (err) => console.error('Error searching products', err)
    });
  }

  selectProduct(product: Product): void {
    this.selectedProduct.set(product);
  }

  clearSelection(): void {
    this.selectedProduct.set(null);
    this.assocForm.reset();
  }

  confirmAssociation(): void {
    const product = this.selectedProduct();
    if (!product) return;

    this.supplierService.associateProduct(this.supplierId(), {
      productoId: product.id,
      codigoProductoProveedor: this.assocForm.value.codigoProductoProveedor || undefined,
      precioReferencia: this.assocForm.value.precioReferencia || undefined
    }).subscribe({
      next: () => {
        this.clearSelection();
        this.associated.emit();
      },
      error: (err) => console.error('Error associating product', err)
    });
  }
}
