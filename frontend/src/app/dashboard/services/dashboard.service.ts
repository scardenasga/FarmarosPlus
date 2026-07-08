import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private readonly base = '/api/dashboard';

  private toBackendDate(isoDate: string): string {
    const [yyyy, mm, dd] = isoDate.split('-');
    return `${dd}-${mm}-${yyyy}`;
  }

  obtenerDashboard(fechaInicio?: string, fechaFin?: string): Observable<DashboardResponse> {
    let params = new HttpParams();
    if (fechaInicio) params = params.set('fechaInicio', this.toBackendDate(fechaInicio));
    if (fechaFin) params = params.set('fechaFin', this.toBackendDate(fechaFin));
    return this.http.get<DashboardResponse>(this.base, { params });
  }
}