import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { Product, ProductoDetalleResponse } from '../../models/product.model';
import { InventoryService } from '../../services/inventory.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, TopBarComponent, ConfirmationDialogComponent],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private inventoryService = inject(InventoryService);

  product = signal<ProductoDetalleResponse | null>(null);
  showDeleteConfirmation = signal<boolean>(false);
  isLotesOpen = signal<boolean>(false);

  ngOnInit(): void {
    this.navService.hideNav();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadProduct(id);
    } else {
      this.onBack();
    }
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  private loadProduct(id: number): void {
    this.inventoryService.getProductDetail(id).subscribe({
      next: (found) => this.product.set(found),
      error: (err) => {
        console.error('Error loading product', err);
        this.onBack();
      }
    });
  }

  toggleLotes(): void {
    this.isLotesOpen.update(v => !v);
  }

  getBarcodePattern(code: string): number[] {
    if (!code) return Array(20).fill(1).map(() => (Math.floor(Math.random() * 3) + 1) * 2);
    // Deterministic pattern based on the barcode string
    const pattern = [];
    for (let i = 0; i < code.length; i++) {
      const num = parseInt(code[i]) || i;
      // Multiplying by 2 to make each bar wider
      pattern.push(((num % 3) + 1) * 2);
      pattern.push((((num + 2) % 3) + 1) * 2);
    }
    // Pad to have a decent number of bars
    while (pattern.length < 30) pattern.push(...pattern.slice(0, 5));
    return pattern.slice(0, 40);
  }

  onBack(): void {
    this.router.navigate(['/inventario']);
  }

  onEdit(): void {
    this.router.navigate(['/inventario/editar', this.product()?.id]);
  }

  onDeleteRequest(): void {
    this.showDeleteConfirmation.set(true);
  }

  confirmDelete(): void {
    // Note: Assuming there will be an endpoint for this in the future
    console.log('Solicitud de eliminación para:', this.product()?.id);
    this.showDeleteConfirmation.set(false);
    this.router.navigate(['/inventario']);
  }

  cancelDelete(): void {
    this.showDeleteConfirmation.set(false);
  }
}
