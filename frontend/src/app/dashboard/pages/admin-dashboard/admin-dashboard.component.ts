import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { ChartData, ChartOptions } from 'chart.js';

import { DashboardService } from '../../services/dashboard.service';
import { DashboardResponse } from '../../models/dashboard.model';
import { ChartPanelComponent } from '../../../shared/components/chart-panel/chart-panel.component';
import { AlertaService } from '../../../services/alerta.service';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ChartPanelComponent, TopBarComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private readonly dashboardService = inject(DashboardService);
  readonly alertaService = inject(AlertaService);
  private readonly router = inject(Router);
  private refreshSub?: Subscription;

  readonly dashboard = signal<DashboardResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly lastUpdated = signal('');
  readonly showMore = signal(false);
  private currentPeriod: { fechaInicio: string; fechaFin: string } | null = null;

  ngOnInit(): void {
    const fin = new Date();
    const inicio = new Date();
    inicio.setDate(fin.getDate() - 30);
    this.currentPeriod = {
      fechaInicio: this.toIso(inicio),
      fechaFin: this.toIso(fin)
    };
    this.cargarDashboard();
    this.refreshSub = interval(60000).subscribe(() => this.cargarDashboard());
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  cargarDashboard(): void {
    if (!this.currentPeriod) return;

    this.loading.set(true);
    this.error.set('');

    this.dashboardService
      .obtenerDashboard(this.currentPeriod.fechaInicio, this.currentPeriod.fechaFin)
      .subscribe({
        next: dashboard => {
          this.dashboard.set(dashboard);
          this.lastUpdated.set(new Date().toLocaleTimeString('es-CO'));
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el resumen.');
          this.loading.set(false);
        }
      });
  }

  handleAlerts(): void {
    this.router.navigate(['/alertas']);
  }

  revisarStock(): void {
    this.router.navigate(['/inventario']);
  }

  toggleMore(): void {
    this.showMore.update(value => !value);
  }

  ventasTrendData = computed<ChartData<'line', number[], string>>(() => {
    const ventas = this.dashboard()?.ventasPorDia ?? [];
    return {
      labels: ventas.map(item => this.formatShortDate(item.fecha)),
      datasets: [
        {
          data: ventas.map(item => item.total),
          label: 'Ventas',
          borderColor: 'rgba(0, 137, 123, 1)',
          backgroundColor: 'rgba(0, 137, 123, 0.18)',
          tension: 0.35,
          fill: true,
          pointRadius: 3,
          pointHoverRadius: 5
        }
      ]
    };
  });

  topProductsData = computed<ChartData<'bar', number[], string>>(() => {
    const top = this.dashboard()?.productosDestacados ?? [];
    return {
      labels: top.slice(0, 6).map(item => this.truncateLabel(item.nombre, 16)),
      datasets: [
        {
          data: top.slice(0, 6).map(item => item.cantidadVendida),
          label: 'Unidades vendidas',
          borderRadius: 10,
          backgroundColor: top.slice(0, 6).map((_, index) => this.palette[index % this.palette.length]),
          borderSkipped: false
        }
      ]
    };
  });

  inventoryMixData = computed<ChartData<'doughnut', number[], string>>(() => {
    const categories = this.dashboard()?.inventarioPorCategoria ?? [];
    return {
      labels: categories.map(item => this.truncateLabel(item.categoria, 14)),
      datasets: [
        {
          data: categories.map(item => item.stockTotal),
          backgroundColor: categories.map((_, index) => this.palette[index % this.palette.length]),
          borderWidth: 0,
          hoverOffset: 6
        }
      ]
    };
  });

  lowStockData = computed<ChartData<'bar', number[], string>>(() => {
    const products = this.dashboard()?.productosStockBajo ?? [];
    return {
      labels: products.slice(0, 6).map(item => this.truncateLabel(item.nombre, 18)),
      datasets: [
        {
          data: products.slice(0, 6).map(item => item.stockActual),
          label: 'Stock actual',
          backgroundColor: 'rgba(244, 67, 54, 0.75)',
          borderRadius: 10,
          borderSkipped: false
        },
        {
          data: products.slice(0, 6).map(item => item.stockMinimo),
          label: 'Stock mínimo',
          backgroundColor: 'rgba(0, 137, 123, 0.35)',
          borderRadius: 10,
          borderSkipped: false
        }
      ]
    };
  });

  readonly salesTrendOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false }, ticks: { color: '#6f6f6f' } },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#6f6f6f',
          callback: (value: string | number) => this.formatCompactNumber(Number(value))
        },
        grid: { color: 'rgba(127, 127, 127, 0.14)' }
      }
    }
  };

  readonly topProductsOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: {
      x: {
        beginAtZero: true,
        ticks: {
          color: '#6f6f6f',
          callback: (value: string | number) => this.formatCompactNumber(Number(value))
        },
        grid: { color: 'rgba(127, 127, 127, 0.12)' }
      },
      y: { ticks: { color: '#6f6f6f' }, grid: { display: false } }
    }
  };

  readonly inventoryMixOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '68%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#4a4a4a',
          boxWidth: 12,
          usePointStyle: true
        }
      }
    }
  };

  readonly lowStockOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: {
        position: 'top',
        labels: { color: '#4a4a4a' }
      }
    },
    scales: {
      x: { beginAtZero: true, ticks: { color: '#6f6f6f' }, grid: { color: 'rgba(127, 127, 127, 0.12)' } },
      y: { ticks: { color: '#6f6f6f' }, grid: { display: false } }
    }
  };

  readonly palette = [
    '#00897b',
    '#1565c0',
    '#7b1fa2',
    '#ef6c00',
    '#2e7d32',
    '#c62828'
  ];

  readonly summaryCards = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return [];

    return [
      {
        label: 'Ventas del día',
        value: this.formatCurrency(dashboard.resumen.ventasDelDia),
        helper: `${dashboard.resumen.cantidadVentasDelDia} venta${dashboard.resumen.cantidadVentasDelDia === 1 ? '' : 's'} hoy`,
        icon: '💰',
        tone: 'success'
      },
      {
        label: 'Ventas del mes',
        value: this.formatCurrency(dashboard.resumen.ventasDelMes),
        helper: `${dashboard.resumen.cantidadVentasDelMes} ventas en el período`,
        icon: '📈',
        tone: 'info'
      },
      {
        label: 'Stock bajo',
        value: this.formatCompactNumber(dashboard.resumen.productosStockBajo),
        helper: 'Productos que requieren reposición',
        icon: '⚠️',
        tone: 'danger'
      }
    ];
  });

  readonly lowStockList = computed(() => (this.dashboard()?.productosStockBajo ?? []).slice(0, 3));

  readonly secondaryStats = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return [];

    return [
      {
        label: 'Por vencer',
        value: dashboard.resumen.productosPorVencer,
        helper: 'Productos y lotes en seguimiento'
      },
      {
        label: 'Stock bajo',
        value: dashboard.resumen.productosStockBajo,
        helper: 'Comparte la misma fuente del inventario'
      },
      {
        label: 'Alertas activas',
        value: this.alertaService.contadorNoLeidas(),
        helper: 'Pendientes por revisar'
      }
    ];
  });

  private toIso(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  private formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }).format(value ?? 0);
  }

  private formatCompactNumber(value: number): string {
    return new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 }).format(value ?? 0);
  }

  private formatShortDate(dateValue: string): string {
    const date = new Date(dateValue);
    return new Intl.DateTimeFormat('es-CO', { day: '2-digit', month: 'short' }).format(date);
  }

  private truncateLabel(value: string, max: number): string {
    if (!value) return '';
    return value.length > max ? `${value.slice(0, max - 1)}…` : value;
  }
}
