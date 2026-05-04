import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VentaService } from '../../../services/venta.service';
// IMPORTA EL NUEVO COMPONENTE
import { InputFechaComponent } from '../../../shared/components/filtro-fecha/filtro-fecha.component';

@Component({
  selector: 'app-filtrar-historial',
  standalone: true,
  // AÑÁDELO AQUÍ
  imports: [CommonModule, FormsModule, InputFechaComponent],
  templateUrl: './filtrar-historial.component.html',
  styleUrl: './filtrar-historial.component.css'
})
export class FiltrarHistorialComponent {
  // Inicializamos con fechas por defecto si quieres, o vacías
  fechaInicio: string = ''; 
  fechaFin: string = '';
  vendedor: string = '';
  cargando: boolean = false;
  error: string = '';

  constructor(private ventaService: VentaService, private router: Router) {}

  aplicar() {
    this.cargando = true;
    this.error = '';

    const inicio = this.fechaInicio ? `${this.fechaInicio}T00:00:00` : undefined;
    const fin = this.fechaFin ? `${this.fechaFin}T23:59:59` : undefined;

    this.ventaService.consultarHistorico(inicio, fin, undefined, undefined, 'admin').subscribe({
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