import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UsuarioAdmin {
  id: number;
  username: string;
  nombreCompleto: string;
  rol: string;
  estado: string;
  ultimoAcceso: string | null;
}

export interface CrearUsuarioPayload {
  username: string;
  passwordHash: string;
  nombreCompleto: string;
  rol: string;
  estado?: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioAdminService {
  private http = inject(HttpClient);

  listar(q?: string): Observable<UsuarioAdmin[]> {
    let params = new HttpParams();
    if (q?.trim()) params = params.set('q', q.trim());
    return this.http.get<UsuarioAdmin[]>('/api/usuarios', { params });
  }

  crear(payload: CrearUsuarioPayload): Observable<UsuarioAdmin> {
    return this.http.post<UsuarioAdmin>('/api/usuarios', payload);
  }

  actualizarEstado(id: number, estado: string): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`/api/usuarios/${id}/estado`, { estado });
  }
}
