import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupplierNote } from '../../models/supplier.model';

@Component({
  selector: 'app-supplier-notes-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './supplier-notes-list.component.html',
  styleUrl: './supplier-notes-list.component.css'
})
export class SupplierNotesListComponent {
  notes = input.required<SupplierNote[]>();
  isOpen = signal<boolean>(false);

  addNote = output<void>();

  toggleAccordion(): void {
    this.isOpen.update(v => !v);
  }

  tipoLabel(tipo: string): string {
    const labels: Record<string, string> = {
      RECLAMO: 'Reclamo',
      OBSERVACION: 'Observación',
      RETRASO: 'Retraso',
      PRODUCTO_DEFECTUOSO: 'Producto defectuoso',
      CAMBIO_PRECIO: 'Cambio de precio',
      CAMBIO_CONTACTO: 'Cambio de contacto',
      OTRO: 'Otro'
    };
    return labels[tipo] || tipo;
  }
}
