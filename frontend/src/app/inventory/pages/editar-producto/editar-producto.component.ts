import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import {Product, Categoria, ProductoDetalleResponse, ActualizarProductoRequest} from '../../models/product.model';
import { InventoryService } from '../../services/inventory.service';
import { CategoriaService } from '../../services/categoria.service';

@Component({
  selector: 'app-editar-producto',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TopBarComponent, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './editar-producto.component.html',
  styleUrl: './editar-producto.component.css'
})
export class EditarProductoComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private inventoryService = inject(InventoryService);
  private categoriaService = inject(CategoriaService);

  productForm!: FormGroup;
  product = signal<ProductoDetalleResponse | null>(null);
  categories = signal<Categoria[]>([]);
  showConfirmDialog = signal<boolean>(false);

  states = [
    { id: 'ACTIVO', nombre: 'ACTIVO' },
    { id: 'INACTIVO', nombre: 'INACTIVO' },
    { id: 'DESCONTINUADO', nombre: 'DESCONTINUADO' }
  ];

  ngOnInit(): void {
    this.navService.hideNav();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCategories();
    if (id) {
      this.loadProduct(id);
    } else {
      this.router.navigate(['/inventario']);
    }
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  private loadProduct(id: number): void {
    this.inventoryService.getProductDetail(id).subscribe({
      next: (found) => {
        this.product.set(found);
        this.initForm(found);
      },
      error: (err) => {
        console.error('Error loading product', err);
        this.router.navigate(['/inventario']);
      }
    });
  }

  private initForm(p: ProductoDetalleResponse): void {
    this.productForm = this.fb.group({
      nombre: [p.nombre, [Validators.required, Validators.minLength(3)]],
      descripcion: [p.descripcion],
      categoriaId: [p.categoria?.id, [Validators.required]],
      estado: [p.estado, [Validators.required]],
      stockActual: [p.stockActual, [Validators.required, Validators.min(0)]],
      stockMinimo: [p.stockMinimo, [Validators.required, Validators.min(0)]],
      costo: [p.costo, [Validators.required, Validators.min(0)]],
      porcentajeIva: [p.porcentajeIva, [Validators.required, Validators.min(0)]],
      precioVenta: [p.precioVenta, [Validators.required, Validators.min(0)]],
      requierePrescripcion: [p.requierePrescripcion]
    }, { validators: this.priceValidator });
  }

  private priceValidator(group: FormGroup): { [key: string]: any } | null {
    const costo = group.get('costo')?.value || 0;
    const precioVenta = group.get('precioVenta')?.value || 0;
    const iva = group.get('porcentajeIva')?.value || 0;

    const minPrecio = costo * (1 + iva / 100);

    if (precioVenta <= minPrecio && precioVenta > 0) {
      return { priceTooLow: true };
    }
    return null;
  }

  private loadCategories(): void {
    this.categoriaService.listar().subscribe({
      next: (cats) => this.categories.set(cats),
      error: (err) => console.error('Error loading categories', err)
    });
  }

  getControl(name: string) {
    return this.productForm.get(name) as any;
  }

  onSaveRequest(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }
    this.showConfirmDialog.set(true);
  }

  confirmUpdate(): void {
    const id = this.product()?.id;
    if (!id) return;

    const formValue = this.productForm.value;
    const updateRequest: ActualizarProductoRequest = {
      nombre: formValue.nombre,
      descripcion: formValue.descripcion,
      categoriaId: Number(formValue.categoriaId),
      estado: formValue.estado,
      stockActual: formValue.stockActual,
      stockMinimo: formValue.stockMinimo,
      costo: formValue.costo,
      porcentajeIva: formValue.porcentajeIva,
      precioVenta: formValue.precioVenta,
      requierePrescripcion: formValue.requierePrescripcion
    };

    this.inventoryService.updateProduct(id, updateRequest).subscribe({
      next: () => {
        this.showConfirmDialog.set(false);
        this.router.navigate(['/inventario', id]);
      },
      error: (err) => {
        console.error('Error updating product', err);
        this.showConfirmDialog.set(false);
      }
    });
  }

  cancelUpdate(): void {
    this.showConfirmDialog.set(false);
  }

  onCancel(): void {
    this.router.navigate(['/inventario', this.product()?.id]);
  }
}
