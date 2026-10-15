import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

import { SesionService } from '../../shared/services/sesion.service';
import {
  AnularVentaRequest,
  CrearVentaRequest,
  ProductoResponse,
  Venta
} from '../models/venta.model';

@Injectable({ providedIn: 'root' })
export class VentaService {

  private api = '/api';
  private sesion = inject(SesionService);

  constructor(private http: HttpClient) {}

  registrarVenta(request: CrearVentaRequest): Observable<Venta> {
    return this.http.post<Venta>(`${this.api}/ventas`, request);
  }

  obtenerVenta(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${this.api}/ventas/${id}`);
  }

  anularVenta(id: number, request: AnularVentaRequest): Observable<Venta> {
    return this.http.patch<Venta>(`${this.api}/ventas/${id}/anular`, request);
  }

  eliminarVenta(id: number, request: AnularVentaRequest): Observable<Venta> {
    return this.http.delete<Venta>(`${this.api}/ventas/${id}`, { body: request });
  }

  buscarProductos(termino: string): Observable<ProductoResponse[]> {
    const t = termino.trim();
    // El backend busca por un solo campo (nombre o codigo); se consultan
    // ambos en paralelo y se combinan sin duplicados.
    return forkJoin({
      porNombre: this.http.get<ProductoResponse[]>(`${this.api}/productos/buscar?nombre=${encodeURIComponent(t)}`),
      porCodigo: this.http.get<ProductoResponse[]>(`${this.api}/productos/buscar?codigo=${encodeURIComponent(t)}`)
    }).pipe(
      map(({ porNombre, porCodigo }) => {
        const vistos = new Set(porNombre.map(p => p.id));
        return [...porNombre, ...porCodigo.filter(p => !vistos.has(p.id))];
      })
    );
  }

  obtenerProductosActivos(): Observable<ProductoResponse[]> {
    return this.http.get<ProductoResponse[]>(`${this.api}/productos/activos`);
  }

  obtenerLotesDisponibles(productoId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/inventario/productos/${productoId}/lotes-disponibles`);
  }

  /**
   * Consulta el histórico de ventas.
   * El backend espera las fechas en formato dd-MM-yyyy; si se reciben en ISO
   * (yyyy-MM-dd, formato de <input type="date">) se convierten automáticamente.
   */
  consultarHistorico(inicio?: string, fin?: string, estado?: string): Observable<Venta[]> {
    let params = new HttpParams();
    if (inicio) params = params.set('fechaInicio', this.normalizarFecha(inicio));
    if (fin) params = params.set('fechaFin', this.normalizarFecha(fin));
    if (estado) params = params.set('estado', estado);

    return this.http.get<Venta[]>(`${this.api}/ventas/historico`, {
      params,
      headers: { 'X-Username': this.sesion.username() }
    });
  }

  descargarFactura(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/ventas/${id}/factura`, { responseType: 'blob' });
  }

  /** Convierte yyyy-MM-dd a dd-MM-yyyy. Deja pasar cualquier otro formato. */
  private normalizarFecha(fecha: string): string {
    const iso = /^(\d{4})-(\d{2})-(\d{2})$/.exec(fecha.trim());
    if (!iso) return fecha;
    return `${iso[3]}-${iso[2]}-${iso[1]}`;
  }
}
