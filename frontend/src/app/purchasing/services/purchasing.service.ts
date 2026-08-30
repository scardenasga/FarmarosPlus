import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  CompraResponse, 
  ActualizarCompraRequest,
  ActualizarDevolucionClienteRequest,
  ActualizarDevolucionRequest,
  DevolucionClienteResponse,
  DevolucionResponse, 
  RegistrarCompraRequest, 
  RegistrarDevolucionRequest,
  RegistrarDevolucionClienteRequest,
  ResumenSeguimiento,
  PrevisualizacionOrden,
  VentaResponse
  ,OrdenCompraResumen, OrdenCompraResponse, RegistrarOrdenRequest, RegistrarRecepcionRequest, RecepcionCompraResumen,
  TopProductoComprado,
  CumplimientoProveedor
} from '../models/purchasing.model';

@Injectable({
  providedIn: 'root'
})
export class PurchasingService {
  private http = inject(HttpClient);
  private readonly apiCompras = '/api/compras';
  private readonly apiDevoluciones = '/api/devoluciones';
  private readonly apiDevolucionesClientes = '/api/devoluciones-clientes';
  private readonly apiOrdenes = '/api/ordenes-compra';
  private readonly apiProductos = '/api/productos';
  private readonly apiVentas = '/api/ventas';

  // --- Compras ---

  registrarCompra(request: RegistrarCompraRequest): Observable<CompraResponse> {
    return this.http.post<CompraResponse>(this.apiCompras, request);
  }

  listarCompras(idProveedor?: number): Observable<CompraResponse[]> {
    const url = idProveedor ? `${this.apiCompras}?idProveedor=${idProveedor}` : this.apiCompras;
    return this.http.get<CompraResponse[]>(url);
  }

  obtenerCompra(id: number): Observable<CompraResponse> {
    return this.http.get<CompraResponse>(`${this.apiCompras}/${id}`);
  }

  actualizarCompra(id: number, request: ActualizarCompraRequest): Observable<CompraResponse> {
    return this.http.patch<CompraResponse>(`${this.apiCompras}/${id}`, request);
  }

