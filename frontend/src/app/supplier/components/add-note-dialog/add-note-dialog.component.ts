import { Component, input, output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { BottomSheetComponent } from '../../../shared/components/bottom-sheet/bottom-sheet.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { SupplierService } from '../../services/supplier.service';

@Component({
  selector: 'app-add-note-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BottomSheetComponent, FormInputComponent],
  templateUrl: './add-note-dialog.component.html',
  styleUrl: './add-note-dialog.component.css'
})
export class AddNoteDialogComponent {
  private supplierService = inject(SupplierService);
  private fb = inject(FormBuilder);

  isVisible = input.required<boolean>();
  supplierId = input.required<number>();

  close = output<void>();
  added = output<void>();

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
    titulo: ['', [Validators.required]],
    descripcion: ['', [Validators.required]]
  });

  getControl(name: string): FormControl {
    return this.noteForm.get(name) as FormControl;
  }

  handleClose(): void {
    this.noteForm.reset();
    this.close.emit();
  }

  confirmNote(): void {
    if (this.noteForm.invalid) return;

    this.supplierService.createNote(this.supplierId(), this.noteForm.value).subscribe({
      next: () => {
        this.noteForm.reset();
        this.added.emit();
      },
      error: (err) => console.error('Error registrando la nota', err)
    });
  }
}
