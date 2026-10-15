import { Component, OnInit, output } from '@angular/core';
import { CommonModule } from '@angular/common';

export type Periodo = 'DIA' | 'SEMANA' | 'MES' | 'PERSONALIZADO';

export interface PeriodFilterValue {
  periodo: Periodo;
  fechaInicio: string;
  fechaFin: string;
}

/**
 * Filtro de período reutilizable para reportes: día, semana, mes o rango
 * personalizado. Emite automáticamente al cambiar la selección, validando
 * que la fecha de inicio no sea posterior a la fecha de fin.
 */
@Component({
  selector: 'app-period-filter',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './period-filter.component.html',
  styleUrl: './period-filter.component.css'
})
export class PeriodFilterComponent implements OnInit {
  periodo: Periodo = 'MES';
  fechaInicioPersonalizada: string = '';
  fechaFinPersonalizada: string = '';
  error: string = '';

  private ultimoRango: PeriodFilterValue | null = null;

  filtroChange = output<PeriodFilterValue>();

  ngOnInit(): void {
    this.aplicarPeriodoRapido('MES');
  }

  seleccionarPeriodo(periodo: Periodo): void {
    if (periodo === 'PERSONALIZADO') {
      this.periodo = 'PERSONALIZADO';
      this.error = '';
      return;
    }
    this.aplicarPeriodoRapido(periodo);
  }

  onFechaInicioChange(fecha: string): void {
    this.fechaInicioPersonalizada = fecha;
    this.emitirRangoPersonalizado();
  }

  onFechaFinChange(fecha: string): void {
    this.fechaFinPersonalizada = fecha;
    this.emitirRangoPersonalizado();
  }

  /** Texto descriptivo del rango activo para los períodos rápidos. */
  rangoAplicadoTexto(): string {
    if (!this.ultimoRango) return '';
    const f1 = this.formatear(this.ultimoRango.fechaInicio);
    const f2 = this.formatear(this.ultimoRango.fechaFin);
    return `Mostrando del ${f1} al ${f2}`;
  }

  private aplicarPeriodoRapido(periodo: Periodo): void {
    this.periodo = periodo;
    this.error = '';

    const hoy = new Date();
    let inicio: Date;
    let fin: Date;

    switch (periodo) {
      case 'DIA':
        inicio = hoy;
        fin = hoy;
        break;
      case 'SEMANA': {
        const diaSemana = (hoy.getDay() + 6) % 7; // 0 = lunes
        inicio = new Date(hoy);
        inicio.setDate(hoy.getDate() - diaSemana);
        fin = new Date(inicio);
        fin.setDate(inicio.getDate() + 6);
        break;
      }
      case 'MES':
      default:
        inicio = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
        fin = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
        break;
    }

    this.emitir({
      periodo,
      fechaInicio: this.aIso(inicio),
      fechaFin: this.aIso(fin)
    });
  }

  private emitirRangoPersonalizado(): void {
    if (!this.fechaInicioPersonalizada || !this.fechaFinPersonalizada) {
      return;
    }
    if (this.fechaInicioPersonalizada > this.fechaFinPersonalizada) {
      this.error = 'La fecha de inicio no puede ser posterior a la fecha de fin';
      return;
    }
    this.error = '';
    this.emitir({
      periodo: 'PERSONALIZADO',
      fechaInicio: this.fechaInicioPersonalizada,
      fechaFin: this.fechaFinPersonalizada
    });
  }

  private emitir(valor: PeriodFilterValue): void {
    this.ultimoRango = valor;
    this.filtroChange.emit(valor);
  }

  private formatear(fechaIso: string): string {
    const [y, m, d] = fechaIso.split('-');
    return `${d}/${m}/${y}`;
  }

  private aIso(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