  eliminarCompra(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiCompras}/${id}`);
  }

  // --- Devoluciones ---

  registrarDevolucion(request: RegistrarDevolucionRequest): Observable<DevolucionResponse> {
    return this.http.post<DevolucionResponse>(this.apiDevoluciones, request);
  }

  listarDevoluciones(): Observable<DevolucionResponse[]> {
    return this.http.get<DevolucionResponse[]>(this.apiDevoluciones);
  }

  obtenerDevolucion(id: number): Observable<DevolucionResponse> {
    return this.http.get<DevolucionResponse>(`${this.apiDevoluciones}/${id}`);
  }

  actualizarDevolucion(id: number, request: ActualizarDevolucionRequest, usuarioResponsable?: string): Observable<DevolucionResponse> {
    let params = new HttpParams();
    if (usuarioResponsable) params = params.set('usuarioResponsable', usuarioResponsable);
    return this.http.patch<DevolucionResponse>(`${this.apiDevoluciones}/${id}`, request, { params });
  }

  cambiarEstadoDevolucion(id: number, estado: string, usuarioResponsable?: string): Observable<DevolucionResponse> {
    let params = new HttpParams();
    if (usuarioResponsable) params = params.set('usuarioResponsable', usuarioResponsable);
    return this.http.patch<DevolucionResponse>(`${this.apiDevoluciones}/${id}/estado`, { estado }, { params });
  }

  eliminarDevolucion(id: number, usuarioResponsable: string): Observable<void> {
    return this.http.delete<void>(`${this.apiDevoluciones}/${id}`, {
      params: { usuarioResponsable }
    });
  }

  registrarDevolucionCliente(request: RegistrarDevolucionClienteRequest): Observable<DevolucionClienteResponse> {
    return this.http.post<DevolucionClienteResponse>(this.apiDevolucionesClientes, request);
  }

  listarDevolucionesClientes(): Observable<DevolucionClienteResponse[]> {
    return this.http.get<DevolucionClienteResponse[]>(this.apiDevolucionesClientes);
  }

  obtenerDevolucionCliente(id: number): Observable<DevolucionClienteResponse> {
    return this.http.get<DevolucionClienteResponse>(`${this.apiDevolucionesClientes}/${id}`);
  }

  actualizarDevolucionCliente(id: number, request: ActualizarDevolucionClienteRequest): Observable<DevolucionClienteResponse> {
    return this.http.patch<DevolucionClienteResponse>(`${this.apiDevolucionesClientes}/${id}`, request);
  }

  eliminarDevolucionCliente(id: number, usuarioResponsable: string): Observable<void> {
    return this.http.delete<void>(`${this.apiDevolucionesClientes}/${id}`, {
      params: { usuarioResponsable }
    });
  }

  // --- Ã“rdenes de Compra ---

  getResumenSeguimiento(): Observable<ResumenSeguimiento> {
    return this.http.get<ResumenSeguimiento>(`${this.apiOrdenes}/resumen-seguimiento`);
  }

  obtenerPrevisualizacion(proveedorId: number): Observable<PrevisualizacionOrden> {
    return this.http.get<PrevisualizacionOrden>(`${this.apiOrdenes}/previsualizar-propuesta/${proveedorId}`);
  }

  confirmarPedidoFinal(datos: any): Observable<any> {
    return this.http.post(`${this.apiOrdenes}/confirmar`, datos);
  }

  listarOrdenes(): Observable<OrdenCompraResumen[]> { return this.http.get<OrdenCompraResumen[]>(this.apiOrdenes); }
  obtenerOrden(id: number): Observable<OrdenCompraResponse> { return this.http.get<OrdenCompraResponse>(`${this.apiOrdenes}/${id}`); }
  registrarOrden(request: RegistrarOrdenRequest): Observable<OrdenCompraResponse> { return this.http.post<OrdenCompraResponse>(`${this.apiOrdenes}/confirmar`, request); }
  actualizarOrden(id: number, request: RegistrarOrdenRequest): Observable<OrdenCompraResponse> { return this.http.patch<OrdenCompraResponse>(`${this.apiOrdenes}/${id}`, request); }
  cancelarOrden(id: number): Observable<void> { return this.http.delete<void>(`${this.apiOrdenes}/${id}`); }
  registrarRecepcion(request: RegistrarRecepcionRequest): Observable<any> { return this.http.post('/api/recepciones-compra', request); }
  listarRecepciones(): Observable<RecepcionCompraResumen[]> { return this.http.get<RecepcionCompraResumen[]>('/api/recepciones-compra'); }
  listarRecepcionesPorOrden(ordenId: number): Observable<RecepcionCompraResumen[]> { return this.http.get<RecepcionCompraResumen[]>(`/api/recepciones-compra/orden/${ordenId}`); }
  actualizarEstadoPago(recepcionId: number, estadoPago: string, montoPagado: number): Observable<RecepcionCompraResumen> {
    return this.http.patch<RecepcionCompraResumen>(`/api/recepciones-compra/${recepcionId}/estado-pago`, { estadoPago, montoPagado });
  }

  // --- Productos y Lotes ---

  buscarProductos(termino: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiProductos}/buscar?nombre=${encodeURIComponent(termino)}`);
  }

  obtenerLotes(productoId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/inventario/productos/${productoId}/lotes-disponibles`);
  }

  obtenerVenta(id: number): Observable<VentaResponse> {
    return this.http.get<VentaResponse>(`${this.apiVentas}/${id}`);
  }

  // ==================== Analitica de compras ====================

  /** Top productos con mayor gasto en compras del periodo. */
  topProductosComprados(inicio?: string, fin?: string): Observable<TopProductoComprado[]> {
    let params = new HttpParams();
    if (inicio) params = params.set('inicio', inicio);
    if (fin) params = params.set('fin', fin);
    return this.http.get<TopProductoComprado[]>('/api/compras-analitica/top-productos', { params });
  }

  /** % de recepciones a tiempo por proveedor. */
  cumplimientoProveedores(): Observable<CumplimientoProveedor[]> {
    return this.http.get<CumplimientoProveedor[]>('/api/compras-analitica/cumplimiento-proveedores');
  }
}
