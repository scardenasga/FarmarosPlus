import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router'; // Añadido RouterModule
import { VentaService } from '../../../services/venta.service';

@Component({
  selector: 'app-historial-ventas',
  standalone: true,
  imports: [CommonModule, RouterModule],
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
   
    this.ventaService.consultarHistorico(undefined, undefined, undefined, undefined, 'admin').subscribe({
      next: (data) => {
        this.ventas = data;
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
      const fecha = new Date(v.fecha).toLocaleDateString('es-CO', {
        day: '2-digit', month: '2-digit', year: 'numeric'
      });
      if (!mapa.has(fecha)) mapa.set(fecha, []);
      mapa.get(fecha)!.push(v);
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