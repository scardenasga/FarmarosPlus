import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { PurchasingService } from '../../services/purchasing.service';
import { OrdenCompraResumen, RecepcionCompraResumen, DevolucionResponse, AlertaDetallada } from '../../models/purchasing.model';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { Supplier } from '../../../supplier/models/supplier.model';
import { NotificacionService } from '../../../shared/services/notificacion.service';

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
  imports: [CommonModule, RouterModule, NgChartsModule],
  templateUrl: './purchasing-dashboard.component.html',
  styleUrl: './purchasing-dashboard.component.css'
})
export class PurchasingDashboardComponent implements OnInit {
  private recepcionesPagadasCache: RecepcionCompraResumen[] = [];
  private readonly router = inject(Router);
  private readonly purchasingService = inject(PurchasingService);
  private readonly supplierService = inject(SupplierService);
  private readonly notificacion = inject(NotificacionService);

  readonly accionesIconos = {
    proveedores: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 0 .01M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
    historial: 'M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z',
    devoluciones: 'M9 14 4 9l5-5M4 9h10a6 6 0 0 1 0 12h-3'
  };

  readonly loading = signal(true);
  readonly error = signal('');
  readonly notificacionesPendientes = signal(0);
  readonly alertas = signal<AlertaDetallada[]>([]);
  readonly campanaAbierta = signal<boolean>(false);
  readonly totalMonth = signal(0);
  readonly averagePurchase = signal(0);
  readonly activeSuppliers = signal(0);
  readonly recentReturns = signal(0);
  readonly purchasesCount = signal(0);
  readonly monthlyTotals = signal<MonthlyTotal[]>([]);
  readonly mesesChart = signal<number>(Number(localStorage.getItem('compras.mesesChart')) || 6);
  readonly tipoGrafica = signal<'barras' | 'linea'>((localStorage.getItem('compras.tipoGrafica') as 'barras' | 'linea') ?? 'barras');
  readonly supplierTotals = signal<SupplierTotal[]>([]);
  readonly pagosPorEstado = signal<{ estado: string; cantidad: number }[]>([]);
  readonly totalRecepciones = signal(0);

  /** Colores fijos por estado para el donut. */
  colorEstado(estado: string): string {
    const mapa: Record<string, string> = {
      PAGADO: '#34a853',
      PENDIENTE: '#f0b37e',
      PARCIAL: '#4285f4'
    };
    return mapa[estado.toUpperCase()] ?? '#9aa0a6';
  }

  /** Gradiente cónico para el donut a partir de pagosPorEstado. */
  donutGradient(): string {
    const estados = this.pagosPorEstado();
    const total = this.totalRecepciones();
    if (!total) return 'conic-gradient(var(--surface-container-high) 0deg)';
    let acumulado = 0;
    const segmentos: string[] = [];
    for (const e of estados) {
      const desde = (acumulado / total) * 360;
      acumulado += e.cantidad;
      const hasta = (acumulado / total) * 360;
      segmentos.push(this.colorEstado(e.estado) + ' ' + desde.toFixed(1) + 'deg ' + hasta.toFixed(1) + 'deg');
    }
    return 'conic-gradient(' + segmentos.join(', ') + ')';
  }

  readonly actions: DashboardAction[] = [
    { title: 'Proveedores', subtitle: 'Directorio y catÃ¡logos', icon: 'proveedores', link: '/proveedores', accent: '#83d5c5' },
    { title: 'Compras', subtitle: 'Ordenes y recepciones', icon: 'historial', link: '/purchasing/purchase-history', accent: '#accae5' },
    { title: 'Devoluciones', subtitle: 'Gestionar retornos', icon: 'devoluciones', link: '/purchasing/return-history', accent: '#f0b37e' }
  ];

  /** Cierra la ventana flotante al hacer clic fuera. */
  @HostListener('document:click')
  cerrarCampana(): void {
    this.campanaAbierta.set(false);
  }
  ngOnInit(): void {
    this.loadStatistics();
    this.cargarNotificaciones();
  }

  /** Carga las ordenes pendientes para la campana y su badge. */
  cargarNotificaciones(): void {
    this.purchasingService.getResumenSeguimiento().subscribe({
      next: (resumen) => {
        this.notificacionesPendientes.set(resumen?.pendientesRecibirOPagar ?? 0);
        this.alertas.set(resumen?.alertasDetalladas ?? []);
      },
      error: () => {
        this.notificacionesPendientes.set(0);
        this.alertas.set([]);
      }
    });
  }

