import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { DashboardService } from '../../../dashboard/services/dashboard.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { EstadoVentaComponent } from '../../../shared/components/estado-venta/estado-venta.component';
import { VentaDetallePanelComponent } from '../../components/venta-detalle-panel/venta-detalle-panel.component';
import { VentaService } from '../../services/venta.service';
import {
  EstadoVenta,
  Venta,
  metodoPagoPrincipal,
  nombreVendedor
} from '../../models/venta.model';

interface FiltrosActivos {
  fechaInicio: string | null;
  fechaFin: string | null;
  vendedor: string | null;
  estado: string | null;
  metodo: string | null;
}

const FILTROS_INICIALES: FiltrosActivos = {
  fechaInicio: null,
  fechaFin: null,
  vendedor: null,
  estado: null,
  metodo: null
};

@Component({
  selector: 'app-historial-ventas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SearchBarComponent,
    EstadoVentaComponent,
    VentaDetallePanelComponent
  ],
  templateUrl: './historial-ventas.component.html',
  styleUrl: './historial-ventas.component.css'
})
export class HistorialVentasComponent implements OnInit {
  private ventaService = inject(VentaService);
  private dashboardService = inject(DashboardService);
  private router = inject(Router);

  ventas = signal<Venta[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');

  ventasDelDia = signal<number>(0);
  cantidadVentasDelDia = signal<number>(0);
  ventasDelMes = signal<number>(0);

  busqueda = signal<string>('');
  filtros = signal<FiltrosActivos>({ ...FILTROS_INICIALES });
  panelFiltrosAbierto = signal<boolean>(false);

  // Campos del formulario de filtros (ngModel)
  filtroFechaInicio: string | null = null;
  filtroFechaFin: string | null = null;
  filtroVendedor: string | null = null;
  filtroEstado: string | null = null;

  readonly nombreVendedor = nombreVendedor;
  readonly metodo = metodoPagoPrincipal;

  metodosPago = ['EFECTIVO', 'TARJETA', 'TRANSFERENCIA'];
  estadosVenta: (EstadoVenta | string)[] = ['COMPLETADA', 'ANULADA'];

  get filtrosCount(): number {
    const f = this.filtros();
    return [f.fechaInicio, f.fechaFin, f.vendedor, f.estado]
      .filter(v => !!v).length + (f.metodo ? 1 : 0);
  }

  ventasFiltradas = computed(() => {
    const termino = this.busqueda().toLowerCase().trim();
    const f = this.filtros();

    return this.ventas().filter(v => {
      if (termino) {
        const idTexto = String(v.id);
        const nombre = nombreVendedor(v).toLowerCase();
        if (!idTexto.includes(termino) && !nombre.includes(termino)) return false;
      }
      if (f.metodo && !v.pagos?.some(p => p.tipo === f.metodo)) return false;
      return true;
    });
  });

  constructor() {
    // Al volver del POS tras registrar una venta, se abre su detalle.
    const destacada = (this.router.getCurrentNavigation()?.extras?.state as
      { ventaDestacada?: number } | undefined)?.ventaDestacada;
    if (destacada) {
      setTimeout(() => {
        this.panelVentaId.set(destacada);
        this.panelVisible.set(true);
      });
    }
  }

  ngOnInit() {
    this.cargarKpis();
    this.cargarHistorial();
  }

  cargarKpis() {
    this.dashboardService.obtenerDashboard().subscribe({
      next: (data) => {
        this.ventasDelDia.set(data.resumen?.ventasDelDia ?? 0);
        this.cantidadVentasDelDia.set(data.resumen?.cantidadVentasDelDia ?? 0);
        this.ventasDelMes.set(data.resumen?.ventasDelMes ?? 0);
      },
      error: () => {
        // Los KPIs no bloquean el historial; quedan en 0.
      }
    });
  }

  cargarHistorial() {
    const f = this.filtros();
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.ventaService
      .consultarHistorico(f.fechaInicio ?? undefined, f.fechaFin ?? undefined, f.estado ?? undefined)
      .subscribe({
        next: (data) => {
          let finalData = data || [];
          if (f.vendedor) {
            finalData = finalData.filter(v =>
              nombreVendedor(v).toLowerCase().includes(f.vendedor!.toLowerCase())
            );
          }
          this.ventas.set(finalData);
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('No se pudo cargar el historial de ventas.');
          this.ventas.set([]);
          this.isLoading.set(false);
        }
      });
  }

  onBuscar(termino: string) {
    this.busqueda.set(termino);
  }

  /* ---------- Filtros ---------- */

  togglePanelFiltros() {
    this.panelFiltrosAbierto.update(v => !v);
  }

  aplicarFiltros() {
    this.filtros.set({
      fechaInicio: this.filtroFechaInicio || null,
      fechaFin: this.filtroFechaFin || null,
      vendedor: this.filtroVendedor || null,
      estado: this.filtroEstado || null,
      metodo: this.filtros().metodo
    });
    this.cargarHistorial();
  }

  limpiarFiltros() {
    this.filtroFechaInicio = null;
    this.filtroFechaFin = null;
    this.filtroVendedor = null;
    this.filtroEstado = null;
    this.filtros.set({ ...this.filtros(), ...{
      fechaInicio: null, fechaFin: null, vendedor: null, estado: null
    }});
    this.cargarHistorial();
  }

  seleccionarMetodo(metodo: string | null) {
    this.filtros.update(f => ({ ...f, metodo }));
  }

  quitarFecha(fecha: 'fechaInicio' | 'fechaFin') {
    this.filtros.update(f => ({ ...f, [fecha]: null }));
    if (fecha === 'fechaInicio') this.filtroFechaInicio = null;
    else this.filtroFechaFin = null;
    this.cargarHistorial();
  }

  /* ---------- Panel de detalle ---------- */

  panelVentaId = signal<number | null>(null);
  panelVisible = signal<boolean>(false);

  verDetalle(id: number) {
    this.panelVentaId.set(id);
    this.panelVisible.set(true);
  }

  cerrarPanel() {
    this.panelVisible.set(false);
  }

  onVentaAnulada(actualizada: Venta) {
    this.ventas.update(lista => lista.map(v =>
      v.id === actualizada.id ? actualizada : v
    ));
    this.cargarKpis();
  }

  handleAddSale(): void {
    this.router.navigate(['/ventas/pos']);
  }

  irAReportes(): void {
    this.router.navigate(['/reportes/ventas']);
  }
}
