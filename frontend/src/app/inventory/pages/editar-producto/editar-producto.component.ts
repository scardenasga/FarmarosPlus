import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { Product, Categoria } from '../../models/product.model';
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
  product = signal<Product | null>(null);
  categories = signal<Categoria[]>([]);
  showConfirmDialog = signal<boolean>(false);

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
    this.inventoryService.getProductById(id).subscribe({
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

  private initForm(p: Product): void {
    this.productForm = this.fb.group({
      nombre: [p.nombre, [Validators.required]],
      precioVenta: [p.precioVenta, [Validators.required, Validators.min(0)]],
      stockMinimo: [p.stockMinimo, [Validators.required, Validators.min(0)]],
      categoriaId: [p.categoria?.id, [Validators.required]]
    });
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
    if (this.productForm.invalid) return;
    this.showConfirmDialog.set(true);
  }

  confirmUpdate(): void {
    // Note: Documentation shows PATCH /api/productos/codigo-barras/{codigoBarras}/precio
    // For now we simulate the update or log the intent as specified in the service.
    console.log('Solicitud de actualización para:', this.product()?.id, this.productForm.value);
    this.showConfirmDialog.set(false);
    this.router.navigate(['/inventario', this.product()?.id]);
  }

  cancelUpdate(): void {
    this.showConfirmDialog.set(false);
  }

  onCancel(): void {
    this.router.navigate(['/inventario', this.product()?.id]);
  }
}