  toggleCampana(event: Event): void {
    event.stopPropagation();
    this.campanaAbierta.update((v: boolean) => !v);
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
      next: ({ purchases, receptions, returns, suppliers }) => {
        this.buildStatistics(purchases, receptions, returns, suppliers);
      },
      error: () => {
        this.error.set('No se pudieron cargar las estadÃ­sticas. Verifica la conexiÃ³n con el backend.');
        this.notificacion.error('No se pudieron cargar las estadÃ­sticas de compras.');
        this.loading.set(false);
      }
    });
  }

  private buildStatistics(purchases: OrdenCompraResumen[], receptions: RecepcionCompraResumen[], returns: DevolucionResponse[], suppliers: Supplier[]): void {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();
    const paidReceptions = receptions.filter(r => r.estadoPago === 'PAGADO');
    this.recepcionesPagadasCache = paidReceptions;
    const monthPurchases = paidReceptions.filter(p => {
      const date = new Date(p.fechaRecepcion);
      return date.getMonth() === currentMonth && date.getFullYear() === currentYear;
    });

    this.totalMonth.set(monthPurchases.reduce((sum, purchase) => sum + purchase.montoPagado, 0));
    this.averagePurchase.set(monthPurchases.length ? this.totalMonth() / monthPurchases.length : 0);
    this.activeSuppliers.set(suppliers.length);
    this.recentReturns.set(returns.filter(item => this.isInCurrentMonth(item.fecha)).length);
    this.purchasesCount.set(paidReceptions.length);

    // Agrupacion de recepciones por estado de pago (todas, no solo pagadas).
    const conteoPagos = new Map<string, number>();
    receptions.forEach(r => conteoPagos.set(
      (r.estadoPago || 'SIN ESTADO').toUpperCase(),
      (conteoPagos.get((r.estadoPago || 'SIN ESTADO').toUpperCase()) ?? 0) + 1
    ));
    this.pagosPorEstado.set([...conteoPagos.entries()]
      .map(([estado, cantidad]) => ({ estado, cantidad }))
      .sort((a, b) => b.cantidad - a.cantidad));
    this.totalRecepciones.set(receptions.length);

    this.recalcularMensual();

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

  /** Reconstruye la grafica mensual con el periodo seleccionado (3/6/12 meses). */
  recalcularMensual(): void {
    const paid = this.recepcionesPagadasCache;
    const now = new Date();
    const months: MonthlyTotal[] = [];
    for (let offset = this.mesesChart() - 1; offset >= 0; offset--) {
      const date = new Date(now.getFullYear(), now.getMonth() - offset, 1);
      const total = paid
        .filter(p => {
          const d = new Date(p.fechaRecepcion);
          return d.getMonth() === date.getMonth() && d.getFullYear() === date.getFullYear();
        })
        .reduce((sum, p) => sum + p.montoPagado, 0);
      months.push({ label: date.toLocaleDateString('es-CO', { month: 'short' }).replace('.', ''), total });
    }
    this.monthlyTotals.set(months);
  }

  aMeses(valor: unknown): number {
    return Number(valor);
  }

  cambiarPeriodo(meses: number): void {
    this.mesesChart.set(meses);
    localStorage.setItem('compras.mesesChart', String(meses));
    this.recalcularMensual();
  }

  cambiarTipoGrafica(tipo: 'barras' | 'linea'): void {
    this.tipoGrafica.set(tipo);
    localStorage.setItem('compras.tipoGrafica', tipo);
  }
  private isInCurrentMonth(value: string): boolean {
    const date = new Date(value);
    const now = new Date();
    return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
  }

  navigateTo(link: string): void { this.router.navigate([link]); }

  /** Marca el acceso rapido de la seccion en la que estas. */
  esRutaActiva(link: string): boolean {
    return this.router.url.startsWith(link);
  }

  iconoDe(nombre: string): string {
    const mapa: Record<string, string> = this.accionesIconos;
    return mapa[nombre] ?? mapa['historial'];
  }
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(value);
  }


  readonly gastoChartData = computed<ChartData<'bar' | 'line', number[], string>>(() => {
    const esLinea = this.tipoGrafica() === 'linea';
    return {
      labels: this.monthlyTotals().map(m => m.label),
      datasets: [
        {
          label: 'Gasto pagado',
          data: this.monthlyTotals().map(m => Math.round(m.total)),
          borderColor: '#0d6e48',
          backgroundColor: esLinea ? 'rgba(13, 110, 72, 0.18)' : '#0d6e48',
          borderWidth: 2.5,
          fill: esLinea,
          tension: 0.3,
          pointBackgroundColor: '#0d6e48',
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2,
          pointRadius: 4,
          borderRadius: esLinea ? 0 : 6
        }
      ]
    };
  });

  readonly gastoChartOptions = computed<ChartOptions<'bar' | 'line'>>(() => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(ctx.parsed.y ?? ctx.parsed))
        }
      }
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#89938f', font: { size: 11, weight: 600 } }
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(137,147,143,0.15)' },
        ticks: {
          color: '#89938f',
          font: { size: 10 },
          callback: (v) => '$' + new Intl.NumberFormat('es-CO', { notation: 'compact', maximumFractionDigits: 1 }).format(Number(v))
        }
      }
    }
  }));
  barHeight(value: number): number {
    const max = Math.max(...this.monthlyTotals().map(item => item.total), 1);
    return Math.max(value / max * 100, value ? 8 : 2);
  }
}
