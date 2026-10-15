import { Component, OnInit, inject, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ChartData, ChartOptions } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';

import { DashboardService } from '../../services/dashboard.service';
import { CategoriaService } from '../../../services/categoria.service';
import { InventoryService } from '../../../inventory/services/inventory.service';
import {
  AnaliticaDashboardResponse,
  PeriodPreset,
  ProductoAnalitica
} from '../../models/dashboard.model';
import { Categoria, Product } from '../../../inventory/models/product.model';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, NgChartsModule],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.css'
})
export class AnalyticsComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly inventoryService = inject(InventoryService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly data = signal<AnaliticaDashboardResponse | null>(null);

  readonly isFilterCollapsed = signal(true);
  readonly selectedPreset = signal<PeriodPreset>('30d');
  readonly startDate = signal<string>('');
  readonly endDate = signal<string>('');
  readonly selectedCategory = signal<string>('ALL');
  readonly selectedProduct = signal<string>('ALL');
  readonly selectedMetric = signal<'sales' | 'units' | 'margin'>('sales');
  readonly chartVisualType = signal<'line' | 'bar' | 'area'>('line');
  readonly compareWithPrevious = signal(false);
  readonly searchTerm = signal('');

  readonly categories = signal<Categoria[]>([]);
  readonly products = signal<Product[]>([]);

  ngOnInit(): void {
    this.initDates('30d');
    this.loadFilterOptions();
    this.cargarAnalitica();
  }

  initDates(preset: PeriodPreset): void {
    const today = new Date();
    const start = new Date(today);

    if (preset === '7d') {
      start.setDate(today.getDate() - 7);
    } else if (preset === '30d') {
      start.setDate(today.getDate() - 30);
    } else if (preset === '90d') {
      start.setDate(today.getDate() - 90);
    }

    this.selectedPreset.set(preset);
    this.startDate.set(this.toIso(start));
    this.endDate.set(this.toIso(today));
  }

  loadFilterOptions(): void {
    this.categoriaService.listar().subscribe({
      next: cats => this.categories.set(cats),
      error: () => {}
    });

    this.inventoryService.getActiveProducts().subscribe({
      next: prods => this.products.set(prods),
      error: () => {}
    });
  }

  cargarAnalitica(): void {
    this.loading.set(true);
    this.error.set('');

    const catId = this.selectedCategory() !== 'ALL' ? Number(this.selectedCategory()) : null;
    const prodId = this.selectedProduct() !== 'ALL' ? Number(this.selectedProduct()) : null;

    this.dashboardService
      .obtenerAnalitica(
        this.startDate(),
        this.endDate(),
        catId,
        prodId,
        this.compareWithPrevious()
      )
      .subscribe({
        next: resp => {
          this.data.set(resp);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar la información analítica.');
          this.loading.set(false);
        }
      });
  }

  toggleFilters(): void {
    this.isFilterCollapsed.update(val => !val);
  }

  onPresetChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const preset = target.value as PeriodPreset;
    if (preset !== 'custom') {
      this.initDates(preset);
    } else {
      this.selectedPreset.set('custom');
    }
    this.cargarAnalitica();
  }

  onFilterChange(): void {
    this.cargarAnalitica();
  }

  setChartVisualType(type: 'line' | 'bar' | 'area'): void {
    this.chartVisualType.set(type);
  }

  goBackHome(): void {
    this.router.navigate(['/dashboard']);
  }

  // Badges text
  readonly badgeRange = computed(() => {
    switch (this.selectedPreset()) {
      case '7d': return 'Últimos 7 días';
      case '30d': return 'Últimos 30 días';
      case '90d': return 'Trimestre Actual';
      default: return `${this.startDate()} a ${this.endDate()}`;
    }
  });

  readonly badgeCategory = computed(() => {
    const catVal = this.selectedCategory();
    if (catVal === 'ALL') return 'Todas las categ.';
    const found = this.categories().find(c => c.id.toString() === catVal);
    return found ? found.nombre : 'Categoría';
  });

  readonly badgeMetric = computed(() => {
    switch (this.selectedMetric()) {
      case 'sales': return 'Ventas ($)';
      case 'units': return 'Unidades (un.)';
      case 'margin': return 'Margen (%)';
    }
  });

  readonly dynamicChartTitle = computed(() => {
    switch (this.selectedMetric()) {
      case 'sales': return 'Tendencia de Ventas Totales ($)';
      case 'units': return 'Tendencia de Unidades Vendidas (un.)';
      case 'margin': return 'Evolución de Margen Bruto Promedio (%)';
    }
  });

  // Main Interactive Dynamic Chart Data
  readonly mainChartData = computed<ChartData<any>>(() => {
    const resp = this.data();
    const metric = this.selectedMetric();
    const visualType = this.chartVisualType();
    const isArea = visualType === 'area';
    const chartType = isArea ? 'line' : visualType;

    const actualSeries = resp?.tendenciaActual ?? [];
    const prevSeries = resp?.tendenciaAnterior ?? [];

    const labels = actualSeries.map(item => this.formatShortDate(item.fecha));

    let actualValues: number[] = [];
    if (metric === 'sales') {
      actualValues = actualSeries.map(item => item.total);
    } else if (metric === 'units') {
      actualValues = actualSeries.map(item => item.cantidad);
    } else {
      const avgMargen = resp?.resumen.margenBrutoPromedio ?? 35.0;
      actualValues = actualSeries.map(() => avgMargen);
    }

    const datasets: any[] = [
      {
        label: metric === 'sales' ? 'Período Actual ($)' : (metric === 'units' ? 'Unidades Vendidas' : 'Margen (%)'),
        data: actualValues,
        borderColor: '#0d6e48',
        backgroundColor: isArea ? 'rgba(13, 110, 72, 0.22)' : '#0d6e48',
        borderWidth: 2.5,
        fill: isArea,
        tension: 0.3,
        pointBackgroundColor: '#0d6e48',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6,
        borderRadius: visualType === 'bar' ? 6 : 0
      }
    ];

    if (this.compareWithPrevious() && prevSeries.length > 0) {
      let prevValues: number[] = [];
      if (metric === 'sales') {
        prevValues = prevSeries.map(item => item.total);
      } else if (metric === 'units') {
        prevValues = prevSeries.map(item => item.cantidad);
      } else {
        const avgMargen = (resp?.resumen.margenBrutoPromedio ?? 35.0) * 0.95;
        prevValues = prevSeries.map(() => avgMargen);
      }

      datasets.push({
        label: 'Período Anterior',
        data: prevValues,
        borderColor: '#94a3b8',
        backgroundColor: isArea ? 'rgba(148, 163, 184, 0.2)' : '#cbd5e1',
        borderWidth: 2,
        borderDash: [5, 5],
        fill: isArea,
        tension: 0.3,
        pointBackgroundColor: '#94a3b8',
        pointRadius: 3,
        borderRadius: visualType === 'bar' ? 6 : 0
      });
    }

    return {
      labels,
      datasets
    };
  });

  readonly mainChartOptions: ChartOptions<any> = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: {
        position: 'top',
        labels: {
          font: { family: 'Inter', size: 11, weight: 'bold' }
        }
      }
    },
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

  // Category Distribution Horizontal Bar Chart
  readonly categoryChartData = computed<ChartData<'bar', number[], string>>(() => {
    const list = this.data()?.ventasPorCategoria ?? [];
    return {
      labels: list.map(item => item.categoria),
      datasets: [
        {
          label: 'Ventas ($)',
          data: list.map(item => item.stockTotal),
          backgroundColor: ['#0d6e48', '#0284c7', '#7e22ce', '#ea580c', '#d97706', '#64748b'],
          borderRadius: 6
        }
      ]
    };
  });

  readonly categoryChartOptions: ChartOptions<'bar'> = {
    indexAxis: 'y',
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: {
        grid: { color: '#f1f5f9' },
        ticks: {
          font: { size: 9 },
          callback: (value: any) => this.formatCompactNumber(Number(value))
        }
      },
      y: {
        grid: { display: false },
        ticks: { font: { size: 10, weight: 'bold' } }
      }
    }
  };

  // Filtered Product Table
  readonly filteredProducts = computed(() => {
    const list = this.data()?.productos ?? [];
    const search = this.searchTerm().toLowerCase().trim();
    if (!search) return list;
    return list.filter(
      p => p.nombre.toLowerCase().includes(search) || p.categoria.toLowerCase().includes(search)
    );
  });

  // Export to CSV
  exportToCSV(): void {
    const items = this.filteredProducts();
    if (items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const headers = ['Producto', 'Categoría', 'Unidades Vendidas', 'Ventas Totales (COP)', 'Margen %', 'Stock Actual', 'Stock Mínimo', 'Estado'];
    const rows = items.map(p => [
      `"${p.nombre.replace(/"/g, '""')}"`,
      `"${p.categoria.replace(/"/g, '""')}"`,
      p.unidadesVendidas,
      p.ventasTotales,
      `${p.margenGanancia}%`,
      p.stockActual,
      p.stockMinimo,
      p.estadoStock === 'critical' ? 'Crítico' : (p.estadoStock === 'warning' ? 'Bajo Stock' : 'Normal')
    ]);

    const csvContent = '\uFEFF' + [headers.join(';'), ...rows.map(r => r.join(';'))].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    const nowStr = new Date().toISOString().split('T')[0];
    link.setAttribute('download', `reporte_analitica_farmaros_${nowStr}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

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
      return `${parseInt(day, 10)} ${months[monthNum - 1] || ''}`;
    }
    return dateValue;
  }

  private toIso(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
