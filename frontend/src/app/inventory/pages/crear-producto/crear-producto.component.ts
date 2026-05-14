import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { InventoryService } from '../../services/inventory.service';
import { CategoriaService } from '../../services/categoria.service';
import { CreateProductRequest, Categoria } from '../../models/product.model';

@Component({
  selector: 'app-crear-producto',
  standalone: true,
  imports: [ReactiveFormsModule, TopBarComponent, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './crear-producto.component.html',
  styleUrl: './crear-producto.component.css'
})
export class CrearProductoComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private inventoryService = inject(InventoryService);
  private categoriaService = inject(CategoriaService);

  productForm!: FormGroup;
  categories = signal<Categoria[]>([]);
  showConfirmation = signal<boolean>(false);

  readonly ivaOptions = [
    { id: 0,  nombre: '0% — Medicamentos' },
    { id: 19, nombre: '19% — Cosméticos / otros' }
  ];

  ngOnInit(): void {
    this.navService.hideNav();
    this.initForm();
    this.loadCategories();
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  getControl(name: string): FormControl {
    return this.productForm.get(name) as FormControl;
  }

  private initForm(): void {
    this.productForm = this.fb.group({
      nombre: ['', [Validators.required]],
      descripcion: [''],
      codigoBarras: ['', [Validators.required]],
      stockInicial: [0, [Validators.required, Validators.min(0)]],
      stockMinimo: [0, [Validators.required, Validators.min(0)]],
      costo: [0, [Validators.required, Validators.min(0)]],
      precioVenta: [0, [Validators.required, Validators.min(0)]],
      porcentajeIva: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
      hasLote: [false],
      numeroLote: [''],
      categoriaId: [null, [Validators.required]]
    });
  }

  private loadCategories(): void {
    this.categoriaService.listar().subscribe({
      next: (cats) => this.categories.set(cats),
      error: (err) => console.error('Error loading categories', err)
    });
  }

  onSave(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }
    this.showConfirmation.set(true);
  }

  confirmCreation(): void {
    const formValue = this.productForm.value;
    
    const request: CreateProductRequest = {
      nombre: formValue.nombre,
      codigoBarras: formValue.codigoBarras,
      stockMinimo: formValue.stockMinimo,
      stockInicial: formValue.stockInicial,
      costo: formValue.costo,
      precioVenta: formValue.precioVenta,
      porcentajeIva: formValue.porcentajeIva ?? 0,
      estado: 'ACTIVO',
      categoriaId: formValue.categoriaId,
      numeroLote: formValue.hasLote ? formValue.numeroLote : undefined
    };

    this.inventoryService.createProduct(request).subscribe({
      next: () => {
        this.showConfirmation.set(false);
        this.router.navigate(['/inventario']);
      },
      error: (err) => {
        console.error('Error creating product', err);
        // Here we could add a toast or error message in the UI
        this.showConfirmation.set(false);
      }
    });
  }

  cancelCreation(): void {
    this.showConfirmation.set(false);
  }

  onCancel(): void {
    this.router.navigate(['/inventario']);
  }
}
