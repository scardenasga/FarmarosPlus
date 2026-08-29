import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavigationService } from '../../../shared/services/navigation.service';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { SupplierService } from '../../services/supplier.service';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { UpdateSupplierRequest, Supplier } from '../../models/supplier.model';

@Component({
  selector: 'app-edit-supplier',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormInputComponent,
    ConfirmationDialogComponent
  ],
  templateUrl: './edit-supplier.component.html',
  styleUrl: './edit-supplier.component.css'
})
export class EditSupplierComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private navService = inject(NavigationService);
  private supplierService = inject(SupplierService);
  private notificacion = inject(NotificacionService);

  supplierForm!: FormGroup;
  showConfirmation = signal<boolean>(false);
  supplierId = signal<number | null>(null);

  ngOnInit(): void {
    this.navService.hideNav();
    this.initForm();
    
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.supplierId.set(id);
      this.loadSupplier(id);
    } else {
      this.onCancel();
    }
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  getControl(name: string): FormControl {
    return this.supplierForm.get(name) as FormControl;
  }

  private initForm(): void {
    this.supplierForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      nit: [''],
      contacto: [''],
      telefono: [''],
      email: ['', [Validators.email]],
      condicionPago: ['']
    });
  }

  private loadSupplier(id: number): void {
    this.supplierService.getById(id).subscribe({
      next: (s) => {
        this.supplierForm.patchValue({
          nombre: s.nombre,
          nit: s.nit,
          contacto: s.contacto,
          telefono: s.telefono,
          email: s.email,
          condicionPago: s.condicionPago
        });
      },
      error: (err) => {
        console.error('Error loading supplier', err);
        this.onCancel();
      }
    });
  }

  onSave(): void {
    if (this.supplierForm.invalid) {
      this.supplierForm.markAllAsTouched();
      return;
    }
    this.showConfirmation.set(true);
  }

  confirmUpdate(): void {
    const id = this.supplierId();
    if (!id) return;

    const request: UpdateSupplierRequest = this.supplierForm.value;

    this.supplierService.update(id, request).subscribe({
      next: () => {
        this.notificacion.exito(`Proveedor "${request.nombre}" actualizado correctamente`);
        this.showConfirmation.set(false);
        this.router.navigate(['/proveedores', id]);
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo actualizar el proveedor.');
        this.showConfirmation.set(false);
      }
    });
  }

  cancelUpdate(): void {
    this.showConfirmation.set(false);
  }

  onCancel(): void {
    const id = this.supplierId();
    if (id) {
      this.router.navigate(['/proveedores', id]);
    } else {
      this.router.navigate(['/proveedores']);
    }
  }
}
