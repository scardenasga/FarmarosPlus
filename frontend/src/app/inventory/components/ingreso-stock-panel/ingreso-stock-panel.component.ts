import { Component, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { InventoryService } from '../../services/inventory.service';
import { IngresoStockRequest, Product } from '../../models/product.model';

@Component({
  selector: 'app-ingreso-stock-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './ingreso-stock-panel.component.html',
  styleUrl: './ingreso-stock-panel.component.css'
})
export class IngresoStockPanelComponent {
  private fb = inject(FormBuilder);
  private inventoryService = inject(InventoryService);
  private notificacion = inject(NotificacionService);

  cerrado = output<void>();
  guardado = output<void>();

  /** Controla la visibilidad del panel desde el inventario. */
  abierto = input<boolean>(false);

  stockForm = this.fb.group({
    codigoBarras: ['', [Validators.required]],
    cantidad: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaVencimiento: ['', [Validators.required]],
    numeroLote: [''],
    nuevoCosto: [null as number | null, [Validators.min(0)]],
    nuevoPrecioVenta: [null as number | null, [Validators.min(0)]]
  });

  foundProduct = signal<Product | null>(null);
  isSearching = signal<boolean>(false);
  showConfirmation = signal<boolean>(false);
  registrando = signal<boolean>(false);

  constructor() {
    this.setupBarcodeSearch();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['abierto'] && this.abierto()) {
      // Cada apertura arranca con el formulario limpio.
      this.reiniciar();
      this.showConfirmation.set(false);
      this.registrando.set(false);
    }
  }

  getControl(name: string): FormControl {
    return this.stockForm.get(name) as FormControl;
  }

  /** Búsqueda automática del producto mientras se escribe el código. */
  private setupBarcodeSearch(): void {
    this.getControl('codigoBarras').valueChanges.pipe(
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
      const encontrado = products && products.length > 0 ? products[0] : null;
      this.foundProduct.set(encontrado);

      const estadoActual = String(this.getControl('codigoBarras').value ?? '');
      if (!encontrado && estadoActual.length >= 3) {
        // El mensaje de "no encontrado" lo maneja el template con el estado.
      }
    });
  }

  codigoBuscadoLargo(): boolean {
    return String(this.getControl('codigoBarras').value ?? '').length >= 3;
  }

  cerrar(): void {
    if (!this.registrando()) this.cerrado.emit();
  }

  reiniciar(): void {
    this.stockForm.reset({
      codigoBarras: '',
      cantidad: null,
      fechaVencimiento: '',
      numeroLote: '',
      nuevoCosto: null,
      nuevoPrecioVenta: null
    });
    this.foundProduct.set(null);
  }

  solicitarGuardar(): void {
    if (!this.foundProduct()) {
      this.notificacion.advertencia('Primero busca un producto por su código de barras.');
      return;
    }
    if (this.stockForm.invalid) {
      this.stockForm.markAllAsTouched();
      this.notificacion.advertencia('Completa los campos obligatorios antes de guardar.');
      return;
    }
    this.showConfirmation.set(true);
  }

  cancelConfirmation(): void {
    if (!this.registrando()) this.showConfirmation.set(false);
  }

  confirmarIngreso(): void {
    const producto = this.foundProduct();
    if (!producto || this.registrando()) return;

    this.registrando.set(true);
    const v = this.stockForm.value;

    const request: IngresoStockRequest = {
      cantidad: Number(v.cantidad),
      fechaVencimiento: String(v.fechaVencimiento ?? ''),
      numeroLote: v.numeroLote || undefined,
      nuevoCosto: v.nuevoCosto != null ? Number(v.nuevoCosto) : undefined,
      nuevoPrecioVenta: v.nuevoPrecioVenta != null ? Number(v.nuevoPrecioVenta) : undefined
    };

    this.inventoryService.registrarIngreso(producto.codigoBarras, request).subscribe({
      next: () => {
        this.notificacion.exito(
          `Ingreso registrado: +${request.cantidad} uds de ${producto.nombre}`
        );
        this.showConfirmation.set(false);
        this.registrando.set(false);

        // Prepara el panel para otro ingreso consecutivo.
        this.reiniciar();
        this.guardado.emit();
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo registrar el ingreso.');
        this.registrando.set(false);
        this.showConfirmation.set(false);
      }
    });
  }
}
