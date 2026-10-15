import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { SesionService, SesionUsuario } from '../../shared/services/sesion.service';
import { PermisoService } from '../../shared/services/permiso.service';

export interface LoginResponse {
  id: number;
  username: string;
  nombreCompleto: string;
  rol: string;
  estado: string;
  ultimoAcceso: string | null;
  permisos?: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private sesion = inject(SesionService);
  private permisoService = inject(PermisoService);
  private router = inject(Router);

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { username, password }).pipe(
      tap(res => {
        const sesionUsuario: SesionUsuario = {
          idUsuario: res.id,
          username: res.username,
          nombreCompleto: res.nombreCompleto,
          rol: res.rol
        };
        this.sesion.setUsuario(sesionUsuario);
        this.permisoService.setDesdeLogin(res.permisos);
        try { localStorage.setItem('farmaros.permisos', JSON.stringify(res.permisos ?? [])); } catch {}
      })
    );
  }

  logout(): void {
    this.sesion.limpiar();
    localStorage.removeItem('farmaros.sesion');
    localStorage.removeItem('farmaros.permisos');
    localStorage.removeItem('farmaros.previewVendedor');
    this.router.navigateByUrl('/login');
  }

  isAuthenticated(): boolean {
    return this.sesion.isAuthenticated();
  }
}
