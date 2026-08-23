/**
 * Modelos del módulo de ventas.
 * Espejan los DTOs del backend (VentaController / VentaService).
 */

export type EstadoVenta = 'COMPLETADA' | 'ANULADA';

export type MetodoPago = 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA';

export interface UsuarioResumen {
  id: number;
  username: string;
  nombreCompleto: string;
  rol: string;
  estado: string;
}

export interface CategoriaResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
}

export interface LoteResumen {
  id: number;
  numeroLote: string | null;
  fechaVencimiento: string | null;
}

/** Espejo de ProductoResponse del backend. */
export interface ProductoResponse {
  id: number;
  categoria: CategoriaResumen | null;
  nombre: string;
  descripcion: string | null;
  codigoBarras: string;
  stockMinimo: number;
  stockActual: number;
  costo: number;
  precioVenta: number;
  margenGanancia: number;
  porcentajeIva: number;
  requierePrescripcion: boolean;
  estado: string;
}

export interface DetalleVenta {
  id: number;
  producto: ProductoResponse | null;
  lote: LoteResumen | null;
  cantidad: number;
  precioUnitarioAplicado: number;
  subtotalLinea: number;
  ivaLinea: number;
}

export interface PagoVenta {
  id: number;
  tipo: string;
  monto: number;
}

/** Espejo de VentaResponse del backend. */
export interface Venta {
  id: number;
  usuario: UsuarioResumen | null;
  fecha: string;
  estado: EstadoVenta | string;
  motivoAnulacion: string | null;
  subtotal: number;
  iva: number;
  descuento: number;
  total: number;
  cambio: number;
  detalles: DetalleVenta[];
  pagos: PagoVenta[];
}

/* ===================== Requests ===================== */

export interface VentaDetalleRequest {
  productoId: number;
  loteId?: number | null;
  cantidad: number;
  precioUnitario?: number | null;
}

export interface PagoVentaRequest {
  tipo: MetodoPago | string;
  monto: number;
}

/**
 * descuento es una fracción entre 0 y 1 (ej. 0.10 = 10%).
 * El backend calcula montoDescuento = subtotal * descuento.
 */
export interface CrearVentaRequest {
  usuarioId: number;
  descuento?: number;
  detalles: VentaDetalleRequest[];
  pagos: PagoVentaRequest[];
}

export interface AnularVentaRequest {
  motivoAnulacion: string;
  usuarioResponsable: string;
  usuarioId?: number;
  confirmacion?: boolean;
}

/* ===================== Helpers ===================== */

/** Nombre a mostrar del vendedor de una venta. */
export function nombreVendedor(venta: Venta): string {
  return venta.usuario?.nombreCompleto || venta.usuario?.username || '—';
}

/** Primer método de pago usado en la venta (para badges del listado). */
export function metodoPagoPrincipal(venta: Venta): string {
  return venta.pagos?.[0]?.tipo ?? '—';
}
