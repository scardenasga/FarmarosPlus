import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { PurchasingService } from '../../services/purchasing.service';
import { CompraResponse } from '../../models/purchasing.model';
import { Supplier } from '../../../supplier/models/supplier.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';

@Component({
  selector: 'app-purchase-history',
  standalone: true,
  imports: [CommonModule, FormsModule, TopBarComponent, FabButtonComponent],
  templateUrl: './purchase-history.component.html',
  styleUrl: './purchase-history.component.css'
})
export class PurchaseHistoryComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private supplierService = inject(SupplierService);
  private router = inject(Router);

  compras = signal<CompraResponse[]>([]);
  proveedores = signal<Supplier[]>([]);
  filtroIdProveedor = signal<number | null>(null);
  cargando = signal<boolean>(true);
  error = signal<string>('');
  idExpandida = signal<number | null>(null);

  ngOnInit(): void {
    this.cargarProveedores();
    this.cargar();
  }

  cargarProveedores(): void {
    this.supplierService.listActive().subscribe({
      next: data => this.proveedores.set(data),
      error: () => {}
    });
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.idExpandida.set(null);
    
    this.purchasingService.listarCompras(this.filtroIdProveedor() ?? undefined).subscribe({
      next: data => {
        this.compras.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial.');
        this.cargando.set(false);
      }
    });
  }

  aplicarFiltro(event: any): void {
    const val = event.target.value;
    this.filtroIdProveedor.set(val === 'null' ? null : Number(val));
    this.cargar();
  }

  toggleDetalles(id: number): void {
    this.idExpandida.update(current => current === id ? null : id);
  }

  nueva(): void {
    this.router.navigate(['/purchasing/register-purchase']);
  }

  volver(): void {
    this.router.navigate(['/compras-gestion']);
  }

  totalUnidades(compra: CompraResponse): number {
    return compra.detalles.reduce((s, d) => s + d.cantidad, 0);
  }
}
