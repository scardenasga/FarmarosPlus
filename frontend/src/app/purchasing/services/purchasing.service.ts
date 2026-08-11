import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  CompraResponse, 
  DevolucionResponse, 
  RegistrarCompraRequest, 
  RegistrarDevolucionRequest,
  ResumenSeguimiento,
  PrevisualizacionOrden
} from '../models/purchasing.model';

@Injectable({
  providedIn: 'root'
})
export class PurchasingService {
  private http = inject(HttpClient);
  private readonly apiCompras = '/api/compras';
  private readonly apiDevoluciones = '/api/devoluciones';
  private readonly apiOrdenes = '/api/ordenes-compra';
  private readonly apiProductos = '/api/productos';

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

  // --- Órdenes de Compra ---

  getResumenSeguimiento(): Observable<ResumenSeguimiento> {
    return this.http.get<ResumenSeguimiento>(`${this.apiOrdenes}/resumen-seguimiento`);
  }

  obtenerPrevisualizacion(proveedorId: number): Observable<PrevisualizacionOrden> {
    return this.http.get<PrevisualizacionOrden>(`${this.apiOrdenes}/previsualizar-propuesta/${proveedorId}`);
  }

  confirmarPedidoFinal(datos: any): Observable<any> {
    return this.http.post(`${this.apiOrdenes}/confirmar`, datos);
  }

  // --- Productos y Lotes ---

  buscarProductos(termino: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiProductos}/buscar?nombre=${encodeURIComponent(termino)}`);
  }

  obtenerLotes(productoId: number): Observable<any[]> {
    // Corregido: de /api/inventario/... a /api/productos/...
    return this.http.get<any[]>(`${this.apiProductos}/${productoId}/lotes-disponibles`);
  }
}
