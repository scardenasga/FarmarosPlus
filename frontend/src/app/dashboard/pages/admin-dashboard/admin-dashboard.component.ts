import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ChartData, ChartOptions } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';

import { DashboardService } from '../../services/dashboard.service';
import { DashboardResponse, ProductoStockBajo } from '../../models/dashboard.model';
import { AlertaResponse, AlertaService } from '../../../services/alerta.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  readonly alertaService = inject(AlertaService);
  private readonly router = inject(Router);

  readonly dashboard = signal<DashboardResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly lastUpdated = signal('');
  readonly showNotifications = signal(false);
  readonly notificationsList = signal<AlertaResponse[]>([]);

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
    this.cargarAlertas();
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
          this.error.set('No se pudo cargar el resumen del negocio.');
          this.loading.set(false);
        }
      });
  }

  cargarAlertas(): void {
    this.alertaService.listarAlertas(true).subscribe({
      next: list => {
        this.notificationsList.set(list);
        this.alertaService.contadorNoLeidas.set(list.length);
      },
      error: () => {}
    });
  }

  toggleNotifications(): void {
    this.showNotifications.update(val => !val);
  }

  marcarTodasLeidas(): void {
    this.alertaService.marcarTodasLeidas().subscribe({
      next: () => {
        this.notificationsList.set([]);
        this.alertaService.contadorNoLeidas.set(0);
        this.showNotifications.set(false);
      },
      error: () => {
        this.notificationsList.set([]);
        this.alertaService.contadorNoLeidas.set(0);
        this.showNotifications.set(false);
      }
    });
  }

  navegarAAlertas(): void {
    this.showNotifications.set(false);
    this.router.navigate(['/alertas']);
  }

  navegarAInventario(): void {
    this.router.navigate(['/inventario']);
  }

  navegarAAnalitica(): void {
    this.router.navigate(['/analitica']);
  }

  // KPI Computeds
  readonly ventasDelDia = computed(() => this.dashboard()?.resumen.ventasDelDia ?? 0);
  readonly cantidadVentasDelDia = computed(() => this.dashboard()?.resumen.cantidadVentasDelDia ?? 0);
  readonly ventasDelMes = computed(() => this.dashboard()?.resumen.ventasDelMes ?? 0);
  readonly cantidadVentasDelMes = computed(() => this.dashboard()?.resumen.cantidadVentasDelMes ?? 0);
  readonly comprasDelMes = computed(() => this.dashboard()?.resumen.comprasDelMes ?? 0);
  readonly gananciaDelMes = computed(() => this.dashboard()?.resumen.gananciaDelMes ?? 0);
  readonly margenGanancia = computed(() => this.dashboard()?.resumen.margenGanancia ?? 0);
  readonly productosPorVencer = computed(() => this.dashboard()?.resumen.productosPorVencer ?? 0);

  readonly ticketPromedio = computed(() => {
    const total = this.ventasDelMes();
    const count = this.cantidadVentasDelMes();
    return count > 0 ? total / count : 0;
  });

  readonly lowStockList = computed<ProductoStockBajo[]>(() => {
    return (this.dashboard()?.productosStockBajo ?? []).slice(0, 3);
  });

  // Main Sales Trend Line Chart
  readonly salesTrendData = computed<ChartData<'line', number[], string>>(() => {
    const ventas = this.dashboard()?.ventasPorDia ?? [];
    return {
      labels: ventas.map(item => this.formatShortDate(item.fecha)),
      datasets: [
        {
          data: ventas.map(item => item.total),
          label: 'Ventas ($)',
          borderColor: '#0d6e48',
          borderWidth: 2.5,
          backgroundColor: 'rgba(13, 110, 72, 0.2)',
          fill: true,
          tension: 0.2,
          pointBackgroundColor: '#e11d48',
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6
        }
      ]
    };
  });

  readonly salesTrendOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: {
        grid: { color: '#f1f5f9' },
        ticks: {
          font: { size: 10 },
          callback: (value: any) => this.formatCompactNumber(Number(value))
        }
      },
      x: {
        grid: { display: false },
        ticks: { font: { size: 10 } }
      }
    }
  };

  // Profit Donut Chart (Rentabilidad: Costos vs Ganancia Neta)
  readonly profitDonutData = computed<ChartData<'doughnut', number[], string>>(() => {
    const compras = this.comprasDelMes();
    const ganancia = Math.max(0, this.gananciaDelMes());
    const ventas = this.ventasDelMes();

    const dataValues = (compras === 0 && ganancia === 0)
      ? [1, 1]
      : [compras, ganancia > 0 ? ganancia : ventas];

    return {
      labels: ['Costo Proveedores', 'Ganancia Neta'],
      datasets: [
        {
          data: dataValues,
          backgroundColor: ['#e11d48', '#0d6e48'],
          borderWidth: 0
        }
      ]
    };
  });

  readonly profitDonutOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '70%',
    plugins: { legend: { display: false } }
  };

  // Profit Bar Chart (Comparativa Mensual)
  readonly profitBarData = computed<ChartData<'bar', number[], string>>(() => {
    const comp = this.dashboard()?.comparativaMensual ?? [];
    const labels = comp.map(item => item.mes);
    const ventas = comp.map(item => item.ventas);
    const costos = comp.map(item => item.costos);

    return {
      labels: labels.length > 0 ? labels : ['Jun', 'Jul', 'Ago'],
      datasets: [
        {
          label: 'Ventas',
          data: ventas.length > 0 ? ventas : [85000, 92000, 99200],
          backgroundColor: '#0d6e48',
          borderRadius: 4
        },
        {
          label: 'Costos',
          data: costos.length > 0 ? costos : [110000, 125000, 142150],
          backgroundColor: '#e11d48',
          borderRadius: 4
        }
      ]
    };
  });

  readonly profitBarOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { display: false },
      x: {
        grid: { display: false },
        ticks: { font: { size: 9 } }
      }
    }
  };

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }).format(value ?? 0);
  }

  formatCompactNumber(value: number): string {
    return new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 }).format(value ?? 0);
  }

  private formatShortDate(dateValue: string): string {
    if (!dateValue) return '';
    const parts = dateValue.split('-');
    if (parts.length === 3) {
      const day = parts[2];
      const monthNum = parseInt(parts[1], 10);
      const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
      return `${parseInt(day, 10)} de ${months[monthNum - 1]?.toLowerCase() || ''}`;
    }
    return dateValue;
  }

  private toIso(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
