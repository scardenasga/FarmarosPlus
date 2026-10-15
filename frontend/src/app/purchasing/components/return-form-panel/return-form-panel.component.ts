import { Component, OnChanges, SimpleChanges, computed, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PurchasingService } from '../../services/purchasing.service';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { SesionService } from '../../../shared/services/sesion.service';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { Supplier, SupplierDetalleResponse, SupplierProductRel } from '../../../supplier/models/supplier.model';
import { VentaResponse } from '../../models/purchasing.model';

type TipoDevolucion = 'proveedor' | 'cliente';
interface ItemDevolucion {
  idProducto: number;
  nombreProducto: string;
  idLote: number | null;
  numeroLote: string | null;
  cantidadDisponible: number;
  cantidad: number;
  idDetalleVenta?: number;
}
interface LoteResult { id: number; numeroLote: string | null; cantidad: number; fechaVencimiento: string | null; }

@Component({
  selector: 'app-return-form-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmationDialogComponent],
  templateUrl: './return-form-panel.component.html',
  styleUrl: './return-form-panel.component.css'
})
export class ReturnFormPanelComponent implements OnChanges {
  private purchasingService = inject(PurchasingService);
  private supplierService = inject(SupplierService);
  private sesion = inject(SesionService);
  private notificacion = inject(NotificacionService);

  modo = input.required<'crear' | 'editar'>();
  devolucionId = input<number | null>(null);
  tipoInicial = input<TipoDevolucion>('proveedor');
  abierto = input<boolean>(false);
  cerrado = output<void>();
  guardado = output<void>();

  tipoDevolucion = signal<TipoDevolucion>('proveedor');
  proveedores = signal<Supplier[]>([]);
  busquedaProveedor = signal<string>('');
  idProveedorSeleccionado = signal<number | null>(null);
  detalleProveedor = signal<SupplierDetalleResponse | null>(null);
  busquedaProductoProveedor = signal<string>('');
  nombreCliente = signal<string>('');
  documentoCliente = signal<string>('');
  idVenta = signal<string>('');
  ventaCargada = signal<VentaResponse | null>(null);
  items = signal<ItemDevolucion[]>([]);
  productoSeleccionado = signal<SupplierProductRel | null>(null);
  lotes = signal<LoteResult[]>([]);
  idLoteSeleccionado = signal<number | null>(null);
  cantidadNueva = signal<number>(1);
  motivo = signal<string>('');
  observaciones = signal<string>('');
  estado = signal<string>('PENDIENTE');
  readonly estadosDevolucion: string[] = ['PENDIENTE', 'ENVIADA', 'ACEPTADA', 'RECHAZADA', 'CERRADA'];
  isAdmin = computed(() => this.sesion.esAdminORegente());
  estadosVisiblesForm(): string[] {
    if (this.isAdmin()) return this.estadosDevolucion;
    const actual = this.estado();
    if (actual === 'PENDIENTE') return ['PENDIENTE', 'ENVIADA', 'CERRADA'];
    if (actual === 'ENVIADA') return ['ENVIADA', 'PENDIENTE'];
    return [actual];
  }
  cargandoProveedores = signal<boolean>(false);
  cargandoDetalleProveedor = signal<boolean>(false);
  cargandoLotes = signal<boolean>(false);
  cargandoVenta = signal<boolean>(false);
  cargandoDevolucion = signal<boolean>(false);
  enviando = signal<boolean>(false);
  error = signal<string>('');
  errorVenta = signal<string>('');
  errorProducto = signal<string>('');
  mostrarConfirmacion = signal<boolean>(false);

  esProveedor = computed(() => this.tipoDevolucion() === 'proveedor');
  esEditar = computed(() => this.modo() === 'editar');

  proveedoresFiltrados = computed(() => {
    const t = this.busquedaProveedor().trim().toLowerCase();
    if (!t) return this.proveedores();
    return this.proveedores().filter(p => [p.nombre, p.nit ?? '', p.contacto ?? '', p.telefono ?? ''].some(v => v.toLowerCase().includes(t)));
  });
  productosProveedorFiltrados = computed(() => {
    const detalle = this.detalleProveedor();
    const t = this.busquedaProductoProveedor().trim().toLowerCase();
    const productos = detalle?.productos ?? [];
    if (!t) return productos.slice(0, 8);
    return productos.filter(prod => [prod.nombre, prod.codigoBarras, prod.codigoProductoProveedor ?? '', prod.descripcion ?? ''].some(v => v.toLowerCase().includes(t)));
  });
  puedeEnviar = computed(() => {
    if (this.enviando() || this.cargandoDevolucion()) return false;
    if (this.esEditar()) return true;
    const tiene = this.items().some(i => i.cantidad > 0);
    const base = this.esProveedor() ? this.idProveedorSeleccionado() !== null : this.ventaCargada() !== null;
    return base && tiene;
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['abierto'] && this.abierto()) {
      this.tipoDevolucion.set(this.tipoInicial());
      this.reiniciar();
      this.cargarProveedores();
      if (this.esEditar() && this.devolucionId()!=null) this.cargarDevolucion();
    }
  }

