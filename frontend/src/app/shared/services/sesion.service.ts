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

const CLAVE_PREVIEW = 'farmaros.previewVendedor';

@Injectable({ providedIn: 'root' })
export class SesionService {

  private readonly _usuario = signal<SesionUsuario>(this.cargarDeStorage());
  private readonly _previewVendedor = signal<boolean>(this.cargarPreview());

  /** Usuario actual de la sesión (solo lectura). */
  readonly usuario = this._usuario.asReadonly();
  /** Vista previa como vendedor activa (solo ADMIN real puede activarla) */
  readonly previewVendedor = this._previewVendedor.asReadonly();

  /** Rol efectivo teniendo en cuenta la preview */
  rolEfectivo(): string {
    const real = this._usuario().rol?.toUpperCase() || '';
    if (this._previewVendedor() && real === 'ADMIN') return 'VENDEDOR';
    return real;
  }

  /** Usuario efectivo para UI (rol sobreescrito en preview) */
  usuarioEfectivo(): SesionUsuario {
    const u = this._usuario();
    if (this._previewVendedor() && u.rol?.toUpperCase() === 'ADMIN') {
      return { ...u, rol: 'VENDEDOR' };
    }
    return u;
  }

  esPreviewActivo(): boolean {
    return this._previewVendedor();
  }

  puedePreview(): boolean {
    return this._usuario().rol?.toUpperCase() === 'ADMIN';
  }

  togglePreview(): void {
    if (!this.puedePreview()) return;
    const nuevo = !this._previewVendedor();
    this._previewVendedor.set(nuevo);
    try { localStorage.setItem(CLAVE_PREVIEW, nuevo ? '1' : '0'); } catch {}
  }

  activarPreview(): void {
    if (!this.puedePreview()) return;
    this._previewVendedor.set(true);
    try { localStorage.setItem(CLAVE_PREVIEW, '1'); } catch {}
  }

  desactivarPreview(): void {
    this._previewVendedor.set(false);
    try { localStorage.removeItem(CLAVE_PREVIEW); } catch {}
  }

  username(): string {
    return this._usuario().username;
  }

  idUsuario(): number {
    return this._usuario().idUsuario;
  }

  esAdminORegente(): boolean {
    const rol = this.rolEfectivo();
    return rol === 'ADMIN' || rol === 'REGENTE';
  }

  esAdmin(): boolean {
    return this.rolEfectivo() === 'ADMIN';
  }

  esVendedor(): boolean {
    const rol = this.rolEfectivo();
    return rol === 'VENDEDOR' || rol === 'EMPLEADO';
  }

  isAuthenticated(): boolean {
    const u = this._usuario();
    // Si sigue siendo el fallback SISTEMA sin haber hecho login, lo consideramos no autenticado si no hay clave en storage
    try {
      const raw = localStorage.getItem(CLAVE_SESION);
      if (!raw) return false;
      const parsed = JSON.parse(raw) as SesionUsuario;
      return !!parsed?.username && !!parsed?.idUsuario;
    } catch {
      return false;
    }
  }

  setUsuario(usuario: SesionUsuario): void {
    this._usuario.set(usuario);
    localStorage.setItem(CLAVE_SESION, JSON.stringify(usuario));
  }

  limpiar(): void {
    localStorage.removeItem(CLAVE_SESION);
    localStorage.removeItem(CLAVE_PREVIEW);
    this._previewVendedor.set(false);
    this._usuario.set(USUARIO_TEMPORAL);
  }

  private cargarPreview(): boolean {
    try { return localStorage.getItem(CLAVE_PREVIEW) === '1'; } catch { return false; }
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
