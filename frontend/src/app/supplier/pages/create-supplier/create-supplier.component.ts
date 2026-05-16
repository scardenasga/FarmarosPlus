import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { SupplierService } from '../../services/supplier.service';
import { CreateSupplierRequest } from '../../models/supplier.model';

@Component({
  selector: 'app-create-supplier',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TopBarComponent,
    FormInputComponent,
    ConfirmationDialogComponent
  ],
  templateUrl: './create-supplier.component.html',
  styleUrl: './create-supplier.component.css'
})
export class CreateSupplierComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private supplierService = inject(SupplierService);

  supplierForm!: FormGroup;
  showConfirmation = signal<boolean>(false);

  ngOnInit(): void {
    this.navService.hideNav();
    this.initForm();
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

  onSave(): void {
    if (this.supplierForm.invalid) {
      this.supplierForm.markAllAsTouched();
      return;
    }
    this.showConfirmation.set(true);
  }

  confirmCreation(): void {
    const request: CreateSupplierRequest = this.supplierForm.value;

    this.supplierService.create(request).subscribe({
      next: () => {
        this.showConfirmation.set(false);
        this.router.navigate(['/proveedores']);
      },
      error: (err) => {
        console.error('Error creating supplier', err);
        this.showConfirmation.set(false);
        // Here you could show an error toast/message
      }
    });
  }

  cancelCreation(): void {
    this.showConfirmation.set(false);
  }

  onCancel(): void {
    this.router.navigate(['/proveedores']);
  }
}
