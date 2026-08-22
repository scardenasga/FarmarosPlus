import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AnaliticaDashboardResponse, DashboardResponse } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private http = inject(HttpClient);

  private readonly base = '/api/dashboard';

  /**
   * Obtiene la información del dashboard.
   *
   * @param fechaInicio fecha inicial en formato yyyy-MM-dd
   * @param fechaFin fecha final en formato yyyy-MM-dd
   */
  obtenerDashboard(
    fechaInicio?: string,
    fechaFin?: string
  ): Observable<DashboardResponse> {

    let params = new HttpParams();

    if (fechaInicio) {
      params = params.set('fechaInicio', fechaInicio);
    }

    if (fechaFin) {
      params = params.set('fechaFin', fechaFin);
    }

    return this.http.get<DashboardResponse>(
      this.base,
      { params }
    );
  }

  /**
   * Obtiene los datos detallados del módulo de analítica avanzada.
   *
   * @param fechaInicio fecha inicial en formato yyyy-MM-dd
   * @param fechaFin fecha final en formato yyyy-MM-dd
   * @param idCategoria id de categoría opcional para filtrar
   * @param idProducto id de producto opcional para filtrar
   * @param comparar si se debe calcular la comparativa con el período previo
   */
  obtenerAnalitica(
    fechaInicio?: string,
    fechaFin?: string,
    idCategoria?: number | null,
    idProducto?: number | null,
    comparar: boolean = false
  ): Observable<AnaliticaDashboardResponse> {

    let params = new HttpParams();

    if (fechaInicio) {
      params = params.set('fechaInicio', fechaInicio);
    }

    if (fechaFin) {
      params = params.set('fechaFin', fechaFin);
    }

    if (idCategoria !== undefined && idCategoria !== null) {
      params = params.set('idCategoria', idCategoria.toString());
    }

    if (idProducto !== undefined && idProducto !== null) {
      params = params.set('idProducto', idProducto.toString());
    }

    if (comparar) {
      params = params.set('comparar', 'true');
    }

    return this.http.get<AnaliticaDashboardResponse>(
      `${this.base}/analitica`,
      { params }
    );
  }
}