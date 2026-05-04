import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CompraService {
  // Ajusta esta URL a la de tu backend (usualmente es 8080)
  private apiUrl = 'http://localhost:8080/api/ordenes-compra';

  constructor(private http: HttpClient) { }

  // Este es el método que necesita tu componente de notificaciones
  getResumenSeguimiento(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/resumen-seguimiento`);
  }
}