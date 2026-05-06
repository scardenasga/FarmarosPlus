import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { VentaService } from '../../services/venta.service';
import { FilterButtonComponent } from '../../../shared/components/filter-button/filter-button.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { VentaListComponent } from '../../components/venta-list/venta-list.component';
import { SalesFilterComponent, SalesFilterOptions } from '../../components/sales-filter/sales-filter.component';

@Component({
  selector: 'app-historial-ventas',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FilterButtonComponent,
    FabButtonComponent,
    TopBarComponent,
    VentaListComponent,
    SalesFilterComponent
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
  isFilterVisible = signal<boolean>(false);

  ventasAgrupadas = computed(() => {
    // ... logic unchanged
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

  loadHistory(inicio?: string, fin?: string, vendedor?: string) {
    this.isLoading.set(true);
    this.ventaService.consultarHistorico(inicio, fin, undefined, undefined, 'admin').subscribe({
      next: (data) => {
        if (data.length > 0) {
          data[0].estado = 'COMPLETADA';
          if (data[1]) {
            data[1].estado = 'ANULADA';
          }
        }

        // Filtrado local por vendedor si es necesario (el servicio parece no filtrarlo por defecto según FiltrarHistorial original)
        let filtered = data;
        if (vendedor) {
          const v = vendedor.toLowerCase();
          filtered = data.filter((sale: any) =>
            sale.vendedorNombre?.toLowerCase().includes(v)
          );
        }

        this.ventas.set(filtered);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al cargar el historial');
        this.isLoading.set(false);
      }
    });
  }

  handleFilterApply(options: SalesFilterOptions) {
    const inicio = options.fechaInicio ? `${options.fechaInicio}T00:00:00` : undefined;
    const fin = options.fechaFin ? `${options.fechaFin}T23:59:59` : undefined;
    this.loadHistory(inicio, fin, options.vendedor || undefined);
  }

  toggleFilter() {
    this.isFilterVisible.update(v => !v);
  }

  verDetalle(id: number) {
    this.router.navigate(['/ventas', id]);
  }

  volver() {
    this.router.navigate(['/home']);
  }
// ... rest of methods

  handleNotification() {
    console.log("Mostrando Notificaciones.");
  }

  handleAddSale(): void {
    this.router.navigate(['/ventas/crear']);
  }
}
