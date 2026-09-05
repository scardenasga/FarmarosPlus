import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { SesionService } from '../../../shared/services/sesion.service';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { ProductSearchCardComponent } from '../../components/product-search-card/product-search-card.component';
import { SugerenciasPosComponent } from '../../../recomendaciones/components/sugerencias-pos/sugerencias-pos.component';
import { RecomendacionItem } from '../../../recomendaciones/models/recomendacion.model';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { VentaService } from '../../services/venta.service';
import {
  MetodoPago,
  ProductoResponse,
  VentaDetalleRequest
} from '../../models/venta.model';

interface ItemCarrito {
  producto: ProductoResponse;
  cantidad: number;
  loteId: number | null;
  numeroLote: string | null;
}

const METODOS_PAGO: MetodoPago[] = ['EFECTIVO', 'TARJETA', 'TRANSFERENCIA'];

@Component({
  selector: 'app-pos-venta',
  standalone: true,
  imports: [CommonModule, FormsModule, SearchBarComponent, ProductSearchCardComponent, SugerenciasPosComponent],
  templateUrl: './pos-venta.component.html',
  styleUrl: './pos-venta.component.css'
})
export class PosVentaComponent implements OnInit {
  private ventaService = inject(VentaService);
  private inventoryService = inject(InventoryService);
  private sesion = inject(SesionService);
  private notificacion = inject(NotificacionService);
  private router = inject(Router);

  metodosPago = METODOS_PAGO;

  productos = signal<ProductoResponse[]>([]);
  sinResultados = signal<boolean>(false);
  buscando = signal<boolean>(false);

  carrito = signal<ItemCarrito[]>([]);
  metodoPago = signal<MetodoPago>('EFECTIVO');
  /** Dinero recibido en efectivo (signal para que los computed reaccionen al teclear). */
  recibido = signal<number | null>(null);
  /** Porcentaje de descuento digitado (0-100). */
  descuentoPorcentaje = signal<number>(0);

  procesando = signal<boolean>(false);

  ngOnInit() {
    // Catálogo inicial para que la vista no abra vacía.
    this.buscando.set(true);
    this.ventaService.obtenerProductosActivos().subscribe({
      next: (data) => {
        this.productos.set(data ?? []);
        this.sinResultados.set(false);
        this.buscando.set(false);
      },
      error: () => {
        this.buscando.set(false);
      }
    });
  }

  cantidadDe(productoId: number): number {
    return this.carrito().find(i => i.producto.id === productoId)?.cantidad ?? 0;
  }

  /** IDs del carrito para el panel de recomendaciones (estable, ordenado). */
  carritoIds = computed(() => this.carrito().map(i => i.producto.id));

  /** Handler del panel: reutiliza agregar() sin duplicar lógica de lote/stock. */
  onSugerenciaAgregar(item: RecomendacionItem): void {
    // Evitar duplicar si ya está en carrito: aumentar cantidad
    if (this.cantidadDe(item.productoId) > 0) {
      const existente = this.carrito().find(i => i.producto.id === item.productoId);
      if (existente) {
        this.aumentar(existente);
        return;
      }
    }
    // Buscar en catálogo ya cargado para respuesta inmediata
    const enCatalogo = this.productos().find(p => p.id === item.productoId);
    if (enCatalogo) {
      this.agregar(enCatalogo);
      return;
    }
    // Fallback: cargar producto por ID y luego agregar
    this.inventoryService.getProductById(item.productoId).subscribe({
      next: (prod: any) => {
        // Normalizar Product -> ProductoResponse si viene de inventory
        const adaptado: ProductoResponse = {
          id: prod.id ?? prod.uniqueID ?? prod.uniqueid,
          categoria: prod.categoria ?? null,
          nombre: prod.nombre,
          descripcion: prod.descripcion ?? null,
          codigoBarras: prod.codigoBarras ?? prod.codigo_barras ?? '',
          stockMinimo: prod.stockMinimo ?? 0,
          stockActual: prod.stockActual ?? 0,
          costo: prod.costo ?? 0,
          precioVenta: prod.precioVenta ?? 0,
          margenGanancia: prod.margenGanancia ?? 0,
          porcentajeIva: prod.porcentajeIva ?? 0,
          requierePrescripcion: prod.requierePrescripcion ?? false,
          estado: prod.estado ?? 'ACTIVO'
        };
        this.agregar(adaptado);
      },
      error: () => this.notificacion.error('No se pudo agregar el producto sugerido.')
    });
  }

