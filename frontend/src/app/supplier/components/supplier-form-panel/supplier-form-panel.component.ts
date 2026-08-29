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
      next: () => {
        this.notificacion.exito(`Proveedor "${request.nombre}" creado correctamente`);
        this.finalizar();
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
        this.notificacion.exito(`Proveedor "${request.nombre}" actualizado correctamente`);
        this.finalizar();
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
