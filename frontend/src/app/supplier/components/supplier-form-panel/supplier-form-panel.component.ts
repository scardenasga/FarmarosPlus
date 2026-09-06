import { Component, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { SupplierService } from '../../services/supplier.service';
import { CreateSupplierRequest, Supplier, UpdateSupplierRequest } from '../../models/supplier.model';

@Component({
  selector: 'app-supplier-form-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './supplier-form-panel.component.html',
  styleUrl: './supplier-form-panel.component.css'
})
export class SupplierFormPanelComponent implements OnChanges {
  private fb = inject(FormBuilder);
  private supplierService = inject(SupplierService);
  private notificacion = inject(NotificacionService);

  modo = input.required<'crear' | 'editar'>();
  supplierId = input<number | null>(null);
  abierto = input<boolean>(false);

  cerrado = output<void>();
  guardado = output<void>();

  supplierForm!: FormGroup;
  detalle = signal<Supplier | null>(null);
  cargandoDetalle = signal<boolean>(false);
  guardando = signal<boolean>(false);
  mostrarConfirmacion = signal<boolean>(false);

  // Imagen (SQLite TEXT + filesystem uploads/proveedores)
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
    if (this.detalle()?.imagenUrl) {
      this.eliminarImagenExistente.set(true);
    }
  }

  condicionesPago = [
    { id: 'Contado', nombre: 'Contado' },
    { id: 'Neto 15', nombre: 'Neto 15 días' },
    { id: 'Neto 30', nombre: 'Neto 30 días' },
    { id: 'Neto 45', nombre: 'Neto 45 días' },
    { id: 'Neto 60', nombre: 'Neto 60 días' },
    { id: 'Contra Entrega', nombre: 'Contra Entrega' }
  ];

  esEditar(): boolean {
    return this.modo() === 'editar';
  }

  getControl(name: string): FormControl {
    return this.supplierForm.get(name) as FormControl;
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

    if (this.esEditar()) {
      const id = this.supplierId();
      if (id != null) {
        this.cargarYConstruir(id);
        return;
      }
    }
    this.construirFormularioCrear();
  }

  private cargarYConstruir(id: number): void {
    this.cargandoDetalle.set(true);
    this.supplierService.getById(id).subscribe({
      next: (detalle) => {
        this.detalle.set(detalle);
        this.cargandoDetalle.set(false);
        if (detalle.imagenUrl) {
          this.imagenPreview.set(`/api/proveedores/${detalle.idProveedor}/imagen`);
          this.imagenNombre.set(detalle.imagenUrl);
        }
        this.construirFormularioEditar(detalle);
      },
      error: () => {
        this.notificacion.error('No se pudo cargar el proveedor a editar.');
        this.cargandoDetalle.set(false);
        this.cerrado.emit();
      }
    });
  }

  private construirFormularioCrear(): void {
    this.supplierForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      nit: [''],
      contacto: [''],
      telefono: [''],
      email: ['', [Validators.email]],
      condicionPago: [null]
    });
  }

  private construirFormularioEditar(s: Supplier): void {
    this.supplierForm = this.fb.group({
      nombre: [s.nombre, [Validators.required, Validators.minLength(3)]],
      nit: [s.nit ?? ''],
      contacto: [s.contacto ?? ''],
      telefono: [s.telefono ?? ''],
      email: [s.email ?? '', [Validators.email]],
      condicionPago: [s.condicionPago ?? null]
    });
  }

  cerrar(): void {
    if (!this.guardando()) this.cerrado.emit();
  }

  solicitarGuardar(): void {
    if (!this.supplierForm || this.supplierForm.invalid) {
      this.supplierForm?.markAllAsTouched();
      this.notificacion.advertencia('Completa los campos obligatorios antes de guardar.');
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

  private guardarCreacion(): void {
    const v = this.supplierForm.value;
    const request: CreateSupplierRequest = {
      nombre: String(v.nombre).trim(),
      nit: v.nit?.trim() || undefined,
      contacto: v.contacto?.trim() || undefined,
      telefono: v.telefono?.trim() || undefined,
      email: v.email?.trim() || undefined,
      condicionPago: v.condicionPago || undefined
    };
    this.guardando.set(true);
    this.supplierService.create(request).subscribe({
      next: (creado: any) => {
        const id = creado?.idProveedor as number | undefined;
        const archivo = this.archivoImagen();
        if (id && archivo) {
          this.supplierService.subirImagen(id, archivo).subscribe({
            next: () => {
              this.notificacion.exito(`Proveedor "${request.nombre}" creado con imagen`);
              this.finalizar();
            },
            error: (err) => {
              this.notificacion.advertencia(`Proveedor creado pero la imagen no se pudo subir: ${err.error?.message || err.message}`);
              this.finalizar();
            }
          });
        } else {
          this.notificacion.exito(`Proveedor "${request.nombre}" creado correctamente`);
          this.finalizar();
        }
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo crear el proveedor.');
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
      }
    });
  }

  private guardarEdicion(): void {
    const id = this.detalle()?.idProveedor;
    if (!id) return;
    const v = this.supplierForm.value;
    const request: UpdateSupplierRequest = {
      nombre: String(v.nombre).trim(),
      nit: v.nit?.trim() || undefined,
      telefono: v.telefono?.trim() || undefined,
      email: v.email?.trim() || undefined,
      contacto: v.contacto?.trim() || undefined,
      condicionPago: v.condicionPago || undefined
    };
    this.guardando.set(true);
    this.supplierService.update(id, request).subscribe({
      next: () => {
        const archivo = this.archivoImagen();
        const debeEliminar = this.eliminarImagenExistente();
        if (archivo) {
          this.supplierService.subirImagen(id, archivo).subscribe({
            next: () => {
              this.notificacion.exito(`Proveedor "${request.nombre}" actualizado con imagen`);
              this.finalizar();
            },
            error: (err) => {
              this.notificacion.error(err.error?.message || 'No se pudo subir la imagen.');
              this.guardando.set(false);
              this.mostrarConfirmacion.set(false);
            }
          });
        } else if (debeEliminar) {
          this.supplierService.eliminarImagen(id).subscribe({
            next: () => {
              this.notificacion.exito(`Imagen eliminada y proveedor "${request.nombre}" actualizado`);
              this.finalizar();
            },
            error: () => {
              this.notificacion.exito(`Proveedor "${request.nombre}" actualizado correctamente`);
              this.finalizar();
            }
          });
        } else {
          this.notificacion.exito(`Proveedor "${request.nombre}" actualizado correctamente`);
          this.finalizar();
        }
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo actualizar el proveedor.');
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