  /* ---------- Catálogo ---------- */

  buscar(termino: string) {
    const t = termino.trim();
    if (!t) {
      // Sin término de búsqueda se vuelve a mostrar el catálogo completo.
      this.cargarCatalogo();
      return;
    }

    this.buscando.set(true);
    this.ventaService.buscarProductos(t).subscribe({
      next: (data) => {
        this.productos.set(data);
        this.sinResultados.set(data.length === 0);
        this.buscando.set(false);
      },
      error: () => {
        this.productos.set([]);
        this.sinResultados.set(true);
        this.buscando.set(false);
      }
    });
  }

  private cargarCatalogo() {
    this.buscando.set(true);
    this.ventaService.obtenerProductosActivos().subscribe({
      next: (data) => {
        this.productos.set(data ?? []);
        this.sinResultados.set(false);
        this.buscando.set(false);
      },
      error: () => {
        this.productos.set([]);
        this.sinResultados.set(true);
        this.buscando.set(false);
      }
    });
  }

  /* ---------- Carrito ---------- */

  agregar(producto: ProductoResponse) {
    if (this.cantidadDe(producto.id) > 0) {
      this.aumentar(producto);
      return;
    }

    // Consulta el primer lote disponible; si no hay, la línea va sin lote
    // y el backend descuenta del stock global del producto.
    this.ventaService.obtenerLotesDisponibles(producto.id).subscribe({
      next: (lotes) => this.pushItem(producto, lotes?.[0] ?? null),
      error: () => this.pushItem(producto, null)
    });
  }

  private pushItem(
    producto: ProductoResponse,
    lote: { id: number; numeroLote?: string | null } | null
  ) {
    this.carrito.update(prev => [
      ...prev,
      {
        producto,
        cantidad: 1,
        loteId: lote?.id ?? null,
        numeroLote: lote?.numeroLote ?? null
      }
    ]);
  }

  aumentar(item: ItemCarrito | ProductoResponse) {
    const id = 'producto' in item ? item.producto.id : item.id;
    this.carrito.update(prev => prev.map(i =>
      i.producto.id === id && i.cantidad < (i.producto.stockActual ?? 0)
        ? { ...i, cantidad: i.cantidad + 1 }
        : i
    ));
  }

  disminuir(item: ItemCarrito | ProductoResponse) {
    const id = 'producto' in item ? item.producto.id : item.id;
    const actual = this.carrito().find(i => i.producto.id === id);
    if (!actual) return;

    if (actual.cantidad <= 1) {
      this.carrito.update(prev => prev.filter(i => i.producto.id !== id));
    } else {
      this.carrito.update(prev => prev.map(i =>
        i.producto.id === id ? { ...i, cantidad: i.cantidad - 1 } : i
      ));
    }
  }

  eliminar(item: ItemCarrito) {
    this.carrito.update(prev => prev.filter(i => i.producto.id !== item.producto.id));
  }

  /** Actualiza la cantidad escrita manualmente, limitada al stock disponible. */
  actualizarCantidad(item: ItemCarrito, valor: string | number | null) {
    const stock = item.producto.stockActual ?? 0;
    let cantidad = Math.floor(Number(valor));

    if (!Number.isFinite(cantidad) || cantidad <= 0) {
      // Si el campo queda vacío o inválido, se elimina la línea.
      this.eliminar(item);
      return;
    }
    if (cantidad > stock) cantidad = stock;

    this.carrito.update(prev => prev.map(i =>
      i.producto.id === item.producto.id ? { ...i, cantidad } : i
    ));
  }

  vaciar() {
    this.carrito.set([]);
    this.recibido.set(null);
    this.descuentoPorcentaje.set(0);
  }

