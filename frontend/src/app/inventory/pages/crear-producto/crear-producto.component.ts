import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { InventoryService } from '../../services/inventory.service';
import { CategoriaService } from '../../services/categoria.service';
import {Categoria, CrearProductoRequest} from '../../models/product.model';

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
      categoriaId: [null],
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: [''],
      codigoBarras: ['', [Validators.required]],
      stockMinimo: [0, [Validators.required, Validators.min(0)]],
      stockInicial: [0, [Validators.required, Validators.min(1)]],
      costo: [0, [Validators.required, Validators.min(0)]],
      precioVenta: [0, [Validators.required, Validators.min(0)]],
      porcentajeIva: [0, [Validators.required, Validators.min(0)]],
      requierePrescripcion: [false],
      fechaVencimiento: ['', [Validators.required]],
      numeroLote: ['', [Validators.required]]
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

  onSave(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }
    this.showConfirmation.set(true);
  }

  confirmCreation(): void {
    const formValue = this.productForm.value;

    const request: CrearProductoRequest = {
      nombre: formValue.nombre,
      descripcion: formValue.descripcion || undefined,
      codigoBarras: formValue.codigoBarras,
      categoriaId: formValue.categoriaId ? Number(formValue.categoriaId) : undefined,
      stockMinimo: formValue.stockMinimo,
      stockInicial: formValue.stockInicial,
      costo: formValue.costo,
      precioVenta: formValue.precioVenta,
      porcentajeIva: formValue.porcentajeIva,
      requierePrescripcion: formValue.requierePrescripcion,
      fechaVencimiento: formValue.fechaVencimiento,
      numeroLote: formValue.numeroLote
    };

    this.inventoryService.createProduct(request).subscribe({
      next: () => {
        this.showConfirmation.set(false);
        this.router.navigate(['/inventario']);
      },
      error: (err) => {
        console.error('Error creating product', err);
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
