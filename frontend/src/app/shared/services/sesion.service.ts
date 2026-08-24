import { Injectable, signal } from '@angular/core';

/**
 * Usuario activo de la aplicación mientras no exista un módulo de login.
 * Cuando se implemente autenticación, solo este servicio debe cambiar:
 * el resto de la app consume `usuario()` / `setUsuario()`.
 */
export interface SesionUsuario {
  idUsuario: number;
  username: string;
  nombreCompleto?: string;
  rol?: string;
}

const CLAVE_SESION = 'farmaros.sesion';

/**
 * Único punto con un usuario temporal, usado solo si no hay sesión guardada
 * en localStorage. Coincide con el usuario base que crea DataInitializer
 * en el backend. NO duplicar este valor en otros componentes o servicios.
 */
const USUARIO_TEMPORAL: SesionUsuario = {
  idUsuario: 1,
  username: 'SISTEMA',
  nombreCompleto: 'USUARIO DE SISTEMA',
  rol: 'ADMIN'
};

@Injectable({ providedIn: 'root' })
export class SesionService {

  private readonly _usuario = signal<SesionUsuario>(this.cargarDeStorage());

  /** Usuario actual de la sesión (solo lectura). */
  readonly usuario = this._usuario.asReadonly();

  username(): string {
    return this._usuario().username;
  }

  idUsuario(): number {
    return this._usuario().idUsuario;
  }

  esAdminORegente(): boolean {
    const rol = this._usuario().rol?.toUpperCase();
    return rol === 'ADMIN' || rol === 'REGENTE';
  }

  setUsuario(usuario: SesionUsuario): void {
    this._usuario.set(usuario);
    localStorage.setItem(CLAVE_SESION, JSON.stringify(usuario));
  }

  limpiar(): void {
    localStorage.removeItem(CLAVE_SESION);
    this._usuario.set(USUARIO_TEMPORAL);
  }

  private cargarDeStorage(): SesionUsuario {
    try {
      const raw = localStorage.getItem(CLAVE_SESION);
      if (!raw) return USUARIO_TEMPORAL;
      const parsed = JSON.parse(raw) as SesionUsuario;
      if (!parsed?.idUsuario || !parsed?.username) return USUARIO_TEMPORAL;
      return parsed;
    } catch {
      return USUARIO_TEMPORAL;
    }
  }
}
