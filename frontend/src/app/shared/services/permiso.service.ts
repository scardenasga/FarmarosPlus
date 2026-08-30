import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SesionService } from './sesion.service';

export interface PermisoDef {
  id: number;
  clave: string;
  descripcion: string;
  modulo: string;
}

@Injectable({ providedIn: 'root' })
export class PermisoService {
  private http = inject(HttpClient);
  private sesion = inject(SesionService);

  private _catalogo = signal<PermisoDef[]>([]);
  private _permisos = signal<Set<string>>(this.cargarInicial());

  private cargarInicial(): Set<string> {
    try {
      const raw = localStorage.getItem('farmaros.permisos');
      if (raw) return new Set(JSON.parse(raw) as string[]);
    } catch {}
    return new Set();
  }

  catalogo = this._catalogo.asReadonly();
  permisos = this._permisos.asReadonly();

  // Para preview vendedor, se filtra a solo los que un vendedor vería (no ADMIN)
  private readonly VENDEDOR_BASE = new Set(['DASHBOARD_VER','VENTAS_VER','VENTAS_CREAR','INVENTARIO_VER','PROVEEDORES_VER','CONFIG_VER']);

  tiene(clave: string): boolean {
    // TEMP DEBUG: always true to test responsiveness
    try {
      if (this.sesion.esPreviewActivo() && this.sesion.usuario().rol?.toUpperCase() === 'ADMIN') {
        return this.VENDEDOR_BASE.has(clave);
      }
      // if no permisos loaded yet, allow all to avoid blank sidebar
      if (this._permisos().size === 0) return true;
      return this._permisos().has(clave);
    } catch { return true; }
  }

  tieneAlguno(claves: string[]): boolean {
    return claves.some(c => this.tiene(c));
  }

  cargarCatalogo(): void {
    this.http.get<PermisoDef[]>('/api/permisos').subscribe({
      next: d => this._catalogo.set(d ?? []),
      error: () => {}
    });
  }

  cargarParaUsuario(idUsuario: number): void {
    this.http.get<string[]>(`/api/usuarios/${idUsuario}/permisos`).subscribe({
      next: d => this._permisos.set(new Set(d ?? [])),
      error: () => this._permisos.set(new Set())
    });
  }

  obtenerPermisosUsuario(idUsuario: number): import('rxjs').Observable<string[]> {
    return this.http.get<string[]>(`/api/usuarios/${idUsuario}/permisos`);
  }

  guardarPermisosUsuario(idUsuario: number, permisosMap: Record<string, boolean>): import('rxjs').Observable<string[]> {
    return this.http.put<string[]>(`/api/usuarios/${idUsuario}/permisos`, permisosMap);
  }

  setDesdeLogin(permisos: string[] | Set<string> | undefined): void {
    if (!permisos) { this._permisos.set(new Set()); return; }
    const set = permisos instanceof Set ? permisos : new Set(permisos);
    this._permisos.set(set);
  }

  // Preview: reemplaza temporalmente con permisos de VENDEDOR base
  activarPreviewVendedor(permisosVendedor: string[]): void {
    this._permisos.set(new Set(permisosVendedor));
  }

  agrupadoPorModulo = computed(() => {
    const map = new Map<string, PermisoDef[]>();
    for (const p of this._catalogo()) {
      const m = p.modulo || 'OTROS';
      if (!map.has(m)) map.set(m, []);
      map.get(m)!.push(p);
    }
    return map;
  });
}
