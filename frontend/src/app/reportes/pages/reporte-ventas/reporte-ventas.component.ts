import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PeriodFilterComponent, PeriodFilterValue } from '../../../shared/components/period-filter/period-filter.component';
import { EstadoVentaComponent } from '../../../shared/components/estado-venta/estado-venta.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { VentaService } from '../../../ventas/services/venta.service';
import {
  DetalleVenta,
  Venta,
  metodoPagoPrincipal,
  nombreVendedor
} from '../../../ventas/models/venta.model';
import { ReporteService } from '../../services/reporte.service';

interface ProductoAnalizado {
  nombre: string;
  unidades: number;
  ingresos: number;
}

@Component({
  selector: 'app-reporte-ventas',
  standalone: true,
  imports: [CommonModule, PeriodFilterComponent, EstadoVentaComponent],
  templateUrl: './reporte-ventas.component.html',
  styleUrl: './reporte-ventas.component.css'
})
export class ReporteVentasComponent implements OnInit {
  private ventaService = inject(VentaService);
  private reporteService = inject(ReporteService);
  private notificacion = inject(NotificacionService);
  private router = inject(Router);

  ventas = signal<Venta[]>([]);
  cargando = signal<boolean>(false);
  error = signal<string>('');
  exportando = signal<boolean>(false);

  private fechaInicioActual = '';
  private fechaFinActual = '';

  readonly vendedor = nombreVendedor;
  readonly metodo = metodoPagoPrincipal;

  /** Ingresos del período: solo ventas COMPLETADAS (igual criterio que el reporte PDF/Excel). */
  totalPeriodo = computed(() =>
    this.completadas().reduce((acc, v) => acc + (v.total || 0), 0)
  );

  /** Ventas con estado COMPLETADA: base de las métricas de ingreso. */
  private completadas = computed(() =>
    this.ventas().filter(v => String(v.estado).toUpperCase() === 'COMPLETADA')
  );

  /** Total del período dividido entre el número de transacciones. */
  ticketPromedio = computed(() => {
    const n = this.completadas().length;
    return n > 0 ? this.totalPeriodo() / n : 0;
  });

  anuladasCount = computed(() =>
    this.ventas().filter(v => String(v.estado).toUpperCase() === 'ANULADA').length
  );

  unidadesVendidas = computed(() =>
    this.completadas().reduce((acc, v) => acc + (v.detalles ?? []).reduce((s, d) => s + (d.cantidad || 0), 0), 0)
  );

  ivaRecaudado = computed(() =>
    this.completadas().reduce((acc, v) => acc + (v.detalles ?? []).reduce((s, d) => s + (d.ivaLinea || 0), 0), 0)
  );

  /** Productos agregados por nombre a partir de los detalles de las ventas completadas. */
  private productosAgregados = computed(() => {
    const mapa = new Map<string, ProductoAnalizado>();
    for (const v of this.completadas()) {
      for (const d of (v.detalles ?? []) as DetalleVenta[]) {
        const nombre = d.producto?.nombre || 'Producto';
        const actual = mapa.get(nombre) ?? { nombre, unidades: 0, ingresos: 0 };
        actual.unidades += d.cantidad || 0;
        actual.ingresos += d.subtotalLinea || 0;
        mapa.set(nombre, actual);
      }
    }
    return Array.from(mapa.values());
  });

  topProductos = computed(() =>
    [...this.productosAgregados()]
      .sort((a, b) => b.unidades - a.unidades || b.ingresos - a.ingresos)
      .slice(0, 5)
  );

  menosVendidos = computed(() =>
    [...this.productosAgregados()]
      .sort((a, b) => a.unidades - b.unidades || a.ingresos - b.ingresos)
      .slice(0, 5)
  );

  /** Ingresos agrupados por día (solo completadas). */
  private ingresosPorDia = computed(() => {
    const mapa = new Map<string, number>();
    for (const v of this.completadas()) {
      if (!v.fecha) continue;
      const dia = v.fecha.substring(0, 10); // yyyy-MM-dd
      mapa.set(dia, (mapa.get(dia) ?? 0) + (v.total || 0));
    }
    return new Map([...mapa.entries()].sort((a, b) => a[0].localeCompare(b[0])));
  });

  mejorDia = computed(() => {
    let mejor: { dia: string; total: number } | null = null;
    for (const [dia, total] of this.ingresosPorDia()) {
      if (!mejor || total > mejor.total) mejor = { dia, total };
    }
    return mejor;
  });

  promedioDiario = computed(() => {
    const dias = this.ingresosPorDia().size;
    return dias > 0 ? this.totalPeriodo() / dias : 0;
  });

