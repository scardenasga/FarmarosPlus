import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { PurchasingService } from '../../services/purchasing.service';
import { OrdenCompraResumen, RecepcionCompraResumen, DevolucionResponse } from '../../models/purchasing.model';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { Supplier } from '../../../supplier/models/supplier.model';

interface DashboardAction {
  title: string;
  subtitle: string;
  icon: string;
  link: string;
  accent: string;
}

interface MonthlyTotal {
  label: string;
  total: number;
}

interface SupplierTotal {
  name: string;
  total: number;
  percentage: number;
}

@Component({
  selector: 'app-purchasing-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, TopBarComponent],
  templateUrl: './purchasing-dashboard.component.html',
  styleUrl: './purchasing-dashboard.component.css'
})
export class PurchasingDashboardComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly purchasingService = inject(PurchasingService);
  private readonly supplierService = inject(SupplierService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly totalMonth = signal(0);
  readonly averagePurchase = signal(0);
  readonly activeSuppliers = signal(0);
  readonly recentReturns = signal(0);
  readonly purchasesCount = signal(0);
  readonly monthlyTotals = signal<MonthlyTotal[]>([]);
  readonly supplierTotals = signal<SupplierTotal[]>([]);

  readonly actions: DashboardAction[] = [
    { title: 'Proveedores', subtitle: 'Directorio y catálogos', icon: '🏢', link: '/proveedores', accent: '#83d5c5' },
    { title: 'Historial', subtitle: 'Compras y recepciones', icon: '📦', link: '/purchasing/purchase-history', accent: '#accae5' },
    { title: 'Devoluciones', subtitle: 'Gestionar retornos', icon: '↔', link: '/purchasing/return-history', accent: '#f0b37e' }
  ];

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading.set(true);
    this.error.set('');
    forkJoin({
      purchases: this.purchasingService.listarOrdenes(),
      receptions: this.purchasingService.listarRecepciones(),
      returns: this.purchasingService.listarDevoluciones(),
      suppliers: this.supplierService.listActive()
    }).subscribe({
      next: ({ purchases, receptions, returns, suppliers }) => this.buildStatistics(purchases, receptions, returns, suppliers),
      error: () => {
        this.error.set('No se pudieron cargar las estadísticas. Verifica la conexión con el backend.');
        this.loading.set(false);
      }
    });
  }

  private buildStatistics(purchases: OrdenCompraResumen[], receptions: RecepcionCompraResumen[], returns: DevolucionResponse[], suppliers: Supplier[]): void {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();
    const paidReceptions = receptions.filter(r => r.estadoPago === 'PAGADO');
    const monthPurchases = paidReceptions.filter(p => {
      const date = new Date(p.fechaRecepcion);
      return date.getMonth() === currentMonth && date.getFullYear() === currentYear;
    });

    this.totalMonth.set(monthPurchases.reduce((sum, purchase) => sum + purchase.montoPagado, 0));
    this.averagePurchase.set(monthPurchases.length ? this.totalMonth() / monthPurchases.length : 0);
    this.activeSuppliers.set(suppliers.length);
    this.recentReturns.set(returns.filter(item => this.isInCurrentMonth(item.fecha)).length);
    this.purchasesCount.set(paidReceptions.length);

    const months: MonthlyTotal[] = [];
    for (let offset = 5; offset >= 0; offset--) {
      const date = new Date(currentYear, currentMonth - offset, 1);
      const total = paidReceptions
        .filter(p => {
          const purchaseDate = new Date(p.fechaRecepcion);
          return purchaseDate.getMonth() === date.getMonth() && purchaseDate.getFullYear() === date.getFullYear();
        })
        .reduce((sum, purchase) => sum + purchase.montoPagado, 0);
      months.push({ label: date.toLocaleDateString('es-CO', { month: 'short' }).replace('.', ''), total });
    }
    this.monthlyTotals.set(months);

    const totalsBySupplier = new Map<string, number>();
    paidReceptions.forEach(purchase => totalsBySupplier.set(
      purchase.orden.proveedorNombre,
      (totalsBySupplier.get(purchase.orden.proveedorNombre) ?? 0) + purchase.montoPagado
    ));
    const grandTotal = paidReceptions.reduce((sum, purchase) => sum + purchase.montoPagado, 0);
    this.supplierTotals.set([...totalsBySupplier.entries()]
      .map(([name, total]) => ({ name, total, percentage: grandTotal ? (total / grandTotal) * 100 : 0 }))
      .sort((a, b) => b.total - a.total)
      .slice(0, 4));
    this.loading.set(false);
  }

  private isInCurrentMonth(value: string): boolean {
    const date = new Date(value);
    const now = new Date();
    return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
  }

  navigateTo(link: string): void { this.router.navigate([link]); }
  handleBack(): void { this.router.navigate(['/']); }
  handleAlerts(): void { this.router.navigate(['/purchasing/order-notifications']); }
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(value);
  }
  barHeight(value: number): number {
    const max = Math.max(...this.monthlyTotals().map(item => item.total), 1);
    return Math.max(value / max * 100, value ? 8 : 2);
  }
}
