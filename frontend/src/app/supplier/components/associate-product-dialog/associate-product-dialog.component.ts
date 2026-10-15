import { Component, input, output, inject, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { SupplierService } from '../../services/supplier.service';
import { Product } from '../../../inventory/models/product.model';

@Component({
  selector: 'app-associate-product-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SearchBarComponent, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './associate-product-dialog.component.html',
  styleUrl: './associate-product-dialog.component.css'
})
export class AssociateProductDialogComponent {
  private inventoryService = inject(InventoryService);
  private supplierService = inject(SupplierService);
  private fb = inject(FormBuilder);
  private notificacion = inject(NotificacionService);

  constructor() {
    effect(() => {
      if (this.isVisible()) {
        this.cargarProductos();
      }
    });
  }

  private cargarProductos(): void {
    if (this.allProducts().length > 0) return;
    this.buscando.set(true);
    this.inventoryService.getActiveProducts().subscribe({
      next: (products) => {
        this.allProducts.set(products ?? []);
        this.buscando.set(false);
      },
      error: () => {
        this.buscando.set(false);
        this.notificacion.error('No se pudieron cargar los productos.');
      }
    });
  }

  isVisible = input.required<boolean>();
  supplierId = input.required<number>();

  close = output<void>();
  associated = output<void>();

  allProducts = signal<Product[]>([]);
  searchTerm = signal<string>('');
  selectedProduct = signal<Product | null>(null);
  buscando = signal<boolean>(false);
  guardando = signal<boolean>(false);
  mostrarConfirmacion = signal<boolean>(false);

  productosFiltrados = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    const lista = this.allProducts();
    if (!term) return lista;
    return lista.filter(p =>
      p.nombre.toLowerCase().includes(term) ||
      p.codigoBarras.toLowerCase().includes(term) ||
      (p.descripcion ?? '').toLowerCase().includes(term) ||
      (p.categoria?.nombre ?? '').toLowerCase().includes(term)
    );
  });

  assocForm: FormGroup = this.fb.group({
    codigoProductoProveedor: [''],
    precioReferencia: [null, [Validators.min(0)]]
  });

  getControl(name: string): FormControl {
    return this.assocForm.get(name) as FormControl;
  }

  handleSearch(term: string): void {
    this.searchTerm.set(term ?? '');
  }

  claseStock(p: Product): string {
    if (p.stockActual <= 0) return 'stock-agotado';
    if (p.stockActual <= p.stockMinimo) return 'stock-bajo';
    return 'stock-ok';
  }

  etiquetaStock(p: Product): string {
    if (p.stockActual <= 0) return 'AGOTADO';
    return `${p.stockActual} uds`;
  }

  selectProduct(product: Product): void {
    this.selectedProduct.set(product);
  }

  clearSelection(): void {
    this.selectedProduct.set(null);
    this.assocForm.reset();
    this.mostrarConfirmacion.set(false);
  }

  handleClose(): void {
    this.selectedProduct.set(null);
    this.searchTerm.set('');
    this.assocForm.reset();
    this.mostrarConfirmacion.set(false);
    this.close.emit();
  }

  solicitarAsociacion(): void {
    if (!this.selectedProduct()) return;
    if (this.assocForm.invalid) {
      this.assocForm.markAllAsTouched();
      this.notificacion.advertencia('Revisa el precio de referencia.');
      return;
    }
    this.mostrarConfirmacion.set(true);
  }

  cancelarConfirmacion(): void {
    this.mostrarConfirmacion.set(false);
  }

  confirmAssociation(): void {
    const product = this.selectedProduct();
    if (!product) return;
    this.guardando.set(true);
    this.supplierService.associateProduct(this.supplierId(), {
      productoId: product.id,
      codigoProductoProveedor: this.assocForm.value.codigoProductoProveedor?.trim() || undefined,
      precioReferencia: this.assocForm.value.precioReferencia != null ? Number(this.assocForm.value.precioReferencia) : undefined
    }).subscribe({
      next: () => {
        this.notificacion.exito(`Producto "${product.nombre}" asociado correctamente`);
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
        this.selectedProduct.set(null);
        this.searchTerm.set('');
        this.assocForm.reset();
        this.associated.emit();
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo asociar el producto.');
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
      }
    });
  }
}
