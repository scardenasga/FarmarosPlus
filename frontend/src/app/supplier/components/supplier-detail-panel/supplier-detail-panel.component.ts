import { Component, computed, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupplierService } from '../../services/supplier.service';
import { SupplierDetalleResponse, SupplierNote, SupplierProductRel } from '../../models/supplier.model';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { AssociateProductDialogComponent } from '../associate-product-dialog/associate-product-dialog.component';
import { AddNoteDialogComponent } from '../add-note-dialog/add-note-dialog.component';

@Component({
  selector: 'app-supplier-detail-panel',
  standalone: true,
  imports: [CommonModule, ConfirmationDialogComponent, AssociateProductDialogComponent, AddNoteDialogComponent],
  templateUrl: './supplier-detail-panel.component.html',
  styleUrl: './supplier-detail-panel.component.css'
})
export class SupplierDetailPanelComponent {
  private supplierService = inject(SupplierService);

  supplierId = input<number | null>(null);
  abierto = input<boolean>(false);

  cerrado = output<void>();
  editado = output<number>();
  actualizado = output<void>();

  detalle = signal<SupplierDetalleResponse | null>(null);
  notes = signal<SupplierNote[]>([]);
  cargando = signal<boolean>(false);

  showStatusConfirmation = signal<boolean>(false);
  showDeleteRelationConfirmation = signal<boolean>(false);
  showAssociateDialog = signal<boolean>(false);
  showAddNoteDialog = signal<boolean>(false);
  selectedProduct = signal<SupplierProductRel | null>(null);

  productosActivos = computed(() => (this.detalle()?.productos ?? []).filter(p => p.estadoRelacion === 'ACTIVO').length);
  productosInactivos = computed(() => (this.detalle()?.productos ?? []).filter(p => p.estadoRelacion === 'INACTIVO').length);

  patronBarras(codigo: string): number[] {
    let semilla = 0;
    for (let i = 0; i < codigo.length; i++) semilla = (semilla * 31 + codigo.charCodeAt(i)) % 100000;
    const barras: number[] = [];
    for (let i = 0; i < 18; i++) {
      semilla = (semilla * 1103515245 + 12345) % 2147483648;
      barras.push(1 + ((semilla >> 8) % 3));
    }
    return barras;
  }

  tipoLabel(tipo: string): string {
    const map: Record<string, string> = {
      RECLAMO: 'Reclamo',
      OBSERVACION: 'Observación',
      NOTA: 'Nota',
      INCIDENCIA: 'Incidencia'
    };
    return map[tipo?.toUpperCase()] ?? tipo;
  }

  cargar(): void {
    const id = this.supplierId();
    if (id == null) return;
    this.cargando.set(true);
    this.supplierService.getDetail(id).subscribe({
      next: (d) => { this.detalle.set(d); this.cargando.set(false); },
      error: () => this.cargando.set(false)
    });
    this.supplierService.listNotes(id).subscribe({
      next: (n) => this.notes.set(n ?? []),
      error: () => this.notes.set([])
    });
  }

  cerrar(): void {
    this.cerrado.emit();
  }

  solicitarEditar(): void {
    const id = this.detalle()?.idProveedor;
    if (id) this.editado.emit(id);
  }

  toggleStatus(): void { this.showStatusConfirmation.set(true); }

  confirmarCambioEstado(): void {
    const s = this.detalle();
    if (!s) return;
    const nuevo = s.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    this.supplierService.updateStatus(s.idProveedor, { estado: nuevo }).subscribe({
      next: (updated) => {
        this.detalle.update(prev => prev ? { ...prev, estado: updated.estado } : null);
        this.showStatusConfirmation.set(false);
        this.actualizado.emit();
      },
      error: () => this.showStatusConfirmation.set(false)
    });
  }

  handleToggleProductStatus(product: SupplierProductRel): void {
    const s = this.detalle();
    if (!s) return;
    const nuevo = product.estadoRelacion === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    this.supplierService.updateRelationStatus(s.idProveedor, product.id, { estado: nuevo }).subscribe({
      next: (updated) => {
        this.detalle.update(prev => prev ? { ...prev, productos: prev.productos.map(p => p.id === product.id ? updated : p) } : null);
      }
    });
  }

  handleRemoveProduct(product: SupplierProductRel): void {
    this.selectedProduct.set(product);
    this.showDeleteRelationConfirmation.set(true);
  }

  confirmRemoveRelation(): void {
    const s = this.detalle();
    const p = this.selectedProduct();
    if (!s || !p) return;
    this.supplierService.deleteRelation(s.idProveedor, p.id).subscribe({
      next: () => {
        this.detalle.update(prev => prev ? { ...prev, productos: prev.productos.filter(x => x.id !== p.id) } : null);
        this.showDeleteRelationConfirmation.set(false);
        this.selectedProduct.set(null);
      },
      error: () => this.showDeleteRelationConfirmation.set(false)
    });
  }

  openAssociate(): void { this.showAssociateDialog.set(true); }
  handleAssociated(): void { this.cargar(); this.showAssociateDialog.set(false); this.actualizado.emit(); }

  openAddNote(): void { this.showAddNoteDialog.set(true); }
  handleNoteAdded(): void {
    const id = this.detalle()?.idProveedor;
    if (id) this.supplierService.listNotes(id).subscribe(n => this.notes.set(n ?? []));
    this.showAddNoteDialog.set(false);
  }
}
