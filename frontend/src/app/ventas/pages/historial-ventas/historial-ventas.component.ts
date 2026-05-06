import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { VentaService } from '../../services/venta.service';
import { BotonFiltroComponent } from '../../../shared/components/boton-filtro/boton-filtro.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { VentaListComponent } from '../../components/venta-list/venta-list.component';

@Component({
  selector: 'app-historial-ventas',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    BotonFiltroComponent,
    FabButtonComponent,
    TopBarComponent,
    VentaListComponent
  ],
  templateUrl: './historial-ventas.component.html',
  styleUrl: './historial-ventas.component.css'
})
export class HistorialVentasComponent implements OnInit {
  private ventaService = inject(VentaService);
  private router = inject(Router);

  ventas = signal<any[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');

  ventasAgrupadas = computed(() => {
    const mapa = new Map<string, any[]>();
    for (const v of this.ventas()) {
      const fechaObj = new Date(v.fecha);
      const fechaKey = fechaObj.toLocaleDateString('es-CO', {
        day: '2-digit', month: '2-digit', year: 'numeric'
      });

      if (!mapa.has(fechaKey)) mapa.set(fechaKey, []);
      mapa.get(fechaKey)!.push(v);
    }
    return Array.from(mapa.entries()).map(([fecha, ventas]) => ({ fecha, ventas }));
  });

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.isLoading.set(true);
    this.ventaService.consultarHistorico(undefined, undefined, undefined, undefined, 'admin').subscribe({
      next: (data) => {
        if (data.length > 0) {
          data[0].estado = 'COMPLETADA';
          if (data[1]) {
            data[1].estado = 'ANULADA';
          }
        }
        this.ventas.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al cargar el historial');
        this.isLoading.set(false);
      }
    });
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

  handleNotification() {
    console.log("Mostrando Notificaciones.");
  }

  handleAddSale(): void {
    this.router.navigate(['/ventas/crear']);
  }
}
