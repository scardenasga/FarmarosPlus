import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl, FormArray } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
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
  imports: [CommonModule, ReactiveFormsModule, TopBarComponent, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './ingreso-stock.component.html',
  styleUrl: './ingreso-stock.component.css'
})
export class IngresoStockComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private navService = inject(NavigationService);
  private inventoryService = inject(InventoryService);

  stockForm!: FormGroup;
  foundProduct = signal<Product | null>(null);
  showConfirmation = signal<boolean>(false);
  isSearching = signal<boolean>(false);
  registrando = signal<boolean>(false);
  errorMsg = signal<string>('');
  lotesPendientes = signal<number>(0);

  ngOnInit(): void {
    this.navService.hideNav();
    this.initForm();
    this.setupBarcodeSearch();
    const barcode = this.route.snapshot.queryParamMap.get('barcode');
    if (barcode) {
      this.stockForm.get('codigoBarras')?.setValue(barcode);
}
    
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  getControl(name: string): FormControl {
    return this.stockForm.get(name) as FormControl;
  }

  get lotes(): FormArray {
    return this.stockForm.get('lotes') as FormArray;
  }

  getLoteControl(index: number, name: string): FormControl {
    return (this.lotes.at(index) as FormGroup).get(name) as FormControl;
  }

  private initForm(): void {
    this.stockForm = this.fb.group({
      codigoBarras: ['', [Validators.required]],
      nuevoCosto: [null, [Validators.min(0)]],
      nuevoPrecioVenta: [null, [Validators.min(0)]],
      lotes: this.fb.array([this.crearLoteGroup()])
    });
  }

  private crearLoteGroup(): FormGroup {
    return this.fb.group({
      cantidad: [null, [Validators.required, Validators.min(1)]],
      fechaVencimiento: ['', [Validators.required]],
      numeroLote: ['']
    });
  }

  agregarLote(): void {
    this.lotes.push(this.crearLoteGroup());
  }

  eliminarLote(index: number): void {
    if (this.lotes.length > 1) {
      this.lotes.removeAt(index);
    }
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
    if (this.stockForm.invalid || !this.foundProduct()) {
      this.stockForm.markAllAsTouched();
      return;
    }
    this.lotesPendientes.set(this.lotes.length);
    this.showConfirmation.set(true);
  }

  confirmIngreso(): void {
    this.showConfirmation.set(false);
    this.registrando.set(true);
    this.errorMsg.set('');

    const formValue = this.stockForm.value;
    const codigoBarras = formValue.codigoBarras;
    const lotesArray = formValue.lotes;

    // Registrar lotes secuencialmente
    const registrarLote = (index: number) => {
      if (index >= lotesArray.length) {
        this.registrando.set(false);
        this.router.navigate(['/inventario']);
        return;
      }

      const lote = lotesArray[index];
      const request: IngresoStockRequest = {
        cantidad: lote.cantidad,
        fechaVencimiento: lote.fechaVencimiento,
        numeroLote: lote.numeroLote || undefined,
        nuevoCosto: index === 0 && formValue.nuevoCosto ? formValue.nuevoCosto : undefined,
        nuevoPrecioVenta: index === 0 && formValue.nuevoPrecioVenta ? formValue.nuevoPrecioVenta : undefined
      };

      this.inventoryService.registrarIngreso(codigoBarras, request).subscribe({
        next: () => registrarLote(index + 1),
        error: (err) => {
          this.registrando.set(false);
          this.errorMsg.set(`Error registrando lote ${index + 1}: ${err?.error?.message || 'Error desconocido'}`);
        }
      });
    };

    registrarLote(0);
  }

  cancelConfirmation(): void {
    this.showConfirmation.set(false);
  }

  onCancel(): void {
    this.router.navigate(['/inventario']);
  }
}