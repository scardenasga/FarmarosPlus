import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecomendacionResponse } from '../models/recomendacion.model';

@Injectable({ providedIn: 'root' })
export class RecomendacionService {
  private http = inject(HttpClient);
  private api = '/api/recomendaciones';

  obtenerRecomendaciones(productoIds: number[], limit = 5): Observable<RecomendacionResponse> {
    let params = new HttpParams();
    if (productoIds.length) {
      params = params.set('productoIds', productoIds.join(','));
    }
    params = params.set('limit', String(limit));
    return this.http.get<RecomendacionResponse>(`${this.api}/pos`, { params });
  }
}