  /* ---------- Totales ---------- */

  subtotal = computed(() =>
    this.carrito().reduce((acc, i) => acc + i.producto.precioVenta * i.cantidad, 0)
  );

  ivaTotal = computed(() =>
    this.carrito().reduce((acc, i) => {
      const linea = i.producto.precioVenta * i.cantidad;
      return acc + linea * ((i.producto.porcentajeIva ?? 0) / 100);
    }, 0)
  );

  montoDescuento = computed(() =>
    this.subtotal() * Math.min(Math.max(this.descuentoNormalizado(), 0), 1)
  );

  total = computed(() => Math.max(0, this.subtotal() + this.ivaTotal() - this.montoDescuento()));

  cambio = computed(() => {
    if (this.metodoPago() !== 'EFECTIVO') return 0;
    const r = this.recibido();
    if (r == null || r <= this.total()) return 0;
    return r - this.total();
  });

  /** Cuánto falta por cubrir cuando lo recibido no alcanza (solo efectivo). */
  falta = computed(() => {
    if (this.metodoPago() !== 'EFECTIVO') return 0;
    const r = this.recibido();
    const t = this.total();
    if (r == null || r >= t) return 0;
    return t - r;
  });

  /** El backend espera el descuento como fracción entre 0 y 1 (0.10 = 10%). */
  private descuentoNormalizado(): number {
    const pct = Number(this.descuentoPorcentaje()) || 0;
    return pct / 100;
  }

  puedeCobrar(): boolean {
    if (!this.carrito().length || this.procesando()) return false;
    const pct = Number(this.descuentoPorcentaje());
    if (pct < 0 || pct > 100) return false;
    // En efectivo el valor "Recibido" es obligatorio y debe cubrir el total.
    if (this.metodoPago() === 'EFECTIVO') {
      const r = this.recibido();
      if (r == null || r <= 0) return false;
      if (this.falta() > 0) return false;
    }
    return true;
  }

  seleccionarMetodo(metodo: MetodoPago) {
    this.metodoPago.set(metodo);
  }

  /* ---------- Cobro ---------- */

  confirmarVenta() {
    if (!this.puedeCobrar()) return;

    this.procesando.set(true);

    const detalles: VentaDetalleRequest[] = this.carrito().map(i => ({
      productoId: i.producto.id,
      loteId: i.loteId,
      cantidad: i.cantidad,
      precioUnitario: i.producto.precioVenta
    }));

    // EFECTIVO admite sobrepago (el excedente es cambio); los otros medios
    // deben ser por el monto exacto según reglas del backend.
    const recibidoEfectivo = this.metodoPago() === 'EFECTIVO' ? this.recibido() : null;
    const montoPago = recibidoEfectivo && recibidoEfectivo > 0 ? recibidoEfectivo : this.total();

    const request = {
      usuarioId: this.sesion.idUsuario(),
      descuento: this.descuentoNormalizado(),
      detalles,
      pagos: [{ tipo: this.metodoPago(), monto: Number(montoPago.toFixed(2)) }]
    };

    this.ventaService.registrarVenta(request).subscribe({
      next: (venta) => {
        this.notificacion.exito(`Venta #${venta?.id ?? ''} registrada correctamente`);
        // Se permanece en el POS y se limpia todo para la siguiente venta.
        this.resetearParaNuevaVenta();
      },
      error: (err) => {
        this.notificacion.error(err.error?.message || 'No se pudo registrar la venta. Verifica los datos e inténtalo de nuevo.');
        this.procesando.set(false);
      }
    });
  }

  /** Limpia carrito, pago y descuento para iniciar otra venta desde cero. */
  private resetearParaNuevaVenta() {
    this.carrito.set([]);
    this.recibido.set(null);
    this.descuentoPorcentaje.set(0);
    this.metodoPago.set('EFECTIVO');
    this.procesando.set(false);
    // Vuelve al catálogo completo para armar la siguiente venta.
    this.cargarCatalogo();
  }

  volver() {
    this.router.navigate(['/ventas']);
  }
}
