import { Component, inject, computed, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ThemeService, ThemeType } from '../../../services/theme.service';
import { SesionService } from '../../../shared/services/sesion.service';
import { AuthService } from '../../../auth/services/auth.service';
import { UsuarioAdminService, UsuarioAdmin, CrearUsuarioPayload } from '../../services/usuario-admin.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styles: [`
    .settings-page {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      background-color: var(--bg-main, var(--background));
    }

    /* Header alineado a Inventario / Gestión de Compras */
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: var(--space-l);
      padding: var(--space-l) var(--space-xl) var(--space-s);
      flex-wrap: wrap;
    }

    .header-text h1 {
      margin: 0;
      font-size: 1.3rem;
      font-weight: 800;
      color: var(--text-dark, var(--on-background));
    }

    .header-subtitle {
      margin: 4px 0 0;
      font-size: 0.78rem;
      color: var(--text-muted, var(--outline));
      font-weight: 500;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: var(--space-s);
    }

    .btn-ghost {
      display: inline-flex;
      align-items: center;
      gap: var(--space-xs);
      padding: 8px 14px;
      border-radius: 10px;
      border: 1px solid var(--border-color, var(--outline-variant));
      background-color: var(--card-bg, var(--surface-container-low));
      color: var(--text-dark, var(--on-background));
      font-weight: 700;
      font-size: 0.78rem;
    }

    .btn-ghost svg { width: 14px; height: 14px; }

    .settings-content {
      padding: 0 var(--space-xl) var(--space-xl);
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: var(--space-l);
      align-items: start;
    }

    .settings-content > .span-2 { grid-column: 1 / -1; }

    @media (max-width: 980px) {
      .settings-content { grid-template-columns: 1fr; }
      .settings-content > .span-2 { grid-column: auto; }
    }

    .section-card {
      background-color: var(--card-bg, var(--surface-container-low));
      border: 1px solid var(--border-color, var(--outline-variant));
      border-radius: 16px;
      box-shadow: var(--shadow-card, 0 4px 12px rgba(0,0,0,0.05));
      overflow: hidden;
    }

    .section-card-header {
      display: flex;
      align-items: center;
      gap: var(--space-m);
      padding: var(--space-l) var(--space-l) var(--space-m);
      border-bottom: 1px solid var(--border-color, var(--outline-variant));
    }

    .section-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: grid;
      place-items: center;
      flex-shrink: 0;
    }

    .section-icon svg { width: 18px; height: 18px; }

    .icon-teal { background-color: var(--success-bg); color: var(--success-text); }
    .icon-purple { background-color: var(--purple-bg); color: var(--purple-text); }
    .icon-blue { background-color: var(--info-bg); color: var(--info-text); }

    .section-card-header h2 {
      margin: 0;
      font-size: 0.92rem;
      font-weight: 800;
      color: var(--text-dark, var(--on-background));
    }

    .section-desc {
      margin: 2px 0 0;
      font-size: 0.72rem;
      color: var(--text-muted, var(--outline));
      font-weight: 500;
    }

    .section-body {
      padding: var(--space-l);
    }

    /* Sesión */
    .sesion-main {
      display: flex;
      align-items: center;
      gap: var(--space-l);
      flex-wrap: wrap;
    }

    .avatar {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background: var(--accent-green-dark, var(--primary-container));
      color: var(--on-primary-container);
      display: grid;
      place-items: center;
      font-weight: 800;
      font-size: 18px;
      flex-shrink: 0;
      box-shadow: var(--shadow-1);
    }

    .sesion-meta { flex: 1; min-width: 180px; }

    .sesion-nombre {
      font-size: 0.95rem;
      font-weight: 800;
      color: var(--text-dark, var(--on-background));
      margin: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .sesion-username {
      font-size: 0.76rem;
      color: var(--text-muted, var(--outline));
      margin: 2px 0 0 0;
      font-weight: 600;
    }

    .badge-rol {
      font-size: 0.62rem;
      text-transform: uppercase;
      letter-spacing: 0.6px;
      font-weight: 800;
      padding: 6px 12px;
      border-radius: 9999px;
      flex-shrink: 0;
    }

    .badge-rol.admin { background: var(--primary-container); color: var(--on-primary-container); }
    .badge-rol.vendor { background: var(--secondary-container); color: var(--on-secondary-container); }

    .sesion-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: var(--space-m);
      margin-top: var(--space-l);
      background: var(--bg-main, var(--surface-container));
      border: 1px solid var(--border-color, var(--outline-variant));
      border-radius: 12px;
      padding: var(--space-m);
    }

    .detail-item { display: flex; flex-direction: column; gap: 4px; }

    .detail-label {
      font-size: 0.62rem;
      font-weight: 800;
      color: var(--text-muted, var(--outline));
      text-transform: uppercase;
      letter-spacing: 0.7px;
    }

    .detail-value {
      font-size: 0.82rem;
      font-weight: 700;
      color: var(--text-dark, var(--on-background));
    }

    .detail-value.mono { font-family: monospace; font-size: 0.76rem; font-weight: 600; }

    .sesion-actions {
      margin-top: var(--space-l);
      display: flex;
      gap: var(--space-s);
      flex-wrap: wrap;
    }

    .btn-primary {
      display: inline-flex;
      align-items: center;
      gap: var(--space-s);
      padding: 9px 16px;
      border-radius: 10px;
      background-color: var(--accent-green-dark, var(--primary-container));
      color: var(--on-primary-container);
      font-weight: 800;
      font-size: 0.78rem;
      box-shadow: var(--shadow-card);
    }

    .btn-danger {
      display: inline-flex;
      align-items: center;
      gap: var(--space-s);
      padding: 9px 16px;
      border-radius: 10px;
      background-color: var(--danger-bg);
      color: var(--danger-text);
      border: 1px solid color-mix(in srgb, var(--danger-text) 14%, transparent);
      font-weight: 800;
      font-size: 0.78rem;
    }

    .btn-danger svg, .btn-primary svg { width: 14px; height: 14px; }

    .btn-primary:hover, .btn-danger:hover, .btn-ghost:hover { filter: brightness(1.06); }

    /* Temas - grid como tarjetas de producto */
    .theme-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: var(--space-m);
    }

    .theme-card {
      background-color: var(--bg-main, var(--surface-container-low));
      border: 1px solid var(--border-color, var(--outline-variant));
      border-radius: 12px;
      padding: var(--space-m);
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-m);
      transition: border-color 0.2s, box-shadow 0.2s, transform 0.1s;
    }

    .theme-card:hover {
      border-color: var(--primary);
      box-shadow: var(--shadow-card);
      transform: translateY(-2px);
    }

    .theme-card.active {
      border-color: var(--primary);
      background-color: var(--primary-container);
      color: var(--on-primary-container);
      box-shadow: var(--shadow-card);
    }

    .theme-preview {
      width: 100%;
      height: 56px;
      border-radius: 10px;
      display: flex;
      overflow: hidden;
      border: 1px solid var(--border-color, var(--outline-variant));
      box-shadow: inset 0 1px 3px rgba(0,0,0,0.08);
    }

    .preview-color { flex: 1; }

    .theme-name {
      font-size: 0.74rem;
      font-weight: 800;
      text-align: center;
      line-height: 1.2;
    }

    .theme-card.active .theme-name { color: var(--on-primary-container); }

    .info-row {
      display: flex;
      align-items: center;
      gap: var(--space-m);
      font-size: 0.82rem;
      color: var(--text-muted, var(--outline));
      font-weight: 600;
    }

    .info-row strong { color: var(--text-dark, var(--on-background)); }

    /* Gestión usuarios - toolbar igual a inventario */
    .usuarios-toolbar {
      display: flex;
      align-items: center;
      gap: var(--space-m);
      flex-wrap: wrap;
      margin-bottom: var(--space-m);
    }

    .usuarios-toolbar-search {
      flex: 1;
      min-width: 220px;
      position: relative;
    }

    .usuarios-toolbar-search svg {
      position: absolute;
      left: 10px;
      top: 50%;
      transform: translateY(-50%);
      width: 16px;
      height: 16px;
      color: var(--text-muted);
      pointer-events: none;
    }

    .usuarios-toolbar-search input {
      width: 100%;
      height: 38px;
      padding: 0 12px 0 34px;
      border: 1px solid var(--border-color);
      border-radius: 10px;
      background: var(--bg-main);
      color: var(--text-dark);
      font-size: 0.82rem;
      font-weight: 600;
    }

    .usuarios-toolbar-search input::placeholder { color: var(--text-muted); opacity: 0.8; }

    .usuarios-conteo {
      font-size: 0.65rem;
      text-transform: uppercase;
      letter-spacing: 1px;
      font-weight: 800;
      color: var(--text-muted);
      margin-bottom: var(--space-s);
    }

    .usuarios-tabla-wrap {
      border: 1px solid var(--border-color);
      border-radius: 12px;
      overflow: hidden;
      background: var(--bg-main);
      max-height: 260px;
      overflow-y: auto;
    }

    /* Compactar info para no alargar scroll */
    .section-card.compact .section-body { padding-top: var(--space-m); }

    .usuarios-tabla {
      width: 100%;
      border-collapse: collapse;
      min-width: 620px;
    }

    .usuarios-tabla thead th {
      padding: 10px var(--space-m);
      font-size: 0.62rem;
      text-transform: uppercase;
      letter-spacing: 0.8px;
      font-weight: 800;
      color: var(--text-muted);
      border-bottom: 1px solid var(--border-color);
      background: var(--card-bg);
      text-align: left;
      white-space: nowrap;
    }

    .usuarios-tabla tbody td {
      padding: 11px var(--space-m);
      border-bottom: 1px solid var(--border-color);
      font-size: 0.82rem;
      color: var(--text-dark);
    }

    .usuarios-tabla tbody tr:last-child td { border-bottom: none; }
    .usuarios-tabla tbody tr:hover { background: var(--surface-container-high); }

    .user-cell { display: flex; align-items: center; gap: var(--space-s); font-weight: 700; }
    .mini-avatar {
      width: 28px; height: 28px; border-radius: 50%;
      display: grid; place-items: center;
      background: var(--surface-container-high);
      color: var(--text-dark);
      font-weight: 800; font-size: 12px; flex-shrink: 0;
    }

    .estado-pill, .rol-pill {
      display: inline-block;
      font-size: 0.60rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      padding: 4px 10px;
      border-radius: 9999px;
      white-space: nowrap;
    }

    .estado-activo { background: var(--success-bg); color: var(--success-text); }
    .estado-inactivo { background: var(--surface-container-high); color: var(--text-muted); }
    .rol-admin { background: var(--primary-container); color: var(--on-primary-container); }
    .rol-vendedor { background: var(--secondary-container); color: var(--on-secondary-container); }

    .icon-btn {
      width: 30px; height: 30px; border-radius: 8px;
      display: inline-flex; align-items: center; justify-content: center;
      color: var(--text-muted);
    }
    .icon-btn:hover { background: var(--surface-container-high); color: var(--text-dark); }
    .icon-btn.danger:hover { background: var(--danger-bg); color: var(--danger-text); }
    .icon-btn svg { width: 14px; height: 14px; }

    .form-card {
      margin-top: var(--space-m);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      background: var(--bg-main);
      padding: var(--space-l);
      display: flex;
      flex-direction: column;
      gap: var(--space-m);
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: var(--space-m);
    }

    .form-field { display: flex; flex-direction: column; gap: 6px; }
    .form-field label {
      font-size: 0.68rem; font-weight: 800;
      text-transform: uppercase; letter-spacing: 0.5px;
      color: var(--text-muted);
    }
    .form-field input, .form-field select {
      height: 38px;
      padding: 0 12px;
      border: 1px solid var(--border-color);
      border-radius: 10px;
      background: var(--card-bg);
      color: var(--text-dark);
      font-size: 0.82rem;
      font-weight: 600;
    }

    .form-actions { display: flex; gap: var(--space-s); flex-wrap: wrap; justify-content: flex-end; }

    .btn-secondary {
      padding: 9px 16px; border-radius: 10px;
      border: 1px solid var(--border-color);
      background: transparent;
      color: var(--text-dark);
      font-weight: 800; font-size: 0.78rem;
    }

    .error-banner {
      background: var(--danger-bg); color: var(--danger-text);
      border: 1px solid color-mix(in srgb, var(--danger-text) 14%, transparent);
      border-radius: 10px; padding: 10px 12px;
      font-size: 0.78rem; font-weight: 600;
    }

    .empty-state {
      padding: var(--space-xl); text-align: center;
      color: var(--text-muted);
      border: 2px dashed var(--border-color);
      border-radius: 12px;
      background: var(--bg-main);
      font-weight: 600; font-size: 0.84rem;
    }

    .no-permiso {
      padding: var(--space-l);
      border: 2px dashed var(--border-color);
      border-radius: 12px;
      background: var(--bg-main);
      color: var(--text-muted);
      font-weight: 600;
      font-size: 0.84rem;
      text-align: center;
    }
  `]
})
export class SettingsComponent implements OnInit {
  private themeService = inject(ThemeService);
  currentTheme = this.themeService.theme;
  sesion = inject(SesionService);
  private auth = inject(AuthService);
  private usuariosService = inject(UsuarioAdminService);

  usuario = this.sesion.usuario;
  inicial = computed(() => {
    const u = this.usuario();
    const base = u.nombreCompleto || u.username || '?';
    return base.trim().charAt(0).toUpperCase();
  });
  esAdmin = computed(() => this.sesion.esAdmin());
  rolLabel = computed(() => {
    const r = this.usuario().rol?.toUpperCase();
    if (r === 'ADMIN') return 'Administrador';
    if (r === 'VENDEDOR' || r === 'EMPLEADO') return 'Vendedor';
    return r || '—';
  });

  // Gestión usuarios (solo ADMIN)
  usuarios = signal<UsuarioAdmin[]>([]);
  cargandoUsuarios = signal(false);
  errorUsuarios = signal<string | null>(null);
  filtroUsuarios = signal('');
  mostrarFormUsuario = signal(false);
  guardandoUsuario = signal(false);
  errorFormUsuario = signal<string | null>(null);
  formUsuario: CrearUsuarioPayload = { username: '', passwordHash: '', nombreCompleto: '', rol: 'VENDEDOR', estado: 'ACTIVO' };

  usuariosFiltrados = computed(() => {
    const q = this.filtroUsuarios().toLowerCase().trim();
    const lista = this.usuarios();
    if (!q) return lista;
    return lista.filter(u => u.username.toLowerCase().includes(q) || u.nombreCompleto.toLowerCase().includes(q) || u.rol.toLowerCase().includes(q));
  });

  themes: { id: ThemeType; name: string; colors: string[] }[] = [
    { id: 'light-theme', name: 'Claro Clásico', colors: ['#006a60', '#f4fbf7', '#ffffff'] },
    { id: 'dark-theme', name: 'Oscuro Clásico', colors: ['#83d5c5', '#0e1513', '#003730'] },
    { id: 'ligth-blue-theme', name: 'Azul Contraste', colors: ['#002f49', '#f7f9ff', '#ffffff'] },
    { id: 'dark-blue-theme', name: 'Noche Azul', colors: ['#e5f1ff', '#101417', '#93c8f4'] },
    { id: 'ligth-green-theme', name: 'Verde Contraste', colors: ['#0c3407', '#f8fbf1', '#ffffff'] },
    { id: 'dark-green-theme', name: 'Bosque Profundo', colors: ['#cdfdbc', '#11140f', '#a1cf92'] },
    { id: 'sepia-theme', name: 'Sepia Cálido', colors: ['#8b5e3c', '#faf6f0', '#d4a574'] },
    { id: 'high-contrast-theme', name: 'Alto Contraste', colors: ['#00ffff', '#000000', '#008b8b'] },
  ];

  ngOnInit(): void {
    if (this.esAdmin()) this.cargarUsuarios();
  }

  changeTheme(theme: ThemeType) {
    this.themeService.setTheme(theme);
  }

  handleBack() {
    window.history.back();
  }

  cerrarSesion() {
    this.auth.logout();
  }

  cargarUsuarios(): void {
    this.cargandoUsuarios.set(true);
    this.errorUsuarios.set(null);
    this.usuariosService.listar().subscribe({
      next: (data) => { this.usuarios.set(data); this.cargandoUsuarios.set(false); },
      error: (err) => { this.errorUsuarios.set(err?.error?.message || 'No se pudieron cargar los usuarios'); this.cargandoUsuarios.set(false); }
    });
  }

  buscarUsuarios(v: string): void {
    this.filtroUsuarios.set(v);
  }

  toggleFormUsuario(): void {
    this.mostrarFormUsuario.update(v => !v);
    this.errorFormUsuario.set(null);
    if (!this.mostrarFormUsuario()) {
      this.formUsuario = { username: '', passwordHash: '', nombreCompleto: '', rol: 'VENDEDOR', estado: 'ACTIVO' };
    }
  }

  crearUsuario(): void {
    const p = this.formUsuario;
    if (!p.username.trim() || !p.passwordHash.trim() || !p.nombreCompleto.trim() || !p.rol.trim()) {
      this.errorFormUsuario.set('Completa username, contraseña, nombre completo y rol');
      return;
    }
    this.guardandoUsuario.set(true);
    this.errorFormUsuario.set(null);
    this.usuariosService.crear({ ...p, username: p.username.trim(), passwordHash: p.passwordHash.trim(), nombreCompleto: p.nombreCompleto.trim(), rol: p.rol.trim().toUpperCase() }).subscribe({
      next: () => {
        this.guardandoUsuario.set(false);
        this.mostrarFormUsuario.set(false);
        this.formUsuario = { username: '', passwordHash: '', nombreCompleto: '', rol: 'VENDEDOR', estado: 'ACTIVO' };
        this.cargarUsuarios();
      },
      error: (err) => {
        this.guardandoUsuario.set(false);
        this.errorFormUsuario.set(err?.error?.message || 'No se pudo crear el usuario');
      }
    });
  }

  cambiarEstadoUsuario(u: UsuarioAdmin): void {
    const nuevo = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    if (u.id === this.usuario().idUsuario) {
      this.errorUsuarios.set('No puedes cambiar tu propio estado');
      return;
    }
    this.usuariosService.actualizarEstado(u.id, nuevo).subscribe({
      next: () => this.cargarUsuarios(),
      error: (err) => this.errorUsuarios.set(err?.error?.message || 'No se pudo actualizar el estado')
    });
  }

  inicialDe(u: UsuarioAdmin): string {
    const base = u.nombreCompleto || u.username || '?';
    return base.trim().charAt(0).toUpperCase();
  }
}
