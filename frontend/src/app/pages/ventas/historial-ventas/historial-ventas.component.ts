import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { VentaService } from '../../../services/venta.service';
import { BotonNuevoRegistroComponent } from '../../../shared/boton-nuevo-registro/boton-nuevo-registro.component';
import { BotonFiltroComponent } from '../../../shared/boton-filtro/boton-filtro.component';
import { EstadoVentaComponent } from '../../../shared/estado-venta/estado-venta.component';

@Component({
  selector: 'app-historial-ventas', 
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    BotonNuevoRegistroComponent, 
    BotonFiltroComponent, 
    EstadoVentaComponent 
  ],
  templateUrl: './historial-ventas.component.html',
  styleUrl: './historial-ventas.component.css'
})
export class HistorialVentasComponent implements OnInit {

  ventas: any[] = [];
  ventasAgrupadas: { fecha: string; ventas: any[] }[] = [];
  cargando: boolean = true;
  error: string = '';

  constructor(private ventaService: VentaService, private router: Router) {}

  ngOnInit() {
    // Consultamos el histórico
    this.ventaService.consultarHistorico(undefined, undefined, undefined, undefined, 'admin').subscribe({
      next: (data) => {
        this.ventas = data;

        // --- LÓGICA DE PRUEBA (Para ver los colores del componente) ---
        if (this.ventas.length > 0) {
          // Forzamos que la primera sea Completada y la segunda Anulada para probar
          this.ventas[0].estado = 'COMPLETADA'; 
          if (this.ventas[1]) {
            this.ventas[1].estado = 'ANULADA';
          }
        }
        // ---------------------------------------------------------------

        this.agruparPorFecha();
        this.cargando = false;
      },
      error: () => {
        this.error = 'Error al cargar el historial';
        this.cargando = false;
      }
    });
  }

  agruparPorFecha() {
    const mapa = new Map<string, any[]>();
    for (const v of this.ventas) {
      // Usamos la fecha de la venta para agrupar
      const fechaObj = new Date(v.fecha);
      const fechaKey = fechaObj.toLocaleDateString('es-CO', {
        day: '2-digit', month: '2-digit', year: 'numeric'
      });
      
      if (!mapa.has(fechaKey)) mapa.set(fechaKey, []);
      mapa.get(fechaKey)!.push(v);
    }
    this.ventasAgrupadas = Array.from(mapa.entries()).map(([fecha, ventas]) => ({ fecha, ventas }));
  }

  verDetalle(id: number) {
    this.router.navigate(['/ventas', id]);
  }

  irAFiltrar() {
    this.router.navigate(['/ventas/historial/filtrar']);
  }

  volver() {
    this.router.navigate(['/home']); 
  }
}