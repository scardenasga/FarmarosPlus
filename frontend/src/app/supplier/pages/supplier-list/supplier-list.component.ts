import { Component, OnInit, computed, inject, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { SupplierDetailPanelComponent } from '../../components/supplier-detail-panel/supplier-detail-panel.component';
import { SupplierFormPanelComponent } from '../../components/supplier-form-panel/supplier-form-panel.component';
import { Supplier, SupplierDetalleResponse } from '../../models/supplier.model';
import { SupplierService } from '../../services/supplier.service';

type VistaMode = 'grid' | 'tabla';
const CLAVE_VISTA = 'proveedores.vista';

@Component({
  selector: 'app-supplier-list',
  standalone: true,
  imports: [CommonModule, SearchBarComponent, ConfirmationDialogComponent, SupplierDetailPanelComponent, SupplierFormPanelComponent],
  templateUrl: './supplier-list.component.html',
  styleUrl: './supplier-list.component.css'
})
export class SupplierListComponent implements OnInit {
  private router = inject(Router);
  private supplierService = inject(SupplierService);
  private notificacion = inject(NotificacionService);

  @ViewChild(SupplierDetailPanelComponent) detailPanel?: SupplierDetailPanelComponent;

  suppliers = signal<Supplier[]>([]);
  cargando = signal<boolean>(true);
  searchTerm = signal<string>('');
  estadoFiltro = signal<'ACTIVO' | 'INACTIVO' | 'TODOS'>('TODOS');
  readonly opcionesEstado = [
    { valor: 'TODOS', etiqueta: 'Todos' },
    { valor: 'ACTIVO', etiqueta: 'Activos' },
    { valor: 'INACTIVO', etiqueta: 'Inactivos' }
  ] as const;

  vista = signal<VistaMode>(this.cargarVista());

  // Selección masiva
  seleccionados = signal<Set<number>>(new Set());
  cambiandoEstado = signal<boolean>(false);

  // Paginación
  readonly TAMANOS_PAGINA = [5, 10, 25, 50];
  tamanoPagina = signal<number>(10);
  pagina = signal<number>(1);

  // Paneles
  panelDetalleVisible = signal<boolean>(false);
  detalleId = signal<number | null>(null);
  panelFormulario = signal<{ modo: 'crear' | 'editar'; supplierId?: number } | null>(null);
  formularioAbierto = computed(() => this.panelFormulario() !== null);

  // Confirmación cambio masivo
  estadoPendiente = signal<'ACTIVO' | 'INACTIVO' | null>(null);

  suppliersFiltrados = computed(() => {
    const termino = this.searchTerm().toLowerCase().trim();
    const estado = this.estadoFiltro();
    return this.suppliers().filter(s => {
      if (estado !== 'TODOS' && s.estado !== estado) return false;
      if (termino) {
        const coincide =
          s.nombre.toLowerCase().includes(termino) ||
          (s.nit ?? '').toLowerCase().includes(termino) ||
          (s.contacto ?? '').toLowerCase().includes(termino) ||
          (s.email ?? '').toLowerCase().includes(termino);
        if (!coincide) return false;
      }
      return true;
    });
  });

  totalPaginas = computed(() => Math.max(1, Math.ceil(this.suppliersFiltrados().length / this.tamanoPagina())));
  suppliersPaginados = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamanoPagina();
    return this.suppliersFiltrados().slice(inicio, inicio + this.tamanoPagina());
  });
  rangoMostrado = computed(() => {
    const total = this.suppliersFiltrados().length;
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

  ngOnInit(): void { this.cargarProveedores(); }

  cargarProveedores(): void {
    this.cargando.set(true);
    this.supplierService.listAll().subscribe({
      next: (suppliers) => { this.suppliers.set(suppliers ?? []); this.cargando.set(false); },
      error: () => { this.notificacion.error('No se pudo cargar los proveedores.'); this.cargando.set(false); }
    });
  }

  // Filtros
  onBuscar(termino: string): void { this.searchTerm.set(termino); this.pagina.set(1); }
  seleccionarEstado(valor: 'ACTIVO' | 'INACTIVO' | 'TODOS'): void { this.estadoFiltro.set(valor); this.pagina.set(1); }

  // Vista
  cambiarVista(vista: VistaMode): void { this.vista.set(vista); localStorage.setItem(CLAVE_VISTA, vista); }
  private cargarVista(): VistaMode { return localStorage.getItem(CLAVE_VISTA) === 'tabla' ? 'tabla' : 'grid'; }

  // Paginación
  cambiarPagina(nueva: number): void { const d = Math.min(Math.max(1, nueva), this.totalPaginas()); if (d !== this.pagina()) this.pagina.set(d); }
  cambiarTamanoPagina(t: string | number): void { this.tamanoPagina.set(Number(t)); this.pagina.set(1); }

  // Selección
  estaSeleccionado(id: number): boolean { return this.seleccionados().has(id); }
  get haySeleccion(): boolean { return this.seleccionados().size > 0; }
  toggleSeleccion(id: number, event: Event): void {
    event.stopPropagation();
    this.seleccionados.update(prev => { const n = new Set(prev); if (n.has(id)) n.delete(id); else n.add(id); return n; });
  }
  toggleTodos(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (!checked) { this.seleccionados.set(new Set()); return; }
    this.seleccionados.set(new Set(this.suppliersPaginados().map(s => s.idProveedor)));
  }
  get todosVisiblesSeleccionados(): boolean {
    const vis = this.suppliersPaginados();
    return vis.length > 0 && vis.every(s => this.seleccionados().has(s.idProveedor));
  }
  limpiarSeleccion(): void { this.seleccionados.set(new Set()); }
  idsSeleccionados(): number[] { return [...this.seleccionados()]; }

  solicitarCambioEstado(nuevo: 'ACTIVO' | 'INACTIVO'): void {
    if (!this.seleccionados().size) return;
    this.estadoPendiente.set(nuevo);
  }
  cancelarCambioEstado(): void { this.estadoPendiente.set(null); }
  confirmarCambioEstado(): void {
    const nuevo = this.estadoPendiente();
    const ids = [...this.seleccionados()];
    if (!nuevo || !ids.length) return;
    this.cambiandoEstado.set(true);
    let completados = 0;
    const finalizar = () => {
      completados++;
      if (completados === ids.length) {
        this.cambiandoEstado.set(false);
        this.estadoPendiente.set(null);
        this.limpiarSeleccion();
        this.cargarProveedores();
        this.notificacion.exito(`Estado actualizado a ${nuevo.toLowerCase()} en ${ids.length} proveedor(es)`);
      }
    };
    ids.forEach(id => {
      this.supplierService.updateStatus(id, { estado: nuevo }).subscribe({ next: finalizar, error: finalizar });
    });
  }

  // Detalle
  abrirDetalle(s: Supplier): void {
    this.detalleId.set(s.idProveedor);
    this.panelDetalleVisible.set(true);
    setTimeout(() => this.detailPanel?.cargar(), 0);
  }
  cerrarDetalle(): void { this.panelDetalleVisible.set(false); }
  alEditarDesdeDetalle(id: number): void {
    this.panelDetalleVisible.set(false);
    this.panelFormulario.set({ modo: 'editar', supplierId: id });
  }
  alActualizarDetalle(): void { this.cargarProveedores(); }

  // Formularios
  irANuevo(): void { this.panelFormulario.set({ modo: 'crear' }); }
  irAEditar(id: number): void { this.panelFormulario.set({ modo: 'editar', supplierId: id }); }
  cerrarFormulario(): void { this.panelFormulario.set(null); }
  alGuardar(): void { this.panelFormulario.set(null); this.cargarProveedores(); if (this.panelDetalleVisible()) this.detailPanel?.cargar(); }

  // Compatibilidad: navegación legacy
  handleBack(): void { this.router.navigate(['/compras-gestion']); }
}
