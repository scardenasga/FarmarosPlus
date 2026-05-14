import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { InventoryService } from '../../services/inventory.service';
import { Product, IngresoStockRequest } from '../../models/product.model';
import { debounceTime, distinctUntilChanged, switchMap, catchError, of } from 'rxjs';

@Component({
  selector: 'app-ingreso-stock',
  standalone: true,
  imports: [ReactiveFormsModule, TopBarComponent, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './ingreso-stock.component.html',
  styleUrl: './ingreso-stock.component.css'
})
export class IngresoStockComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private inventoryService = inject(InventoryService);

  stockForm!: FormGroup;
  foundProduct = signal<Product | null>(null);
  showConfirmation = signal<boolean>(false);
  isSearching = signal<boolean>(false);

  ngOnInit(): void {
    this.navService.hideNav();
    this.initForm();
    this.setupBarcodeSearch();
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  getControl(name: string): FormControl {
    return this.stockForm.get(name) as FormControl;
  }

  private initForm(): void {
    this.stockForm = this.fb.group({
      codigoBarras: ['', [Validators.required]],
      cantidad: [null, [Validators.required, Validators.min(1)]],
      fechaVencimiento: ['', [Validators.required]],
      numeroLote: [''],
      nuevoCosto: [null, [Validators.min(0)]],
      nuevoPrecioVenta: [null, [Validators.min(0)]]
    });
  }

  private setupBarcodeSearch(): void {
    this.stockForm.get('codigoBarras')?.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(code => {
        if (!code || code.length < 3) {
          this.foundProduct.set(null);
          return of([]);
        }
        this.isSearching.set(true);
        return this.inventoryService.searchProducts(undefined, code).pipe(
          catchError(() => {
            this.isSearching.set(false);
            return of([]);
          })
        );
      })
    ).subscribe(products => {
      this.isSearching.set(false);
      if (products && products.length > 0) {
        this.foundProduct.set(products[0]);
      } else {
        this.foundProduct.set(null);
      }
    });
  }

  onSave(): void {
    if (this.stockForm.invalid) {
      this.stockForm.markAllAsTouched();
      return;
    }
    this.showConfirmation.set(true);
  }

  confirmIngreso(): void {
    const formValue = this.stockForm.value;
    const codigoBarras = formValue.codigoBarras;

    const request: IngresoStockRequest = {
      cantidad: formValue.cantidad,
      fechaVencimiento: formValue.fechaVencimiento,
      numeroLote: formValue.numeroLote || undefined,
      nuevoCosto: formValue.nuevoCosto !== null ? formValue.nuevoCosto : undefined,
      nuevoPrecioVenta: formValue.nuevoPrecioVenta !== null ? formValue.nuevoPrecioVenta : undefined
    };

    this.inventoryService.registrarIngreso(codigoBarras, request).subscribe({
      next: () => {
        this.showConfirmation.set(false);
        this.router.navigate(['/inventario']);
      },
      error: (err) => {
        console.error('Error registrando ingreso', err);
        this.showConfirmation.set(false);
      }
    });
  }

  cancelConfirmation(): void {
    this.showConfirmation.set(false);
  }

  onCancel(): void {
    this.router.navigate(['/inventario']);
  }
}
