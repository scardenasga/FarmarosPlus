import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class VentaService {

  private api = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  registrarVenta(request: any): Observable<any> {
    return this.http.post(`${this.api}/ventas`, request);
  }

  obtenerVenta(id: number): Observable<any> {
    return this.http.get(`${this.api}/ventas/${id}`);
  }

  anularVenta(id: number, request: any): Observable<any> {
    return this.http.patch(`${this.api}/ventas/${id}/anular`, request);
  }

  eliminarVenta(id: number, request: any): Observable<any> {
    return this.http.delete(`${this.api}/ventas/${id}`, { body: request });
  }

  buscarProductos(termino: string, criterio: string = 'nombre'): Observable<any[]> {
    const param = criterio === 'codigo' ? 'codigo' : 'nombre';
    return this.http.get<any[]>(`${this.api}/productos/buscar?${param}=${termino}`);
  }

  obtenerLotesDisponibles(productoId: number): Observable<any[]> {
  return this.http.get<any[]>(`${this.api}/inventario/productos/${productoId}/lotes-disponibles`);
}

consultarHistorico(inicio?: string, fin?: string): Observable<any[]> {
  const url = 'http://localhost:8080/api/ventas/historico';
  
  let params = new HttpParams();
  if (inicio) params = params.set('fechaInicio', inicio);
  if (fin) params = params.set('fechaFin', fin);

  const headers = new HttpHeaders().set('X-Username', 'Tatiana'); // ← esto debe estar

  return this.http.get<any[]>(url, { params, headers }); // ← params Y headers juntos
}

obtenerPrevisualizacion(proveedorId: number): Observable<any> {
    return this.http.get(`${this.api}/previsualizar/${proveedorId}`);
  }

  confirmarPedidoFinal(datosOrden: any): Observable<any> {
    return this.http.post(`${this.api}/confirmar`, datosOrden);
  }
}
