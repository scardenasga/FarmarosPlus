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

  private _previewBackup: Set<string> | null = null;
  private readonly VENDEDOR_BASE = new Set(['DASHBOARD_VER','VENTAS_VER','VENTAS_CREAR','INVENTARIO_VER','PROVEEDORES_VER','CONFIG_VER']);

  tiene(clave: string): boolean {
    try {
      // si no hay permisos cargados aún, permitir para no dejar sidebar vacío en primer render
      if (this._permisos().size === 0) return true;
      return this._permisos().has(clave);
    } catch { return true; }
  }

  iniciarPreview(permisosPreview: string[]): void {
    if (!this._previewBackup) this._previewBackup = new Set(this._permisos());
    this._permisos.set(new Set(permisosPreview));
    this.sesion.activarPreview();
  }

  iniciarPreviewVendedorBase(): void {
    this.iniciarPreview(Array.from(this.VENDEDOR_BASE));
  }

  salirPreview(): void {
    if (this._previewBackup) {
      this._permisos.set(this._previewBackup);
      this._previewBackup = null;
    } else {
      try {
        const raw = localStorage.getItem('farmaros.permisos');
        if (raw) this._permisos.set(new Set(JSON.parse(raw) as string[]));
      } catch {}
    }
    this.sesion.desactivarPreview();
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
