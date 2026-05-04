import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
consultarHistorico(
  fechaInicio?: string,
  fechaFin?: string,
  idVendedor?: number,
  estado?: string,
  username: string = 'admin'
): Observable<any[]> {
  let params: any = {};
  if (fechaInicio) params.fechaInicio = fechaInicio;
  if (fechaFin) params.fechaFin = fechaFin;
  if (idVendedor) params.idVendedor = idVendedor;
  if (estado) params.estado = estado;

  return this.http.get<any[]>(`${this.api}/ventas/historico`, {
    params,
    headers: { 'X-Username': username }
  });
}
}
