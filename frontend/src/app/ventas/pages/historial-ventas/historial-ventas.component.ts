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
  this.errorMessage.set('');

  this.ventaService.consultarHistorico(inicio, fin).subscribe({
    next: (data) => {
      // Si el vendedor viene en el filtro, filtramos localmente para que funcione sí o sí
      let finalData = data || [];
      if (vendedor) {
        finalData = finalData.filter((v: any) => 
          (v.vendedorNombre || v.usuario?.nombreCompleto || '')
          .toLowerCase().includes(vendedor.toLowerCase())
        );
      }
      
      this.ventas.set(finalData);
      this.isLoading.set(false);
    },
    error: (err) => {
      console.error("Error cargando historial:", err);
      this.errorMessage.set('No se pudo cargar el historial de ventas.');
      this.ventas.set([]);
      this.isLoading.set(false);
    }
  });
}

 handleFilterApply(options: SalesFilterOptions) {
  const inicio = options.fechaInicio || undefined;
  const fin = options.fechaFin || undefined;
  this.loadHistory(inicio, fin, options.vendedor || undefined);
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
  toggleFilter() {
    this.isFilterVisible.update(v => !v);
  }
}