  cargarProveedores(): void {
    this.cargandoProveedores.set(true);
    this.supplierService.listActive().subscribe({
      next: d => { this.proveedores.set(d ?? []); this.cargandoProveedores.set(false); },
      error: () => this.cargandoProveedores.set(false)
    });
  }

  cargarDevolucion(): void {
    const id = this.devolucionId();
    if (id==null) return;
    this.cargandoDevolucion.set(true);
    if (this.esProveedor()) {
      this.purchasingService.obtenerDevolucion(id).subscribe({
        next: (d: any) => {
          this.motivo.set(d.motivo ?? '');
          this.observaciones.set(d.observaciones ?? '');
          this.estado.set(d.estado ?? 'PENDIENTE');
          this.cargandoDevolucion.set(false);
        },
        error: () => { this.notificacion.error('No se pudo cargar la devolución.'); this.cargandoDevolucion.set(false); this.cerrado.emit(); }
      });
    } else {
      this.purchasingService.obtenerDevolucionCliente(id).subscribe({
        next: (d: any) => {
          this.motivo.set(d.motivo ?? '');
          this.nombreCliente.set(d.nombreCliente ?? '');
          this.documentoCliente.set(d.documentoCliente ?? '');
          this.cargandoDevolucion.set(false);
        },
        error: () => { this.notificacion.error('No se pudo cargar la devolución.'); this.cargandoDevolucion.set(false); this.cerrado.emit(); }
      });
    }
  }

  reiniciar(): void {
    this.busquedaProveedor.set('');
    this.idProveedorSeleccionado.set(null);
    this.detalleProveedor.set(null);
    this.busquedaProductoProveedor.set('');
    this.nombreCliente.set('');
    this.documentoCliente.set('');
    this.idVenta.set('');
    this.ventaCargada.set(null);
    this.items.set([]);
    this.productoSeleccionado.set(null);
    this.lotes.set([]);
    this.idLoteSeleccionado.set(null);
    this.cantidadNueva.set(1);
    this.motivo.set('');
    this.observaciones.set('');
    this.error.set('');
    this.errorVenta.set('');
    this.errorProducto.set('');
    this.mostrarConfirmacion.set(false);
  }

  onTipoChange(tipo: TipoDevolucion): void {
    if (this.tipoDevolucion()===tipo|| this.esEditar()) return;
    this.tipoDevolucion.set(tipo);
    this.reiniciar();
    if (tipo==='proveedor') this.cargarProveedores();
  }

  onProveedorChange(valor: string): void {
    const id = Number(valor);
    if (!id) return;
    const prov = this.proveedores().find(p => p.idProveedor === id);
    if (prov) this.seleccionarProveedor(prov);
  }

  seleccionarProveedor(p: Supplier): void {
    this.idProveedorSeleccionado.set(p.idProveedor);
    this.cargandoDetalleProveedor.set(true);
    this.detalleProveedor.set(null);
    this.supplierService.getDetail(p.idProveedor).subscribe({
      next: d => { this.detalleProveedor.set(d); this.cargandoDetalleProveedor.set(false); },
      error: () => { this.cargandoDetalleProveedor.set(false); this.errorProducto.set('No se pudo cargar el detalle.'); }
    });
  }

  seleccionarProducto(prod: SupplierProductRel): void {
    this.productoSeleccionado.set(prod);
    this.idLoteSeleccionado.set(null);
    this.cargandoLotes.set(true);
    this.purchasingService.obtenerLotes(prod.id).subscribe({
      next: data => { this.lotes.set(data as any); this.cargandoLotes.set(false); },
      error: () => { this.cargandoLotes.set(false); this.errorProducto.set('No se pudieron cargar lotes.'); }
    });
  }

  agregarItemProveedor(): void {
    const prod = this.productoSeleccionado();
    const loteId = this.idLoteSeleccionado();
    const lote = this.lotes().find(l=>l.id===loteId);
    if (!prod) { this.errorProducto.set('Seleccione producto.'); return; }
    if (!lote) { this.errorProducto.set('Seleccione lote.'); return; }
    if (this.cantidadNueva()<1) { this.errorProducto.set('Cantidad mínima 1.'); return; }
    if (this.cantidadNueva()>lote.cantidad) { this.errorProducto.set(`Stock disponible: ${lote.cantidad}`); return; }
    this.items.update(prev => {
      const ex = prev.find(i=>i.idLote===lote.id);
      if (ex) {
        const tot = ex.cantidad + this.cantidadNueva();
        if (tot>lote.cantidad) { this.errorProducto.set(`No puede devolver más de ${lote.cantidad}`); return prev; }
        return prev.map(i=>i.idLote===lote.id ? {...i, cantidad: tot}:i);
      }
      return [...prev, { idProducto: prod.id, nombreProducto: prod.nombre, idLote: lote.id, numeroLote: lote.numeroLote, cantidadDisponible: lote.cantidad, cantidad: this.cantidadNueva() }];
    });
    this.productoSeleccionado.set(null); this.lotes.set([]); this.idLoteSeleccionado.set(null); this.cantidadNueva.set(1); this.busquedaProductoProveedor.set('');
  }

