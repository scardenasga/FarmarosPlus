import { Component, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { InventoryService } from '../../services/inventory.service';
import {
  ActualizarProductoRequest,
  Categoria,
  CrearProductoRequest,
  ProductoDetalleResponse
} from '../../models/product.model';

@Component({
  selector: 'app-producto-formulario-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './producto-formulario-panel.component.html',
  styleUrl: './producto-formulario-panel.component.css'
})
export class ProductoFormularioPanelComponent implements OnChanges {
  private fb = inject(FormBuilder);
  private inventoryService = inject(InventoryService);
  private notificacion = inject(NotificacionService);

  /** 'crear' o 'editar' */
  modo = input.required<'crear' | 'editar'>();
  /** Id del producto en modo edición. */
  productoId = input<number | null>(null);
  abierto = input<boolean>(false);
  categorias = input<Categoria[]>([]);

  cerrado = output<void>();
  guardado = output<void>();

  productForm!: FormGroup;
  detalle = signal<ProductoDetalleResponse | null>(null);
  cargandoDetalle = signal<boolean>(false);
  guardando = signal<boolean>(false);
  mostrarConfirmacion = signal<boolean>(false);

  estados = [
    { id: 'ACTIVO', nombre: 'ACTIVO' },
    { id: 'INACTIVO', nombre: 'INACTIVO' },
    { id: 'DESCONTINUADO', nombre: 'DESCONTINUADO' }
  ];

  esEditar(): boolean {
    return this.modo() === 'editar';
  }

  getControl(name: string): FormControl {
    return this.productForm.get(name) as FormControl;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const cambioAbierto = changes['abierto'];
    if (!cambioAbierto || !this.abierto()) return;

    this.mostrarConfirmacion.set(false);
    this.detalle.set(null);

    if (this.esEditar()) {
      const id = this.productoId();
      if (id != null) {
        this.cargarYConstruir(id);
        return;
      }
    }
    this.construirFormularioCrear();
  }

  private cargarYConstruir(id: number): void {
    this.cargandoDetalle.set(true);
    this.inventoryService.getProductDetail(id).subscribe({
      next: (detalle) => {
        this.detalle.set(detalle);
        this.cargandoDetalle.set(false);
        this.construirFormularioEditar(detalle);
      },
      error: () => {
        this.notificacion.error('No se pudo cargar el producto a editar.');
        this.cargandoDetalle.set(false);
        this.cerrado.emit();
      }
    });
  }

  private construirFormularioCrear(): void {
    this.productForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: [''],
      categoriaId: [null],
      codigoBarras: ['', [Validators.required]],
      stockInicial: [0, [Validators.required, Validators.min(0)]],
      stockMinimo: [0, [Validators.required, Validators.min(0)]],
      costo: [0, [Validators.required, Validators.min(0)]],
      precioVenta: [0, [Validators.required, Validators.min(0)]],
      porcentajeIva: [0, [Validators.required, Validators.min(0)]],
      requierePrescripcion: [false],
      tieneLote: [false],
      numeroLote: [''],
      fechaVencimiento: ['']
    }, { validators: this.validadorPrecio });
  }

  private construirFormularioEditar(p: ProductoDetalleResponse): void {
    this.productForm = this.fb.group({
      nombre: [p.nombre, [Validators.required, Validators.minLength(3)]],
      descripcion: [p.descripcion ?? ''],
      categoriaId: [p.categoria?.id, [Validators.required]],
      estado: [p.estado, [Validators.required]],
      stockActual: [p.stockActual, [Validators.required, Validators.min(0)]],
      stockMinimo: [p.stockMinimo, [Validators.required, Validators.min(0)]],
      costo: [p.costo, [Validators.required, Validators.min(0)]],
      precioVenta: [p.precioVenta, [Validators.required, Validators.min(0)]],
      porcentajeIva: [p.porcentajeIva, [Validators.required, Validators.min(0)]],
      requierePrescripcion: [!!p.requierePrescripcion]
    }, { validators: this.validadorPrecio });
  }

  private validadorPrecio(group: FormGroup): { [key: string]: any } | null {
    const costo = group.get('costo')?.value || 0;
    const precioVenta = group.get('precioVenta')?.value || 0;
    const iva = group.get('porcentajeIva')?.value || 0;

    const minPrecio = costo * (1 + iva / 100);
    if (precioVenta <= minPrecio && precioVenta > 0) {
      return { priceTooLow: true };
    }
    return null;
  }

  cerrar(): void {
    if (!this.guardando()) this.cerrado.emit();
  }

  solicitarGuardar(): void {
    if (!this.productForm || this.productForm.invalid) {
      this.productForm?.markAllAsTouched();
      this.notificacion.advertencia('Completa los campos obligatorios antes de guardar.');
      return;
    }

    // Un lote necesita fecha de vencimiento para ser útil.
    if (!this.esEditar()) {
      const tieneLote = !!this.getControl('tieneLote')?.value;
      const fechaVence = this.getControl('fechaVencimiento')?.value;
      if (tieneLote && !fechaVence) {
        this.notificacion.advertencia('Si usas número de lote, indica la fecha de vencimiento.');
        return;
      }
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

  private guardarCreacion(): void {
    const v = this.productForm.value;

    const request: CrearProductoRequest = {
      nombre: v.nombre,
      descripcion: v.descripcion || undefined,
      codigoBarras: String(v.codigoBarras).trim(),
      categoriaId: v.categoriaId ? Number(v.categoriaId) : undefined,
      stockMinimo: Number(v.stockMinimo),
      stockInicial: Number(v.stockInicial),
      costo: Number(v.costo),
      precioVenta: Number(v.precioVenta),
      porcentajeIva: Number(v.porcentajeIva) || 0,
      requierePrescripcion: !!v.requierePrescripcion,
      fechaVencimiento: v.fechaVencimiento || undefined,
      numeroLote: v.tieneLote && v.numeroLote ? v.numeroLote : undefined
    };

    this.inventoryService.createProduct(request).subscribe({
      next: () => {
        this.notificacion.exito(`Producto "${request.nombre}" creado correctamente`);
        this.finalizar();
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo crear el producto.');
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
      }
    });
  }

  private guardarEdicion(): void {
    const id = this.detalle()?.id;
    if (!id) return;

    const v = this.productForm.value;
    const request: ActualizarProductoRequest = {
      nombre: v.nombre,
      descripcion: v.descripcion || undefined,
      categoriaId: v.categoriaId ? Number(v.categoriaId) : undefined,
      estado: v.estado,
      stockActual: Number(v.stockActual),
      stockMinimo: Number(v.stockMinimo),
      costo: Number(v.costo),
      porcentajeIva: Number(v.porcentajeIva) || 0,
      precioVenta: Number(v.precioVenta),
      requierePrescripcion: !!v.requierePrescripcion
    };

    this.inventoryService.updateProduct(id, request).subscribe({
      next: () => {
        this.notificacion.exito(`Producto "${request.nombre}" actualizado correctamente`);
        this.finalizar();
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo actualizar el producto.');
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
      }
    });
  }

  private finalizar(): void {
    this.guardando.set(false);
    this.mostrarConfirmacion.set(false);
    this.guardado.emit();
  }
}
