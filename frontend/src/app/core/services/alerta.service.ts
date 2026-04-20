import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AlertaResponse {
  idAlerta: number;
  tipo: 'STOCK_MINIMO' | 'PROXIMO_VENCIMIENTO';
  idProducto: number;
  nombreProducto: string;
  idLote: number | null;
  numeroLote: string | null;
  cantidadActual: number | null;
  stockMinimo: number | null;
  fechaVencimiento: string | null;
  leida: boolean;
  fechaGeneracion: string;
}

export interface ConfiguracionAlertaResponse {
  diasProximoVencimiento: number;
}

@Injectable({ providedIn: 'root' })
export class AlertaService {
  private readonly base = 'http://localhost:8080/api/alertas';

  constructor(private http: HttpClient) {}

  generarAlertas(): Observable<AlertaResponse[]> {
    return this.http.post<AlertaResponse[]>(`${this.base}/generar`, {});
  }

  listarAlertas(soloNoLeidas = false): Observable<AlertaResponse[]> {
    return this.http.get<AlertaResponse[]>(`${this.base}?soloNoLeidas=${soloNoLeidas}`);
  }

  marcarLeida(id: number): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/leer`, {});
  }

  marcarTodasLeidas(): Observable<void> {
    return this.http.patch<void>(`${this.base}/leer-todas`, {});
  }

  obtenerConfiguracion(): Observable<ConfiguracionAlertaResponse> {
    return this.http.get<ConfiguracionAlertaResponse>(`${this.base}/configuracion`);
  }

  actualizarConfiguracion(dias: number): Observable<ConfiguracionAlertaResponse> {
    return this.http.put<ConfiguracionAlertaResponse>(`${this.base}/configuracion`, {
      diasProximoVencimiento: dias
    });
  }
}
