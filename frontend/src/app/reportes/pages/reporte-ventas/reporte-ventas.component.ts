import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { PeriodFilterComponent, PeriodFilterValue } from '../../../shared/components/period-filter/period-filter.component';
import { VentaService } from '../../../ventas/services/venta.service';
import { ReporteService } from '../../services/reporte.service';

@Component({
  selector: 'app-reporte-ventas',
  standalone: true,
  imports: [CommonModule, TopBarComponent, PeriodFilterComponent],
  templateUrl: './reporte-ventas.component.html',
  styleUrl: './reporte-ventas.component.css'
})
export class ReporteVentasComponent implements OnInit {
  private ventaService = inject(VentaService);
  private reporteService = inject(ReporteService);
  private router = inject(Router);

  ventas = signal<any[]>([]);
  cargando = signal<boolean>(false);
  error = signal<string>('');
  exportando = signal<boolean>(false);

  private fechaInicioActual = '';
  private fechaFinActual = '';

  totalPeriodo = computed(() =>
    this.ventas().reduce((acc, v) => acc + (v.total || 0), 0)
  );

  ngOnInit(): void {}

  handleFiltroChange(filtro: PeriodFilterValue): void {
    this.fechaInicioActual = filtro.fechaInicio;
    this.fechaFinActual = filtro.fechaFin;

    this.cargando.set(true);
    this.error.set('');

    const inicio = this.aFormatoBackend(filtro.fechaInicio);
    const fin = this.aFormatoBackend(filtro.fechaFin);

    this.ventaService.consultarHistorico(inicio, fin).subscribe({
      next: (data) => {
        this.ventas.set(data || []);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error('Error generando reporte de ventas', err);
        this.error.set('No se pudo generar el reporte de ventas.');
        this.ventas.set([]);
        this.cargando.set(false);
      }
    });
  }

  exportarPdf(): void {
    this.exportando.set(true);
    this.reporteService.descargarReporteVentasPdf(this.fechaInicioActual, this.fechaFinActual).subscribe({
      next: (blob) => this.descargarBlob(blob, `reporte-ventas-${this.fechaInicioActual}-${this.fechaFinActual}.pdf`),
      error: () => {
        alert('No se pudo generar el reporte en PDF.');
        this.exportando.set(false);
      }
    });
  }

  exportarExcel(): void {
    this.exportando.set(true);
    this.reporteService.descargarReporteVentasExcel(this.fechaInicioActual, this.fechaFinActual).subscribe({
      next: (blob) => this.descargarBlob(blob, `reporte-ventas-${this.fechaInicioActual}-${this.fechaFinActual}.xlsx`),
      error: () => {
        alert('No se pudo generar el reporte en Excel.');
        this.exportando.set(false);
      }
    });
  }

  private descargarBlob(blob: Blob, nombreArchivo: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombreArchivo;
    a.click();
    URL.revokeObjectURL(url);
    this.exportando.set(false);
  }

  private aFormatoBackend(fechaIso: string): string {
    const [yyyy, mm, dd] = fechaIso.split('-');
    return `${dd}-${mm}-${yyyy}`;
  }

  volver(): void {
    this.router.navigate(['/ventas']);
  }
}
