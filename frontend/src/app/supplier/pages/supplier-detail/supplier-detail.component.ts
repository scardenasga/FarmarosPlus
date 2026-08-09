import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { SupplierService } from '../../services/supplier.service';
import { SupplierDetalleResponse, SupplierProductRel, SupplierNote } from '../../models/supplier.model';
import { SupplierInfoCardComponent } from '../../components/supplier-info-card/supplier-info-card.component';
import { SupplierProductListComponent } from '../../components/supplier-product-list/supplier-product-list.component';
import { AssociateProductDialogComponent } from '../../components/associate-product-dialog/associate-product-dialog.component';
import { SupplierNotesListComponent } from '../../components/supplier-notes-list/supplier-notes-list.component';
import { AddNoteDialogComponent } from '../../components/add-note-dialog/add-note-dialog.component';

@Component({
  selector: 'app-supplier-detail',
  standalone: true,
  imports: [
    CommonModule,
    TopBarComponent,
    ConfirmationDialogComponent,
    SupplierInfoCardComponent,
    SupplierProductListComponent,
    AssociateProductDialogComponent,
    SupplierNotesListComponent,
    AddNoteDialogComponent
  ],
  templateUrl: './supplier-detail.component.html',
  styleUrl: './supplier-detail.component.css'
})
export class SupplierDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private supplierService = inject(SupplierService);

  supplier = signal<SupplierDetalleResponse | null>(null);
  notes = signal<SupplierNote[]>([]);
  showStatusConfirmation = signal<boolean>(false);
  showDeleteRelationConfirmation = signal<boolean>(false);
  showAssociateDialog = signal<boolean>(false);
  showAddNoteDialog = signal<boolean>(false);

  selectedProduct = signal<SupplierProductRel | null>(null);

  ngOnInit(): void {
    this.navService.hideNav();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadSupplier(id);
    } else {
      this.onBack();
    }
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  private loadSupplier(id: number): void {
    this.supplierService.getDetail(id).subscribe({
      next: (found) => this.supplier.set(found),
      error: (err) => {
        console.error('Error loading supplier', err);
        this.onBack();
      }
    });
    this.loadNotes(id);
  }

  private loadNotes(id: number): void {
    this.supplierService.listNotes(id).subscribe({
      next: (found) => this.notes.set(found),
      error: (err) => console.error('Error loading supplier notes', err)
    });
  }

  onBack(): void {
    this.router.navigate(['/proveedores']);
  }

  onEdit(): void {
    this.router.navigate(['/proveedores', this.supplier()?.idProveedor, 'editar']);
  }

  toggleSupplierStatus(): void {
    this.showStatusConfirmation.set(true);
  }

  confirmStatusChange(): void {
    const s = this.supplier();
    if (!s) return;

    const newStatus = s.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    this.supplierService.updateStatus(s.idProveedor, { estado: newStatus }).subscribe({
      next: (updated) => {
        this.supplier.update(prev => prev ? { ...prev, estado: updated.estado } : null);
        this.showStatusConfirmation.set(false);
      },
      error: (err) => {
        console.error('Error updating status', err);
        this.showStatusConfirmation.set(false);
      }
    });
  }

  handleToggleProductStatus(product: SupplierProductRel): void {
    const s = this.supplier();
    if (!s) return;

    const newStatus = product.estadoRelacion === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    this.supplierService.updateRelationStatus(s.idProveedor, product.id, { estado: newStatus }).subscribe({
      next: (updated) => {
        this.supplier.update(prev => {
          if (!prev) return null;
          return {
            ...prev,
            productos: prev.productos.map(p => p.id === product.id ? updated : p)
          };
        });
      },
      error: (err) => console.error('Error updating product relation status', err)
    });
  }

  handleRemoveProduct(product: SupplierProductRel): void {
    this.selectedProduct.set(product);
    this.showDeleteRelationConfirmation.set(true);
  }

  confirmRemoveRelation(): void {
    const s = this.supplier();
    const p = this.selectedProduct();
    if (!s || !p) return;

    this.supplierService.deleteRelation(s.idProveedor, p.id).subscribe({
      next: () => {
        this.supplier.update(prev => {
          if (!prev) return null;
          return {
            ...prev,
            productos: prev.productos.filter(prod => prod.id !== p.id)
          };
        });
        this.showDeleteRelationConfirmation.set(false);
        this.selectedProduct.set(null);
      },
      error: (err) => {
        console.error('Error deleting relation', err);
        this.showDeleteRelationConfirmation.set(false);
      }
    });
  }

  openAssociateDialog(): void {
    this.showAssociateDialog.set(true);
  }

  handleProductAssociated(): void {
    const id = this.supplier()?.idProveedor;
    if (id) this.loadSupplier(id);
    this.showAssociateDialog.set(false);
  }

  openAddNoteDialog(): void {
    this.showAddNoteDialog.set(true);
  }

  handleNoteAdded(): void {
    const id = this.supplier()?.idProveedor;
    if (id) this.loadNotes(id);
    this.showAddNoteDialog.set(false);
  }
}
