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

  console.log("Intentando conectar con:", inicio, fin);

  this.ventaService.consultarHistorico(inicio, fin).subscribe({
    next: (data) => {
      console.log("PRIMERA VENTA:", JSON.stringify(data[0]));
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
      // AQUÍ ESTÁ EL PROBLEMA: El servidor responde 500
      console.error("EL SERVIDOR TIENE UN BUG (500):", err);
      
      this.errorMessage.set('El servidor de Java falló (Error 500)');
      this.ventas.set([]); // Vaciamos la lista para que no se quede el spinner infinito
      this.isLoading.set(false);
    }
  });
}

 handleFilterApply(options: SalesFilterOptions) {
  const formatearFecha = (fechaStr: string | null | undefined): string | undefined => {
    if (!fechaStr) return undefined;
    const partes = fechaStr.split('-'); 
    return `${partes[2]}-${partes[1]}-${partes[0]}`; 
  };

  // Aquí ya no debería dar error
  const inicio = formatearFecha(options.fechaInicio);
  const fin = formatearFecha(options.fechaFin);

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
