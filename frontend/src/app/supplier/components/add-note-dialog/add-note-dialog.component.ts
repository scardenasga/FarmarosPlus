import { Component, input, output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { SupplierService } from '../../services/supplier.service';

@Component({
  selector: 'app-add-note-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './add-note-dialog.component.html',
  styleUrl: './add-note-dialog.component.css'
})
export class AddNoteDialogComponent {
  private supplierService = inject(SupplierService);
  private fb = inject(FormBuilder);
  private notificacion = inject(NotificacionService);

  isVisible = input.required<boolean>();
  supplierId = input.required<number>();

  close = output<void>();
  added = output<void>();

  guardando = signal<boolean>(false);
  mostrarConfirmacion = signal<boolean>(false);

  tipoOptions = [
    { id: 'RECLAMO', nombre: 'Reclamo' },
    { id: 'OBSERVACION', nombre: 'Observación' },
    { id: 'RETRASO', nombre: 'Retraso en entrega' },
    { id: 'PRODUCTO_DEFECTUOSO', nombre: 'Producto defectuoso' },
    { id: 'CAMBIO_PRECIO', nombre: 'Cambio de precio' },
    { id: 'CAMBIO_CONTACTO', nombre: 'Cambio de contacto' },
    { id: 'OTRO', nombre: 'Otro' }
  ];

  noteForm: FormGroup = this.fb.group({
    tipoNota: [null, [Validators.required]],
    titulo: ['', [Validators.required, Validators.minLength(4)]],
    descripcion: ['', [Validators.required, Validators.minLength(8)]]
  });

  getControl(name: string): FormControl {
    return this.noteForm.get(name) as FormControl;
  }

  handleClose(): void {
    if (this.guardando()) return;
    this.noteForm.reset();
    this.mostrarConfirmacion.set(false);
    this.close.emit();
  }

  solicitarRegistro(): void {
    if (this.noteForm.invalid) {
      this.noteForm.markAllAsTouched();
      this.notificacion.advertencia('Completa todos los campos obligatorios.');
      return;
    }
    this.mostrarConfirmacion.set(true);
  }

  cancelarConfirmacion(): void {
    this.mostrarConfirmacion.set(false);
  }

  confirmNote(): void {
    if (this.noteForm.invalid) return;
    this.guardando.set(true);
    this.supplierService.createNote(this.supplierId(), this.noteForm.value).subscribe({
      next: () => {
        this.notificacion.exito('Nota registrada correctamente');
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
        this.noteForm.reset();
        this.added.emit();
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo registrar la nota.');
        this.guardando.set(false);
        this.mostrarConfirmacion.set(false);
      }
    });
  }
}
