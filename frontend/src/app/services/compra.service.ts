import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CompraService {
  // Ajusta esta URL a la de tu backend (usualmente es 8080)
  private apiUrl = 'http://localhost:8080/api/ordenes-compra';

  constructor(private http: HttpClient) { }

  // Este es el método que necesita tu componente de notificaciones
  getResumenSeguimiento(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/resumen-seguimiento`);
  }

previsualizar(idProveedor: number) {
  return this.http.get(`/api/ordenes-compra/previsualizar-propuesta/${idProveedor}`);
}

confirmarOrden(data: any) {
  return this.http.post(`/api/ordenes-compra/confirmar`, data);
}
obtenerPrevisualizacion(proveedorId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/previsualizar-propuesta/${proveedorId}`);
  }
  confirmarPedidoFinal(datos: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/confirmar`, datos);
  }


  // Paso 1: Crea la orden básica
  crearOrden(ordenData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, ordenData);
  }

  // Paso 2: Agrega cada producto de la lista a la orden creada
  agregarDetalle(ordenId: number, detalle: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${ordenId}/detalles`, detalle);
  }

  // El método de alertas que ya tenías
  getResumenAlertas(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/resumen-seguimiento`);
  }
  // En compra.service.ts
buscarProductos(termino: string): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/productos/buscar?nombre=${termino}`);
}
}
