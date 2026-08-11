import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReporteService {

  private api = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  private construirParams(fechaInicio?: string, fechaFin?: string): HttpParams {
    let params = new HttpParams();
    if (fechaInicio) params = params.set('fechaInicio', this.aFormatoBackend(fechaInicio));
    if (fechaFin) params = params.set('fechaFin', this.aFormatoBackend(fechaFin));
    return params;
  }

  private aFormatoBackend(fechaIso: string): string {
    const [yyyy, mm, dd] = fechaIso.split('-');
    return `${dd}-${mm}-${yyyy}`;
  }

  descargarReporteVentasPdf(fechaInicio?: string, fechaFin?: string, username: string = 'SISTEMA'): Observable<Blob> {
    return this.http.get(`${this.api}/reportes/ventas/pdf`, {
      params: this.construirParams(fechaInicio, fechaFin),
      headers: { 'X-Username': username },
      responseType: 'blob'
    });
  }

  descargarReporteVentasExcel(fechaInicio?: string, fechaFin?: string, username: string = 'SISTEMA'): Observable<Blob> {
    return this.http.get(`${this.api}/reportes/ventas/excel`, {
      params: this.construirParams(fechaInicio, fechaFin),
      headers: { 'X-Username': username },
      responseType: 'blob'
    });
  }
}