  quitarItem(i: number): void { this.items.update(p=>{ const c=[...p]; c.splice(i,1); return c; }); }
  actualizarCantidad(i:number, v:number): void { this.items.update(p=>p.map((it,idx)=> idx===i? {...it, cantidad: Number(v)||0}: it)); }

  cargarVenta(): void {
    const valor = Number(this.idVenta());
    if (!valor) { this.errorVenta.set('Ingrese ID de venta.'); return; }
    this.cargandoVenta.set(true); this.errorVenta.set(''); this.ventaCargada.set(null); this.items.set([]);
    this.purchasingService.obtenerVenta(valor).subscribe({
      next: venta => {
        this.ventaCargada.set(venta);
        this.items.set(venta.detalles.map(d=>({ idProducto: d.producto.id, nombreProducto: d.producto.nombre, idLote: d.lote?.idLote ?? null, numeroLote: d.lote?.numeroLote ?? null, cantidadDisponible: d.cantidad, cantidad: 0, idDetalleVenta: d.id })));
        this.cargandoVenta.set(false);
      },
      error: () => { this.errorVenta.set('No se pudo cargar venta.'); this.cargandoVenta.set(false); }
    });
  }

  cerrar(): void { if(!this.enviando()) this.cerrado.emit(); }
  solicitarGuardar(): void {
    if (!this.puedeEnviar()) { this.notificacion.advertencia('Completa los campos obligatorios.'); return; }
    if (this.esProveedor() && !this.esEditar() && !this.motivo().trim()) { this.notificacion.advertencia('Indica el motivo.'); return; }
    this.mostrarConfirmacion.set(true);
  }
  cancelarConfirmacion(): void { this.mostrarConfirmacion.set(false); }
  confirmarGuardado(): void { this.esEditar() ? this.guardarEdicion() : this.guardarCreacion(); }

  private guardarCreacion(): void {
    this.enviando.set(true);
    if (this.esProveedor()) {
      this.purchasingService.registrarDevolucion({
        idProveedor: this.idProveedorSeleccionado()!,
        usuarioResponsable: this.sesion.username(),
        motivo: this.motivo().trim() || 'OTRO',
        detalles: this.items().map(i=>({ idProducto: i.idProducto, idLote: i.idLote!, cantidad: i.cantidad }))
      }).subscribe({
        next: () => { this.notificacion.exito('Devolución a proveedor registrada'); this.enviando.set(false); this.mostrarConfirmacion.set(false); this.guardado.emit(); },
        error: err => { this.error.set(err?.error?.message ?? 'Error al registrar'); this.enviando.set(false); this.mostrarConfirmacion.set(false); }
      });
    } else {
      const detalles = this.items().filter(i=>i.cantidad>0).map(i=>({ idDetalleVenta: i.idDetalleVenta!, cantidad: i.cantidad }));
      this.purchasingService.registrarDevolucionCliente({
        idVenta: Number(this.idVenta()),
        usuarioResponsable: this.sesion.username(),
        nombreCliente: this.nombreCliente().trim()||undefined,
        documentoCliente: this.documentoCliente().trim()||undefined,
        motivo: this.motivo().trim()||undefined,
        detalles
      }).subscribe({
        next: () => { this.notificacion.exito('Devolución a cliente registrada'); this.enviando.set(false); this.mostrarConfirmacion.set(false); this.guardado.emit(); },
        error: err => { this.error.set(err?.error?.message ?? 'Error al registrar'); this.enviando.set(false); this.mostrarConfirmacion.set(false); }
      });
    }
  }

  private guardarEdicion(): void {
    const id = this.devolucionId();
    if (id==null) return;
    this.enviando.set(true);
    if (this.esProveedor()) {
      this.purchasingService.actualizarDevolucion(id, { motivo: this.motivo().trim()||undefined, observaciones: this.observaciones().trim()||undefined, estado: this.estado() }, this.sesion.username()).subscribe({
        next: () => { this.notificacion.exito('Devolución actualizada'); this.enviando.set(false); this.mostrarConfirmacion.set(false); this.guardado.emit(); },
        error: (err: any) => { this.error.set(err?.error?.message ?? 'No se pudo actualizar'); this.enviando.set(false); this.mostrarConfirmacion.set(false); }
      });
    } else {
      this.purchasingService.actualizarDevolucionCliente(id, { motivo: this.motivo().trim()||undefined, nombreCliente: this.nombreCliente().trim()||undefined, documentoCliente: this.documentoCliente().trim()||undefined }).subscribe({
        next: () => { this.notificacion.exito('Devolución actualizada'); this.enviando.set(false); this.mostrarConfirmacion.set(false); this.guardado.emit(); },
        error: (err: any) => { this.error.set(err?.error?.message ?? 'No se pudo actualizar'); this.enviando.set(false); this.mostrarConfirmacion.set(false); }
      });
    }
  }
}
