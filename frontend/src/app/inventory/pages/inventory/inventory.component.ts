import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { ProductoFormularioPanelComponent } from '../../components/producto-formulario-panel/producto-formulario-panel.component';
import { IngresoStockPanelComponent } from '../../components/ingreso-stock-panel/ingreso-stock-panel.component';
import { CategoriasPanelComponent } from '../../components/categorias-panel/categorias-panel.component';
import { InventoryService } from '../../services/inventory.service';
import { CategoriaService } from '../../services/categoria.service';
import {
  Categoria,
  ProductoDetalleResponse,
  Product,
  TendenciaProducto
} from '../../models/product.model';

type VistaMode = 'grid' | 'tabla';

const CLAVE_VISTA = 'inventario.vista';
const CLAVE_TENDENCIA_DIAS = 'inventario.tendenciaDias';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, SearchBarComponent, ConfirmationDialogComponent, ProductoFormularioPanelComponent, IngresoStockPanelComponent, CategoriasPanelComponent],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css'
})
export class InventoryComponent implements OnInit {
  private inventoryService = inject(InventoryService);
  private categoriaService = inject(CategoriaService);
  private notificacion = inject(NotificacionService);
  private router = inject(Router);

  productos = signal<Product[]>([]);
  categorias = signal<Categoria[]>([]);
  tendencias = signal<Map<number, number>>(new Map());
  cargando = signal<boolean>(true);

  categoriaSeleccionada = signal<number | null>(null);
  searchTerm = signal<string>('');

  /** Filtro por estado: ACTIVO por defecto; TODOS muestra también inactivos/descontinuados. */
  estadoFiltro = signal<'ACTIVO' | 'INACTIVO' | 'DESCONTINUADO' | 'TODOS'>('ACTIVO');
  readonly opcionesEstado = [
    { valor: 'ACTIVO', etiqueta: 'Activos' },
    { valor: 'INACTIVO', etiqueta: 'Inactivos' },
    { valor: 'DESCONTINUADO', etiqueta: 'Descont.' },
    { valor: 'TODOS', etiqueta: 'Todos' }
  ] as const;

  seleccionarEstado(valor: 'ACTIVO' | 'INACTIVO' | 'DESCONTINUADO' | 'TODOS'): void {
    this.estadoFiltro.set(valor);
    this.pagina.set(1);
  }

  vista = signal<VistaMode>(this.cargarVista());
  tendenciaDias = signal<number>(this.cargarTendenciaDias());
  readonly opcionesTendencia = [7, 30, 90];

  /* ---------- Selección ---------- */
  seleccionados = signal<Set<number>>(new Set());
  cambiandoEstado = signal<boolean>(false);

  /* ---------- Panel de detalle ---------- */
  panelVisible = signal<boolean>(false);
  detalleProducto = signal<ProductoDetalleResponse | null>(null);
  detalleCargando = signal<boolean>(false);
  trendDetalle = computed(() => this.trendDe(this.detalleProducto()?.id ?? null));

  /* ---------- Paginación ---------- */
  readonly TAMANOS_PAGINA = [5, 10, 25, 50];
  tamanoPagina = signal<number>(10);
  pagina = signal<number>(1);

  totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.productosFiltrados().length / this.tamanoPagina()))
  );

  productosPaginados = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamanoPagina();
    return this.productosFiltrados().slice(inicio, inicio + this.tamanoPagina());
  });

  rangoMostrado = computed(() => {
    const total = this.productosFiltrados().length;
    if (!total) return '0 de 0';
    const inicio = (this.pagina() - 1) * this.tamanoPagina() + 1;
    const fin = Math.min(this.pagina() * this.tamanoPagina(), total);
    return `${inicio}–${fin} de ${total}`;
  });

  paginasVisibles = computed(() => {
    const total = this.totalPaginas();
    const actual = this.pagina();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const paginas: (number | '...')[] = [1];
    const desde = Math.max(2, actual - 1);
    const hasta = Math.min(total - 1, actual + 1);
    if (desde > 2) paginas.push('...');
    for (let i = desde; i <= hasta; i++) paginas.push(i);
    if (hasta < total - 1) paginas.push('...');
    paginas.push(total);
    return paginas;
  });

  productosFiltrados = computed(() => {
    const termino = this.searchTerm().toLowerCase().trim();
    const catId = this.categoriaSeleccionada();
    const estado = this.estadoFiltro();

    return this.productos().filter(p => {
      if (estado !== 'TODOS' && String(p.estado).toUpperCase() !== estado) return false;
      if (catId !== null && p.categoria?.id !== catId) return false;
      if (termino) {
        const coincide =
          p.nombre.toLowerCase().includes(termino) ||
          p.codigoBarras.includes(termino) ||
          (p.descripcion ?? '').toLowerCase().includes(termino);
        if (!coincide) return false;
      }
      return true;
    });
  });

  ngOnInit(): void {
    this.cargarProductos();
    this.cargarCategorias();
    this.cargarTendencias();
  }

  cargarProductos(): void {
    // Carga TODOS los productos para permitir filtrar por estado en cliente.
    this.inventoryService.getAllProducts().subscribe({
      next: (products) => {
        this.productos.set(products ?? []);
        this.cargando.set(false);
      },
      error: () => {
        this.notificacion.error('No se pudo cargar el inventario.');
        this.cargando.set(false);
      }
    });
  }

  cargarCategorias(): void {
    this.categoriaService.listar().subscribe(cats => this.categorias.set(cats ?? []));
  }

  cargarTendencias(): void {
    this.inventoryService.getTendencias(this.tendenciaDias()).subscribe({
      next: (lista) => {
        const mapa = new Map<number, number>();
        for (const t of lista ?? []) mapa.set(t.idProducto, t.porcentajeCambio);
        this.tendencias.set(mapa);
      },
      error: () => this.tendencias.set(new Map())
    });
  }

  cambiarPeriodoTendencia(dias: number): void {
    this.tendenciaDias.set(dias);
    localStorage.setItem(CLAVE_TENDENCIA_DIAS, String(dias));
    this.cargarTendencias();
  }

  aNumero(valor: unknown): number {
    return Number(valor);
  }

  cambiarVista(vista: VistaMode): void {
    this.vista.set(vista);
    localStorage.setItem(CLAVE_VISTA, vista);
  }

  private cargarVista(): VistaMode {
    return localStorage.getItem(CLAVE_VISTA) === 'tabla' ? 'tabla' : 'grid';
  }

  private cargarTendenciaDias(): number {
    const guardado = Number(localStorage.getItem(CLAVE_TENDENCIA_DIAS));
    return [7, 30, 90].includes(guardado) ? guardado : 30;
  }

  /* ---------- Filtros ---------- */

  onBuscar(termino: string): void {
    this.searchTerm.set(termino);
    this.pagina.set(1);
  }

  seleccionarCategoria(id: number | null): void {
    this.categoriaSeleccionada.set(id);
    this.pagina.set(1);
  }

  /* ---------- Paginación ---------- */

  cambiarPagina(nueva: number): void {
    const destino = Math.min(Math.max(1, nueva), this.totalPaginas());
    if (destino !== this.pagina()) this.pagina.set(destino);
  }

  cambiarTamanoPagina(tamano: string | number): void {
    this.tamanoPagina.set(Number(tamano));
    this.pagina.set(1);
  }

  /* ---------- Tendencia ---------- */

  trendDe(productoId: number | null | undefined): number | null {
    if (productoId == null) return null;
    return this.tendencias().get(productoId) ?? null;
  }

  /* ---------- Stock / estado visual ---------- */

  claseStock(p: Product): string {
    if (p.stockActual <= 0) return 'stock-agotado';
    if (p.stockActual <= p.stockMinimo) return 'stock-bajo';
    return 'stock-ok';
  }

  etiquetaStock(p: Product): string {
    if (p.stockActual <= 0) return 'AGOTADO';
    return `${p.stockActual} uds`;
  }

  descripcionCorta(p: Product): string {
    if (p.descripcion && p.descripcion.trim()) return p.descripcion;
    return p.categoria?.nombre ?? '';
  }

  /** Patrón determinístico de "código de barras" decorativo a partir del código. */
  patronBarras(codigo: string): number[] {
    let semilla = 0;
    for (let i = 0; i < codigo.length; i++) {
      semilla = (semilla * 31 + codigo.charCodeAt(i)) % 100000;
    }
    const barras: number[] = [];
    for (let i = 0; i < 18; i++) {
      semilla = (semilla * 1103515245 + 12345) % 2147483648;
      barras.push(1 + ((semilla >> 8) % 3));
    }
    return barras;
  }

  /* ---------- Detalle (panel derecho) ---------- */

  abrirDetalle(p: Product): void {
    this.panelVisible.set(true);
    this.detalleCargando.set(true);
    this.detalleProducto.set(null);

    this.inventoryService.getProductDetail(p.id).subscribe({
      next: (detalle) => {
        this.detalleProducto.set(detalle);
        this.detalleCargando.set(false);
      },
      error: () => {
        this.notificacion.error('No se pudo cargar el detalle del producto.');
        this.detalleCargando.set(false);
      }
    });
  }

  cerrarDetalle(): void {
    this.panelVisible.set(false);
  }

  /* ---------- Selección y acciones masivas ---------- */

  estaSeleccionado(id: number): boolean {
    return this.seleccionados().has(id);
  }

  get haySeleccion(): boolean {
    return this.seleccionados().size > 0;
  }

  toggleSeleccion(id: number, event: Event): void {
    event.stopPropagation();
    this.seleccionados.update(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  toggleTodos(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (!checked) {
      this.seleccionados.set(new Set());
      return;
    }
    // Solo se pueden seleccionar los visibles en la página actual.
    this.seleccionados.set(new Set(this.productosPaginados().map(p => p.id)));
  }

  get todosVisiblesSeleccionados(): boolean {
    const visibles = this.productosPaginados();
    return visibles.length > 0 && visibles.every(p => this.seleccionados().has(p.id));
  }

  limpiarSeleccion(): void {
    this.seleccionados().clear();
    this.seleccionados.set(new Set());
  }

  idsSeleccionados(): number[] {
    return [...this.seleccionados()];
  }

  cambiarEstadoSeleccion(nuevoEstado: string): void {
    const ids = [...this.seleccionados()];
    if (!ids.length || this.cambiandoEstado()) return;

    this.cambiandoEstado.set(true);
    let exitosos = 0;

    ids.forEach(id => {
      this.inventoryService.updateProduct(id, { estado: nuevoEstado }).subscribe({
        next: () => {
          exitosos++;
          if (exitosos === ids.length) this.finalizarCambioEstado(ids.length, nuevoEstado);
        },
        error: () => {
          exitosos++;
          if (exitosos === ids.length) this.finalizarCambioEstado(ids.length, nuevoEstado);
        }
      });
    });
  }

  private finalizarCambioEstado(total: number, nuevoEstado: string): void {
    this.cambiandoEstado.set(false);
    this.limpiarSeleccion();
    this.cargarProductos();
    this.notificacion.exito(
      `Estado actualizado a ${nuevoEstado.toLowerCase()} en ${total} ${total === 1 ? 'producto' : 'productos'}`
    );
  }

  /* ---------- Eliminación ---------- */

  eliminacionPendiente = signal<number[] | null>(null);
  eliminacionTexto = signal<string>('este producto');
  eliminando = signal<boolean>(false);

  /** Abre la confirmación para eliminar uno o varios productos. */
  solicitarEliminacion(ids: number[]): void {
    if (!ids.length || this.eliminando()) return;
    this.eliminacionTexto.set(ids.length === 1 ? 'este producto' : `${ids.length} productos`);
    this.eliminacionPendiente.set(ids);
  }

  cancelarEliminacion(): void {
    if (!this.eliminando()) this.eliminacionPendiente.set(null);
  }

  confirmarEliminacion(): void {
    const ids = [...(this.eliminacionPendiente() ?? [])];
    if (!ids.length || this.eliminando()) return;

    this.eliminando.set(true);
    let exitosos = 0;
    let fallos = 0;
    let ultimoError = '';

    const evaluarFin = () => {
      if (exitosos + fallos < ids.length) return;

      this.eliminando.set(false);
      this.eliminacionPendiente.set(null);
      this.limpiarSeleccion();
      this.cargarProductos();

      if (exitosos > 0) {
        this.notificacion.exito(
          `${exitosos} ${exitosos === 1 ? 'producto eliminado' : 'productos eliminados'}`
        );
      }
      if (fallos > 0) {
        this.notificacion.error(ultimoError || `No se pudieron eliminar ${fallos} productos.`);
      }
    };

    ids.forEach(id => {
      this.inventoryService.eliminarProducto(id).subscribe({
        next: () => { exitosos++; evaluarFin(); },
        error: (err) => {
          fallos++;
          ultimoError = err.error?.message || '';
          evaluarFin();
        }
      });
    });
  }

  /* ---------- Navegación ---------- */

  irACategorias(): void {
    this.panelCategoriasAbierto.set(true);
  }

  panelCategoriasAbierto = signal<boolean>(false);

  cerrarPanelCategorias(): void {
    this.panelCategoriasAbierto.set(false);
  }

  alCambiarCategorias(): void {
    // Refresca las chips de filtrado sin recargar la página.
    this.cargarCategorias();
  }

  irAIngresoStock(): void {
    this.panelIngresoAbierto.set(true);
  }

  panelIngresoAbierto = signal<boolean>(false);

  cerrarPanelIngreso(): void {
    this.panelIngresoAbierto.set(false);
  }

  alGuardarIngreso(): void {
    this.panelIngresoAbierto.set(false);
    this.cargarProductos();
    this.cargarTendencias();
  }

  irAEditar(id: number): void {
    this.panelFormulario.set({ modo: 'editar', productoId: id });
  }

  /* ---------- Panel de formulario (crear / editar) ---------- */

  panelFormulario = signal<{ modo: 'crear' | 'editar'; productoId?: number } | null>(null);

  formularioAbierto = computed(() => this.panelFormulario() !== null);

  irANuevoProducto(): void {
    this.panelFormulario.set({ modo: 'crear' });
  }

  cerrarFormulario(): void {
    this.panelFormulario.set(null);
  }

  alGuardarProducto(): void {
    this.panelFormulario.set(null);
    this.cargarProductos();
    this.cargarTendencias();
    if (this.panelVisible()) {
      // Refresca también el detalle abierto si corresponde al mismo producto.
      this.cerrarDetalle();
    }
  }
}
