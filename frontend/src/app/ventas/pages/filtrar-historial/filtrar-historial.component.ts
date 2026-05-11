import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VentaService } from '../../services/venta.service';
import { InputFechaComponent } from '../../../shared/filtro-fecha/filtro-fecha.component';

@Component({
  selector: 'app-filtrar-historial',
  standalone: true,
  imports: [CommonModule, FormsModule, InputFechaComponent],
  templateUrl: './filtrar-historial.component.html',
  styleUrl: './filtrar-historial.component.css'
})
export class FiltrarHistorialComponent {
  fechaInicio: string = '';
  fechaFin: string = '';
  vendedor: string = '';
  cargando: boolean = false;
  error: string = '';

  constructor(private ventaService: VentaService, private router: Router) {}

  aplicar() {
  this.cargando = true;
  this.error = '';

  const formatear = (fecha: string) => {
    if (!fecha) return undefined;
    const [yyyy, mm, dd] = fecha.split('-');
    return `${dd}-${mm}-${yyyy}`;
  };

  const inicio = formatear(this.fechaInicio);
  const fin = formatear(this.fechaFin);

  this.ventaService.consultarHistorico(inicio, fin).subscribe({
    next: (data) => {
      sessionStorage.setItem('historialFiltrado', JSON.stringify(data));
      sessionStorage.setItem('historialFiltros', JSON.stringify({
        fechaInicio: this.fechaInicio,
        fechaFin: this.fechaFin,
        vendedor: this.vendedor
      }));
      this.router.navigate(['/ventas/historial']);
      this.cargando = false;
    },
    error: () => {
      this.error = 'Error al aplicar los filtros';
      this.cargando = false;
    }
  });
}

  limpiar() {
    this.fechaInicio = '';
    this.fechaFin = '';
    this.vendedor = '';
    sessionStorage.removeItem('historialFiltrado');
    sessionStorage.removeItem('historialFiltros');
  }

  volver() {
    this.router.navigate(['/ventas/historial']);
  }
}
