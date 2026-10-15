import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ConfiguracionGanancia {
  porcentajeMinimo: number;
}

@Injectable({ providedIn: 'root' })
export class ConfiguracionService {
  private api = '/api/configuracion';

  constructor(private http: HttpClient) {}

  obtenerGanancia(): Observable<ConfiguracionGanancia> {
    return this.http.get<ConfiguracionGanancia>(`${this.api}/ganancia`);
  }

  actualizarGanancia(porcentajeMinimo: number): Observable<ConfiguracionGanancia> {
    return this.http.put<ConfiguracionGanancia>(`${this.api}/ganancia`, { porcentajeMinimo });
  }
}