  tendencia = computed(() => {
    const dias = [...this.ingresosPorDia().keys()];
    if (dias.length < 2) return null;

    const corte = dias[Math.floor(dias.length / 2)];
    let mitad1 = 0;
    let mitad2 = 0;
    for (const [dia, total] of this.ingresosPorDia()) {
      if (dia < corte) mitad1 += total;
      else mitad2 += total;
    }

    if (mitad1 <= 0) return mitad2 > 0 ? 100 : null;
    return ((mitad2 - mitad1) / mitad1) * 100;
  });

  /** Solapa activa en la card de análisis: top o menos vendidos. */
  solapaActiva = signal<'top' | 'menos'>('top');

  /* ---------- Paginación ---------- */
  readonly TAMANOS_PAGINA = [5, 10, 25, 50];
  tamanoPagina = signal<number>(5);
  pagina = signal<number>(1);

  totalPaginas = computed(() => Math.max(1, Math.ceil(this.ventas().length / this.tamanoPagina())));

  ventasPaginadas = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamanoPagina();
    return this.ventas().slice(inicio, inicio + this.tamanoPagina());
  });

  rangoMostrado = computed(() => {
    if (!this.ventas().length) return '0 de 0';
    const inicio = (this.pagina() - 1) * this.tamanoPagina() + 1;
    const fin = Math.min(this.pagina() * this.tamanoPagina(), this.ventas().length);
    return `${inicio}–${fin} de ${this.ventas().length}`;
  });

  cambiarPagina(nueva: number): void {
    const destino = Math.min(Math.max(1, nueva), this.totalPaginas());
    if (destino !== this.pagina()) {
      this.pagina.set(destino);
    }
  }

  cambiarTamanoPagina(tamano: string | number): void {
    this.tamanoPagina.set(Number(tamano));
    this.pagina.set(1);
  }

  /** Lista de números de página con elipsis: 1 … 4 5 6 … 20 */
  paginasVisibles = computed(() => {
    const total = this.totalPaginas();
    const actual = this.pagina();
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    const paginas: (number | '...')[] = [1];
    let desde = Math.max(2, actual - 1);
    let hasta = Math.min(total - 1, actual + 1);
    if (desde > 2) paginas.push('...');
    for (let i = desde; i <= hasta; i++) paginas.push(i);
    if (hasta < total - 1) paginas.push('...');
    paginas.push(total);
    return paginas;
  });

  productosSolapa = computed(() =>
    this.solapaActiva() === 'top' ? this.topProductos() : this.menosVendidos()
  );

  /** Máximo de unidades de la solapa activa, para escalar las barras. */
  maxUnidadesSolapa = computed(() => {
    const lista = this.productosSolapa();
    return lista.length ? Math.max(...lista.map(p => p.unidades), 1) : 1;
  });

  cambiarSolapa(solapa: 'top' | 'menos'): void {
    this.solapaActiva.set(solapa);
  }

  formatearDia(diaIso: string): string {
    const [y, m, d] = diaIso.split('-');
    return `${d}/${m}/${y}`;
  }

  ngOnInit(): void {}

  handleFiltroChange(filtro: PeriodFilterValue): void {
    this.fechaInicioActual = filtro.fechaInicio;
    this.fechaFinActual = filtro.fechaFin;
    this.pagina.set(1);

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
        this.notificacion.error('No se pudo generar el reporte de ventas.');
        this.ventas.set([]);
        this.cargando.set(false);
      }
    });
  }

  exportarPdf(): void {
    this.exportando.set(true);
    this.reporteService.descargarReporteVentasPdf(this.fechaInicioActual, this.fechaFinActual).subscribe({
      next: (blob) => {
        this.descargarBlob(blob, `reporte-ventas-${this.fechaInicioActual}-${this.fechaFinActual}.pdf`);
        this.notificacion.exito('Reporte PDF descargado');
      },
      error: () => {
        this.notificacion.error('No se pudo generar el reporte en PDF.');
        this.exportando.set(false);
      }
    });
  }

  exportarExcel(): void {
    this.exportando.set(true);
    this.reporteService.descargarReporteVentasExcel(this.fechaInicioActual, this.fechaFinActual).subscribe({
      next: (blob) => {
        this.descargarBlob(blob, `reporte-ventas-${this.fechaInicioActual}-${this.fechaFinActual}.xlsx`);
        this.notificacion.exito('Reporte Excel descargado');
      },
      error: () => {
        this.notificacion.error('No se pudo generar el reporte en Excel.');
        this.exportando.set(false);
      }
    });
  }

  volver(): void {
    this.router.navigate(['/ventas']);
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
}
