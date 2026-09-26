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
import { ConfiguracionService } from '../../../configuracion/services/configuracion.service';
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
  private configService = inject(ConfiguracionService);

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

  gananciaMinima = signal<number>(30);
  precioSugerido = signal<number | null>(null);

  /* ---------- Imagen (persistida en backend: SQLite TEXT + filesystem) ---------- */
  imagenPreview = signal<string | null>(null);
  imagenNombre = signal<string>('');
  archivoImagen = signal<File | null>(null);
  eliminarImagenExistente = signal<boolean>(false);

  seleccionarImagen(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;

    if (!archivo.type.startsWith('image/')) {
      this.notificacion.advertencia('Selecciona un archivo de imagen (PNG, JPG, etc.).');
      return;
    }
    if (archivo.size > 5 * 1024 * 1024) {
      this.notificacion.advertencia('La imagen no puede superar 5MB.');
      return;
    }

    this.archivoImagen.set(archivo);
    this.eliminarImagenExistente.set(false);
    const lector = new FileReader();
    lector.onload = () => {
      this.imagenPreview.set(String(lector.result));
      this.imagenNombre.set(archivo.name);
    };
    lector.readAsDataURL(archivo);

    input.value = '';
  }

  eliminarImagen(): void {
    this.imagenPreview.set(null);
    this.imagenNombre.set('');
    this.archivoImagen.set(null);
    // si estaba editando y tenía imagen en servidor, marcar para borrar
    if (this.esEditar() && this.detalle()?.imagenUrl) {
      this.eliminarImagenExistente.set(true);
    }
  }

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
    this.imagenPreview.set(null);
    this.imagenNombre.set('');
    this.archivoImagen.set(null);
    this.eliminarImagenExistente.set(false);
    this.cargarGanancia();

    if (this.esEditar()) {
      const id = this.productoId();
      if (id != null) {
        this.cargarYConstruir(id);
        return;
      }
    }
    this.construirFormularioCrear();
  }

  private cargarGanancia(): void {
    this.configService.obtenerGanancia().subscribe({
      next: (c) => this.gananciaMinima.set(c.porcentajeMinimo ?? 30),
      error: () => {}
    });
  }

  calcularSugerido(): void {
    const costo = Number(this.getControl('costo')?.value) || 0;
    const iva = Number(this.getControl('porcentajeIva')?.value) || 0;
    if (costo <= 0) { this.precioSugerido.set(null); return; }
    const base = costo * (1 + this.gananciaMinima() / 100);
    const minimoIva = costo * (1 + iva / 100);
    const sugerido = base <= minimoIva ? minimoIva + 1 : base;
    this.precioSugerido.set(Math.round(sugerido * 100) / 100);
  }

  aplicarPrecioSugerido(): void {
    const s = this.precioSugerido();
    if (s != null) {
      this.getControl('precioVenta').setValue(s);
      this.getControl('precioVenta').markAsTouched();
    }
  }

  private cargarYConstruir(id: number): void {
    this.cargandoDetalle.set(true);
    this.inventoryService.getProductDetail(id).subscribe({
      next: (detalle) => {
        this.detalle.set(detalle);
        this.cargandoDetalle.set(false);
        if (detalle.imagenUrl) {
          this.imagenPreview.set(`/api/productos/${detalle.id}/imagen`);
          this.imagenNombre.set(detalle.imagenUrl);
        }
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
      precioVenta: [null, [Validators.min(0)]],
      porcentajeIva: [0, [Validators.required, Validators.min(0)]],
      requierePrescripcion: [false],
      tieneLote: [false],
      numeroLote: [''],
      fechaVencimiento: [''],
      unidadVenta: ['UNIDAD', [Validators.required]],
      unidadesPorPresentacion: [null],
      precioPresentacion: [null]
    }, { validators: this.validadorPresentacion });
    // recalcular sugerido al cambiar costo/iva
    setTimeout(() => {
      this.getControl('costo')?.valueChanges.subscribe(() => this.calcularSugerido());
      this.getControl('porcentajeIva')?.valueChanges.subscribe(() => this.calcularSugerido());
      this.calcularSugerido();
    });
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
      requierePrescripcion: [!!p.requierePrescripcion],
      unidadVenta: [p.unidadVenta || 'UNIDAD', [Validators.required]],
      unidadesPorPresentacion: [p.unidadesPorPresentacion ?? null],
      precioPresentacion: [p.precioPresentacion ?? null]
    }, { validators: this.validadorPresentacion });
  }

  private validadorPresentacion(group: FormGroup): { [key: string]: any } | null {
    const uv = group.get('unidadVenta')?.value;
    const up = group.get('unidadesPorPresentacion')?.value;
    const pp = group.get('precioPresentacion')?.value;
    const needs = uv === 'PRESENTACION' || uv === 'AMBAS';
    if (needs) {
      if (up == null || up === '' || Number(up) <= 1) return { presentacionInvalida: true };
      if (pp == null || pp === '' || Number(pp) <= 0) return { presentacionInvalida: true };
    }
    const costo = group.get('costo')?.value || 0;
    const precioVenta = group.get('precioVenta')?.value;
    const iva = group.get('porcentajeIva')?.value || 0;
    if (precioVenta != null && precioVenta !== '') {
      const minPrecio = costo * (1 + iva / 100);
      if (Number(precioVenta) <= minPrecio && Number(precioVenta) > 0) {
        return { priceTooLow: true };
      }
    }
    return null;
  }

  private validadorPrecio(group: FormGroup): { [key: string]: any } | null {
    const costo = group.get('costo')?.value || 0;
    const precioVenta = group.get('precioVenta')?.value;
    const iva = group.get('porcentajeIva')?.value || 0;

    if (precioVenta == null || precioVenta === '' ) return null;
    const minPrecio = costo * (1 + iva / 100);
    if (Number(precioVenta) <= minPrecio && Number(precioVenta) > 0) {
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

    const precio = v.precioVenta != null && v.precioVenta !== '' ? Number(v.precioVenta) : undefined;
    const uv = v.unidadVenta || 'UNIDAD';
    const needs = uv === 'PRESENTACION' || uv === 'AMBAS';

    const request: CrearProductoRequest = {
      nombre: v.nombre,
      descripcion: v.descripcion || undefined,
      codigoBarras: String(v.codigoBarras).trim(),
      categoriaId: v.categoriaId ? Number(v.categoriaId) : undefined,
      stockMinimo: Number(v.stockMinimo),
      stockInicial: Number(v.stockInicial),
      costo: Number(v.costo),
      precioVenta: precio,
      porcentajeIva: Number(v.porcentajeIva) || 0,
      requierePrescripcion: !!v.requierePrescripcion,
      fechaVencimiento: v.fechaVencimiento || undefined,
      numeroLote: v.tieneLote && v.numeroLote ? v.numeroLote : undefined,
      unidadVenta: uv,
      unidadesPorPresentacion: needs && v.unidadesPorPresentacion ? Number(v.unidadesPorPresentacion) : undefined,
      precioPresentacion: needs && v.precioPresentacion ? Number(v.precioPresentacion) : undefined
    };

    this.guardando.set(true);
    this.inventoryService.createProduct(request).subscribe({
      next: (creado: any) => {
        const id = creado?.id as number | undefined;
        const archivo = this.archivoImagen();
        if (id && archivo) {
          this.inventoryService.subirImagen(id, archivo).subscribe({
            next: () => {
              this.notificacion.exito(`Producto "${request.nombre}" creado con imagen`);
              this.finalizar();
            },
            error: (err) => {
              this.notificacion.advertencia(`Producto creado pero la imagen no se pudo subir: ${err.error?.message || err.message}`);
              this.finalizar();
            }
          });
        } else {
          this.notificacion.exito(`Producto "${request.nombre}" creado correctamente`);
          this.finalizar();
        }
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
    const uv = v.unidadVenta || 'UNIDAD';
    const needs = uv === 'PRESENTACION' || uv === 'AMBAS';
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
      requierePrescripcion: !!v.requierePrescripcion,
      unidadVenta: uv,
      unidadesPorPresentacion: needs && v.unidadesPorPresentacion ? Number(v.unidadesPorPresentacion) : null,
      precioPresentacion: needs && v.precioPresentacion ? Number(v.precioPresentacion) : null
    };

    this.guardando.set(true);
    this.inventoryService.updateProduct(id, request).subscribe({
      next: () => {
        const archivo = this.archivoImagen();
        const debeEliminar = this.eliminarImagenExistente();
        if (archivo) {
          this.inventoryService.subirImagen(id, archivo).subscribe({
            next: () => {
              this.notificacion.exito(`Producto "${request.nombre}" actualizado con imagen`);
              this.finalizar();
            },
            error: (err) => {
              this.notificacion.error(err.error?.message || 'No se pudo subir la imagen.');
              this.guardando.set(false);
              this.mostrarConfirmacion.set(false);
            }
          });
        } else if (debeEliminar) {
          this.inventoryService.eliminarImagen(id).subscribe({
            next: () => {
              this.notificacion.exito(`Imagen eliminada y producto "${request.nombre}" actualizado`);
              this.finalizar();
            },
            error: () => {
              this.notificacion.exito(`Producto "${request.nombre}" actualizado correctamente`);
              this.finalizar();
            }
          });
        } else {
          this.notificacion.exito(`Producto "${request.nombre}" actualizado correctamente`);
          this.finalizar();
        }
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
