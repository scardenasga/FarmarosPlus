import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { DashboardResponse } from '../models/dashboard.model';

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
}