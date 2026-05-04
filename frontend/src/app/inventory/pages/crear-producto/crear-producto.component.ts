import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';

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

  productForm!: FormGroup;
  categories = signal<{id: number, nombre: string}[]>([]);
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
      nombre: ['', [Validators.required]],
      descripcion: [''],
      codigoBarras: ['', [Validators.required]],
      stockInicial: [0, [Validators.required, Validators.min(0)]],
      stockMinimo: [0, [Validators.required, Validators.min(0)]],
      costo: [0, [Validators.required, Validators.min(0)]],
      precioVenta: [0, [Validators.required, Validators.min(0)]],
      hasLote: [false],
      numeroLote: [''],
      categoriaId: [null, [Validators.required]]
    });
  }

  private loadCategories(): void {
    // Static data as requested
    this.categories.set([
      { id: 1, nombre: 'Analgésicos' },
      { id: 2, nombre: 'Antibióticos' },
      { id: 3, nombre: 'Vitaminas' },
      { id: 4, nombre: 'Cuidado Personal' },
      { id: 5, nombre: 'Maternidad' }
    ]);
  }

  onSave(): void {
    if (this.productForm.invalid) return;
    this.showConfirmation.set(true);
  }

  confirmCreation(): void {
    console.log('Producto creado (simulado):', this.productForm.value);
    this.showConfirmation.set(false);
    this.router.navigate(['/inventario']);
  }

  cancelCreation(): void {
    this.showConfirmation.set(false);
  }

  onCancel(): void {
    this.router.navigate(['/inventario']);
  }
}
