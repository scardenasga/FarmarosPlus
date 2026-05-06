import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
// Ruta corregida para llegar a app/services/compra.service.ts
import { CompraService } from '../../../ventas/services/compra.service'
// Ruta corregida para llegar a app/shared/components/bottom-nav/...
import { BottomNavBarComponent } from '../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';

interface AlertaDetallada {
  idOrden: number;
  codigoGenerado: string;
  proveedorNombre: string;
  estadoActual: string;
  fechaCreacionFormateada: string;
  diasTranscurridos: number;
}

@Component({
  selector: 'app-notificacion-pedidos',
  standalone: true,
  imports: [CommonModule, BottomNavBarComponent, SearchBarComponent],
  templateUrl: './notificacion-pedidos.component.html',
  styleUrl: './notificacion-pedidos.component.css'
})
export class NotificacionPedidosComponent implements OnInit {
  alertas: AlertaDetallada[] = [];
  alertasFiltradas: AlertaDetallada[] = [];

  constructor(private compraService: CompraService) {}

  ngOnInit(): void {
    this.cargarNotificaciones();
  }

  cargarNotificaciones(): void {
    this.compraService.getResumenSeguimiento().subscribe({
      next: (data) => {
        // Solo guardamos los que el backend marque como pendientes
        this.alertas = data.alertasDetalladas.filter((a: any) => a.estadoActual === 'PENDIENTE');
        this.alertasFiltradas = [...this.alertas];
      },
      error: (err) => console.error('Error:', err)
    });
  }

  onSearch(termino: string): void {
    const t = termino.toLowerCase();
    this.alertasFiltradas = this.alertas.filter(a =>
      a.codigoGenerado.toLowerCase().includes(t) ||
      a.proveedorNombre.toLowerCase().includes(t)
    );
  }
}
