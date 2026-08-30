import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { DashboardService } from '../../../dashboard/services/dashboard.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { EstadoVentaComponent } from '../../../shared/components/estado-venta/estado-venta.component';
import { VentaDetallePanelComponent } from '../../components/venta-detalle-panel/venta-detalle-panel.component';
import { VentaService } from '../../services/venta.service';
import { SesionService } from '../../../shared/services/sesion.service';
import {
  Venta,
  metodoPagoPrincipal,
  nombreVendedor
} from '../../models/venta.model';

interface FiltrosActivos {
  estado: string | null;
  metodo: string | null;
}

const FILTROS_INICIALES: FiltrosActivos = {
  estado: null,
  metodo: null
};

@Component({
  selector: 'app-historial-ventas',
  standalone: true,
  imports: [
    CommonModule,
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
  private sesion = inject(SesionService);
  esAdmin = this.sesion.esAdmin;

  ventas = signal<Venta[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');

  ventasDelDia = signal<number>(0);
  cantidadVentasDelDia = signal<number>(0);
  ventasDelMes = signal<number>(0);

  busqueda = signal<string>('');
  filtros = signal<FiltrosActivos>({ ...FILTROS_INICIALES });
  /**
   * Esta pantalla muestra únicamente las transacciones del día en curso.
   * Es fijo: para analizar otros períodos se usa el módulo de reportes.
   */
  private readonly soloHoy = true;

  /* ---------- Paginación ---------- */
  readonly TAMANOS_PAGINA = [5, 10, 25, 50];
  tamanoPagina = signal<number>(5);
  pagina = signal<number>(1);

  totalPaginas = computed(() => Math.max(1, Math.ceil(this.ventasFiltradas().length / this.tamanoPagina())));

  ventasPaginadas = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamanoPagina();
    return this.ventasFiltradas().slice(inicio, inicio + this.tamanoPagina());
  });

  rangoMostrado = computed(() => {
    if (!this.ventasFiltradas().length) return '0 de 0';
    const inicio = (this.pagina() - 1) * this.tamanoPagina() + 1;
    const fin = Math.min(this.pagina() * this.tamanoPagina(), this.ventasFiltradas().length);
    return `${inicio}–${fin} de ${this.ventasFiltradas().length}`;
  });

  cambiarPagina(nueva: number): void {
    const destino = Math.min(Math.max(1, nueva), this.totalPaginas());
    if (destino !== this.pagina()) {
      this.pagina.set(destino);
    }
  }

  cambiarTamanoPagina(tamano: string | number): void {
    this.tamanoPagina.set(Number(tamano));
    this.pagina.set(1);
  }

  paginasVisibles = computed(() => {
    const total = this.totalPaginas();
    const actual = this.pagina();
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    const paginas: (number | '...')[] = [1];
    const desde = Math.max(2, actual - 1);
    const hasta = Math.min(total - 1, actual + 1);
    if (desde > 2) paginas.push('...');
    for (let i = desde; i <= hasta; i++) paginas.push(i);
    if (hasta < total - 1) paginas.push('...');
    paginas.push(total);
    return paginas;
  });

  private hoyIso(): string {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${dia}`;
  }

  readonly nombreVendedor = nombreVendedor;
  readonly metodo = metodoPagoPrincipal;

  metodosPago = ['EFECTIVO', 'TARJETA', 'TRANSFERENCIA'];

  ventasFiltradas = computed(() => {
    const termino = this.busqueda().toLowerCase().trim();
    const f = this.filtros();
    const hoy = this.hoyIso();

    return this.ventas().filter(v => {
      if (this.soloHoy && !(v.fecha ?? '').startsWith(hoy)) return false;
      if (termino) {
        const idTexto = String(v.id);
        const nombre = nombreVendedor(v).toLowerCase();
        if (!idTexto.includes(termino) && !nombre.includes(termino)) return false;
      }
      if (f.metodo && !v.pagos?.some(p => p.tipo === f.metodo)) return false;
      if (f.estado && String(v.estado).toUpperCase() !== f.estado) return false;
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

    // Sin filtro de fechas en esta pantalla: se carga el histórico completo
    // y "Solo hoy" filtra localmente por el día actual.
    this.ventaService
      .consultarHistorico(undefined, undefined, f.estado ?? undefined)
      .subscribe({
        next: (data) => {
          this.ventas.set(data || []);
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
    this.pagina.set(1);
  }

  /* ---------- Filtros (método y estado; las fechas se manejan con "Solo hoy") ---------- */

  seleccionarMetodo(metodo: string | null) {
    this.filtros.update(f => ({ ...f, metodo }));
    this.pagina.set(1);
  }

  seleccionarEstado(estado: string | null) {
    this.filtros.update(f => ({ ...f, estado }));
    this.pagina.set(1);
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
