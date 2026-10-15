import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RendimientoService {
  private api = '/api/rendimiento';

  constructor(private http: HttpClient) {}

  private aFormatoBackend(fechaIso: string): string {
    // input yyyy-MM-dd -> dd-MM-yyyy
    if (!fechaIso) return '';
    const [y, m, d] = fechaIso.split('-');
    return `${d}-${m}-${y}`;
  }

  descargarPdf(desde?: string, hasta?: string): Observable<Blob> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', this.aFormatoBackend(desde));
    if (hasta) params = params.set('hasta', this.aFormatoBackend(hasta));
    return this.http.get(`${this.api}/reporte/pdf`, { params, responseType: 'blob' });
  }

  descargarExcel(desde?: string, hasta?: string): Observable<Blob> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', this.aFormatoBackend(desde));
    if (hasta) params = params.set('hasta', this.aFormatoBackend(hasta));
    return this.http.get(`${this.api}/reporte/excel`, { params, responseType: 'blob' });
  }
}
