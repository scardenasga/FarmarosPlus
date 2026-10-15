import { Component, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormsModule, FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { PurchasingService } from '../../services/purchasing.service';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { Supplier } from '../../../supplier/models/supplier.model';
import { Product } from '../../../inventory/models/product.model';

interface ItemOrden {
  productoId: number;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
}

@Component({
  selector: 'app-compra-formulario-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ConfirmationDialogComponent],
  templateUrl: './compra-formulario-panel.component.html',
  styleUrl: './compra-formulario-panel.component.css'
})
export class CompraFormularioPanelComponent implements OnChanges {
  private fb = inject(FormBuilder);
  private purchasingService = inject(PurchasingService);
  private supplierService = inject(SupplierService);
  private inventoryService = inject(InventoryService);
  private notificacion = inject(NotificacionService);

  modo = input.required<'crear' | 'editar'>();
  ordenId = input<number | null>(null);
  abierto = input<boolean>(false);

  cerrado = output<void>();
  guardado = output<void>();

  proveedores = signal<Supplier[]>([]);
  orden = signal<any>(null);
  cargando = signal(true);
  guardando = signal(false);

  items = signal<ItemOrden[]>([]);
  busquedaProducto = signal('');
  resultadosBusqueda = signal<Product[]>([]);

  mostrarConfirmacion = signal(false);

  /** La fecha esperada no puede ser anterior a hoy. */
  readonly validadorFechaNoPasada = (control: AbstractControl): { [key: string]: any } | null => {
    const v = String(control.value ?? '');
    if (!v) return null;
    const hoy = new Date();
    const hoyIso = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;
    return v < hoyIso ? { fechaPasada: true } : null;
  }
  productForm = this.fb.group({
    proveedorId: this.fb.control(null as number | null, [Validators.required]),
    fechaEsperada: this.fb.control('' as string, [this.validadorFechaNoPasada]),
    observaciones: ['']
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['abierto'] && this.abierto()) {
      this.cargarProveedores();
      this.reiniciar();
    }
  }

  cargarProveedores(): void {
    this.supplierService.listActive().subscribe({
      next: (p) => this.proveedores.set(p ?? []),
      error: () => this.notificacion.error('No se pudieron cargar los proveedores.')
    });
  }

  reiniciar(): void {
    this.items.set([]);
    this.busquedaProducto.set('');
    this.resultadosBusqueda.set([]);
    this.mostrarConfirmacion.set(false);
    this.guardando.set(false);

    if (this.modo() === 'editar' && this.ordenId() != null) {
      this.cargando.set(true);
      this.purchasingService.obtenerOrden(this.ordenId()!).subscribe({
        next: (orden) => {
          this.orden.set(orden);
          this.productForm.patchValue({
            proveedorId: orden.proveedor?.idProveedor ?? null,
            fechaEsperada: orden.fechaEsperada ? String(orden.fechaEsperada).slice(0, 10) : '',
            observaciones: orden.observaciones ?? ''
          });
          this.items.set((orden.detalles ?? []).map(d => ({
            productoId: d.productoId!,
            nombre: d.nombreProducto || 'Producto',
            cantidad: d.cantidadPedida,
            precioUnitario: d.precioUnitarioPactado
          })));
          this.cargando.set(false);
        },
        error: () => {
          this.notificacion.error('No se pudo cargar la orden.');
          this.cargando.set(false);
          this.cerrado.emit();
        }
      });
    } else {
      this.orden.set(null);
      this.cargando.set(false);
    }
  }

  getControl(name: string): FormControl {
    return this.productForm.get(name) as FormControl;
  }

  /* ---------- Items (solo creacion) ---------- */

  buscarProducto(event: Event): void {
    const termino = String((event.target as HTMLInputElement).value ?? '').trim();
    this.busquedaProducto.set(termino);
    if (termino.length < 3) {
      this.resultadosBusqueda.set([]);
      return;
    }
    this.inventoryService.searchProducts(termino).subscribe({
      next: (lista) => this.resultadosBusqueda.set(lista.slice(0, 5)),
      error: () => this.resultadosBusqueda.set([])
    });
  }

  agregarProducto(p: Product): void {
    if (this.items().some(i => i.productoId === p.id)) {
      this.notificacion.advertencia('Ese producto ya esta en la orden.');
      return;
    }
    this.items.update(prev => [...prev, {
      productoId: p.id,
      nombre: p.nombre,
      cantidad: 1,
      precioUnitario: p.costo
    }]);
    this.busquedaProducto.set('');
    this.resultadosBusqueda.set([]);
  }

  quitarItem(index: number): void {
    this.items.update(prev => prev.filter((_, i) => i !== index));
  }

  totalOrden(): number {
    return this.items().reduce((s, i) => s + i.cantidad * i.precioUnitario, 0);
  }

  cerrar(): void {
    if (!this.guardando()) this.cerrado.emit();
  }

  solicitarGuardar(): void {
    if (!this.getControl('proveedorId').value) {
      this.notificacion.advertencia('Selecciona un proveedor.');
      return;
    }
    if (!this.esEditar() && !this.items().length) {
      this.notificacion.advertencia('Agrega al menos un producto a la orden.');
      return;
    }
    this.mostrarConfirmacion.set(true);
  }

  cancelarConfirmacion(): void {
    if (!this.guardando()) this.mostrarConfirmacion.set(false);
  }

  confirmarGuardado(): void {
    if (this.esEditar()) this.guardarEdicion();
    else this.guardarCreacion();
  }

  esEditar(): boolean {
    return this.modo() === 'editar';
  }

  private guardarCreacion(): void {
    const v = this.productForm.value;
    this.guardando.set(true);
    this.purchasingService.registrarOrden({
      proveedorId: Number(v.proveedorId),
      fechaEsperada: v.fechaEsperada ? v.fechaEsperada + 'T00:00:00' : undefined,
      items: this.items().map(i => ({
        productoId: i.productoId,
        cantidad: i.cantidad,
        precioUnitario: i.precioUnitario
      })),
      observaciones: v.observaciones || undefined
    }).subscribe({
      next: () => {
        this.notificacion.exito('Orden de compra creada correctamente');
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
        this.guardado.emit();
      },
      error: (err) => this.fallo(err, 'No se pudo crear la orden.')
    });
  }

  private guardarEdicion(): void {
    const orden = this.orden();
    const v = this.productForm.value;
    if (!orden) return;

    this.guardando.set(true);
    this.purchasingService.actualizarOrden(orden.idOrden, {
      proveedorId: Number(v.proveedorId),
      fechaEsperada: v.fechaEsperada ? v.fechaEsperada + 'T00:00:00' : undefined,
      items: this.items().map(i => ({
        productoId: i.productoId,
        cantidad: i.cantidad,
        precioUnitario: i.precioUnitario
      })),
      observaciones: v.observaciones || undefined
    }).subscribe({
      next: () => {
        this.notificacion.exito(`Orden OC-${orden.idOrden} actualizada`);
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
        this.guardado.emit();
      },
      error: (err) => this.fallo(err, 'No se pudo actualizar la orden.')
    });
  }

  private fallo(err: any, fallback: string): void {
    this.notificacion.error(err?.error?.message || fallback);
    this.guardando.set(false);
    this.mostrarConfirmacion.set(false);
  }
}
