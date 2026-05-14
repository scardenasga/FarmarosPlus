import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AlertaResponse {
  id: number;
  tipo: 'STOCK_MINIMO' | 'PROXIMO_VENCIMIENTO';
  titulo: string;
  mensaje: string;
  leida: boolean;
  fechaGeneracion: string;
}

@Injectable({ providedIn: 'root' })
export class AlertaService {
  private readonly base = 'http://localhost:8080/api/alertas';

  contadorNoLeidas = signal<number>(0);

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

  actualizarContador(): void {
    this.listarAlertas(true).subscribe({
      next: (alertas) => this.contadorNoLeidas.set(alertas.length),
      error: () => {}
    });
  }

  generarYActualizar(): void {
    this.generarAlertas().subscribe({
      next: (alertas) => this.contadorNoLeidas.set(alertas.filter(a => !a.leida).length),
      error: () => this.actualizarContador()
    });
  }
}
