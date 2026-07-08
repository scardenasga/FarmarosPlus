import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { NgChartsModule } from 'ng2-charts';
import { Chart, ChartConfiguration, ChartData, registerables } from 'chart.js';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardResponse, DashboardPeriod } from '../../models/dashboard.model';

Chart.register(...registerables);

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, TopBarComponent, NgChartsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit, OnDestroy {

  private dashboardService = inject(DashboardService);
  private router = inject(Router);
  private refreshSub?: Subscription;

  dashboard = signal<DashboardResponse | null>(null);
  loading = signal(true);
  error = signal('');
  lastUpdated = signal('');

  selectedPreset = signal<'7d' | '30d' | '90d' | 'custom'>('90d');
  fechaInicio = '';
  fechaFin = '';
  private currentPeriod: any = null;

  salesChartData: ChartData<'line'> = { labels: [], datasets: [] };
  salesChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    interaction: { mode: 'index', intersect: false },
    plugins: { legend: { display: false }, tooltip: { enabled: true } },
    scales: { y: { beginAtZero: true } }
  };

  inventoryChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  inventoryChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    plugins: { legend: { position: 'bottom' }, tooltip: { enabled: true } }
  };

  productsChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  productsChartOptions: ChartConfiguration<'bar'>['options'] = {
    indexAxis: 'y',
    responsive: true,
    plugins: { legend: { display: false }, tooltip: { enabled: true } }
  };

  ngOnInit() {
    this.selectPreset('90d');
    this.refreshSub = interval(60000).subscribe(() => this.cargarDashboard());
  }

  ngOnDestroy() {
    this.refreshSub?.unsubscribe();
  }

  selectPreset(preset: '7d' | '30d' | '90d' | 'custom') {
    this.selectedPreset.set(preset);
    if (preset === 'custom') return;

    const fin = new Date();
    const inicio = new Date();
    const days = preset === '7d' ? 7 : preset === '90d' ? 90 : 30;
    inicio.setDate(fin.getDate() - days);

    this.currentPeriod = {
      preset,
      fechaInicio: this.toIso(inicio),
      fechaFin: this.toIso(fin)
    };
    this.cargarDashboard();
  }

  aplicarFechasPersonalizadas() {
    if (!this.fechaInicio || !this.fechaFin) return;
    this.currentPeriod = {
      preset: 'custom',
      fechaInicio: this.fechaInicio,
      fechaFin: this.fechaFin
    };
    this.cargarDashboard();
  }

  cargarDashboard() {
    if (!this.currentPeriod) return;
    this.loading.set(true);
    this.error.set('');

    this.dashboardService
      .obtenerDashboard(this.currentPeriod.fechaInicio, this.currentPeriod.fechaFin)
      .subscribe({
        next: (data) => {
          this.dashboard.set(data);
          this.actualizarGraficas(data);
          this.lastUpdated.set(new Date().toLocaleTimeString('es-CO'));
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el dashboard.');
          this.loading.set(false);
        }
      });
  }

  private actualizarGraficas(data: DashboardResponse) {
    this.salesChartData = {
      labels: data.ventasPorDia.map(v => v.fecha),
      datasets: [{
        label: 'Ventas ($)',
        data: data.ventasPorDia.map(v => v.total),
        borderColor: '#00897b',
        backgroundColor: 'rgba(0,137,123,0.15)',
        fill: true,
        tension: 0.4
      }]
    };

    this.inventoryChartData = {
      labels: data.inventarioPorCategoria.map(c => c.categoria),
      datasets: [{
        data: data.inventarioPorCategoria.map(c => c.stockTotal),
        backgroundColor: ['#00897b', '#26a69a', '#80cbc4', '#ffb74d', '#ef5350', '#42a5f5']
      }]
    };

    this.productsChartData = {
      labels: data.productosDestacados.map(p => p.nombre),
      datasets: [{
        label: 'Unidades vendidas',
        data: data.productosDestacados.map(p => p.cantidadVendida),
        backgroundColor: '#00897b'
      }]
    };
  }

  maxVenta(ventas: any[]): number {
    return Math.max(...ventas.map((v: any) => v.total), 1);
  }

  maxStock(categorias: any[]): number {
    return Math.max(...categorias.map((c: any) => c.stockTotal), 1);
  }

  handleAlerts() {
    this.router.navigate(['/alertas']);
  }

  irAHealth() {
    this.router.navigate(['/health']);
  }

  private toIso(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}