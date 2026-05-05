import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { Product } from '../../models/product.model';

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

  product = signal<Product | null>(null);
  showDeleteConfirmation = signal<boolean>(false);

  ngOnInit(): void {
    this.navService.hideNav();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadProduct(id);
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  private loadProduct(id: number): void {
    // Mock data based on id
    const mockProducts: Product[] = [
      { 
        id: 1, 
        name: 'Acetaminofen 90mL', 
        price: 7500, 
        stock: 26, 
        unit: 'und.', 
        category: 'Jarabe', 
        description: 'Jarabe pediátrico para el alivio rápido del dolor y la fiebre. Sabor a cereza, libre de azúcar y alcohol. Ideal para el cuidado de los más pequeños.',
        trend: 1.2,
        lotes: [
          { id: 101, numeroLote: 'LOT-2024-001', fechaVencimiento: '2025-12-31', cantidad: 10 },
          { id: 102, numeroLote: 'LOT-2024-015', fechaVencimiento: '2026-06-15', cantidad: 16 }
        ]
      },
      { 
        id: 2, 
        name: 'Ibuprofeno 400mg', 
        price: 12000, 
        stock: 15, 
        unit: 'und.', 
        category: 'Tableta', 
        description: 'Potente antiinflamatorio y analgésico. Indicado para dolores musculares, cefaleas y estados febriles intensos.',
        trend: -0.5,
        lotes: [
          { id: 201, numeroLote: 'IBU-XP-99', fechaVencimiento: '2024-11-20', cantidad: 15 }
        ]
      },
      { 
        id: 3, 
        name: 'Amoxicilina 500mg', 
        price: 15000, 
        stock: 40, 
        unit: 'und.', 
        category: 'Cápsula', 
        description: 'Antibiótico de amplio espectro. Requiere fórmula médica para su despacho.',
        trend: 2.1,
        lotes: []
      }
    ];


    const found = mockProducts.find(p => p.id === id) || mockProducts[0];
    this.product.set(found);
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
    console.log('Producto eliminado (simulado):', this.product()?.id);
    this.showDeleteConfirmation.set(false);
    this.router.navigate(['/inventario']);
  }

  cancelDelete(): void {
    this.showDeleteConfirmation.set(false);
  }
}
