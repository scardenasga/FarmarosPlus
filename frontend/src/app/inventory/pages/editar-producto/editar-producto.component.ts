import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { Product } from '../../models/product.model';

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

  productForm!: FormGroup;
  product = signal<Product | null>(null);
  categories = signal<{id: number, nombre: string}[]>([]);
  showConfirmDialog = signal<boolean>(false);

  ngOnInit(): void {
    this.navService.hideNav();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCategories();
    this.loadProduct(id);
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  private loadProduct(id: number): void {
    // Mock loading product data
    const mockProducts: Product[] = [
      { 
        id: 1, 
        name: 'Acetaminofen 90mL', 
        price: 7500, 
        stock: 26, 
        unit: 'und.', 
        category: 'Jarabe', 
        description: 'Jarabe pediátrico para el alivio rápido del dolor y la fiebre.',
        trend: 1.2
      }
    ];

    const found = mockProducts.find(p => p.id === id) || mockProducts[0];
    this.product.set(found);
    this.initForm(found);
  }

  private initForm(p: Product): void {
    this.productForm = this.fb.group({
      nombre: [p.name, [Validators.required]],
      descripcion: [p.description || ''],
      precioVenta: [p.price, [Validators.required, Validators.min(0)]],
      stockMinimo: [10, [Validators.required, Validators.min(0)]],
      categoriaId: [1, [Validators.required]] // Hardcoded for demo
    });
  }

  private loadCategories(): void {
    this.categories.set([
      { id: 1, nombre: 'Analgésicos' },
      { id: 2, nombre: 'Antibióticos' },
      { id: 3, nombre: 'Vitaminas' }
    ]);
  }

  getControl(name: string) {
    return this.productForm.get(name) as any;
  }

  onSaveRequest(): void {
    if (this.productForm.invalid) return;
    this.showConfirmDialog.set(true);
  }

  confirmUpdate(): void {
    console.log('Cambios guardados (simulado):', this.productForm.value);
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
