import { Component, inject, computed, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ThemeService, ThemeType } from '../../../services/theme.service';
import { SesionService } from '../../../shared/services/sesion.service';
import { AuthService } from '../../../auth/services/auth.service';
import { UsuarioAdminService, UsuarioAdmin, CrearUsuarioPayload } from '../../services/usuario-admin.service';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { PermisoService } from '../../../shared/services/permiso.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmationDialogComponent],
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

    /* Sesión compacta - sin duplicados */
    .sesion-main {
      display: flex;
      align-items: center;
      gap: var(--space-m);
      flex-wrap: nowrap;
    }

    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: var(--accent-green-dark, var(--primary-container));
      color: var(--on-primary-container);
      display: grid;
      place-items: center;
      font-weight: 800;
      font-size: 15px;
      flex-shrink: 0;
      box-shadow: var(--shadow-1);
    }

    .sesion-meta { flex: 1; min-width: 0; }

    .sesion-nombre {
      font-size: 0.84rem;
      font-weight: 800;
      color: var(--text-dark, var(--on-background));
      margin: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .sesion-username {
      font-size: 0.68rem;
      color: var(--text-muted, var(--outline));
      margin: 0;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .badge-rol {
      font-size: 0.60rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      font-weight: 800;
      padding: 3px 8px;
      border-radius: 9999px;
      flex-shrink: 0;
    }

    .badge-rol.admin { background: var(--primary-container); color: var(--on-primary-container); }
    .badge-rol.vendor { background: var(--secondary-container); color: var(--on-secondary-container); }

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

    /* Temas - compacto y escalable */
    .theme-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
      gap: var(--space-s);
    }

    .theme-card {
      background-color: var(--bg-main, var(--surface-container-low));
      border: 1px solid var(--border-color, var(--outline-variant));
      border-radius: 10px;
      padding: var(--space-s);
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-s);
      transition: border-color 0.2s, box-shadow 0.2s, transform 0.1s;
    }

    .theme-card:hover {
      border-color: var(--primary);
      box-shadow: var(--shadow-card);
      transform: translateY(-1px);
    }

    .theme-card.active {
      border-color: var(--primary);
      background-color: var(--primary-container);
      color: var(--on-primary-container);
      box-shadow: var(--shadow-card);
    }

    .theme-preview {
      width: 100%;
      height: 36px;
      border-radius: 8px;
      display: flex;
      overflow: hidden;
      border: 1px solid var(--border-color, var(--outline-variant));
      box-shadow: inset 0 1px 2px rgba(0,0,0,0.08);
    }

    .preview-color { flex: 1; }

    .theme-name {
      font-size: 0.66rem;
      font-weight: 800;
      text-align: center;
      line-height: 1.2;
    }

    .theme-card.active .theme-name { color: var(--on-primary-container); }

    .theme-toggle {
      margin-top: var(--space-m);
      display: flex;
      justify-content: center;
    }

    .btn-link {
      font-size: 0.74rem;
      font-weight: 700;
      color: var(--primary);
      text-decoration: underline;
      padding: 4px 8px;
    }

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
      max-height: 180px;
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
      padding: 7px var(--space-s);
      font-size: 0.60rem;
      text-transform: uppercase;
      letter-spacing: 0.7px;
      font-weight: 800;
      color: var(--text-muted);
      border-bottom: 1px solid var(--border-color);
      background: var(--card-bg);
      text-align: left;
      white-space: nowrap;
    }

    .usuarios-tabla tbody td {
      padding: 7px var(--space-s);
      border-bottom: 1px solid var(--border-color);
      font-size: 0.76rem;
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
      margin-top: var(--space-s);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      background: var(--bg-main);
      padding: var(--space-m);
      display: flex;
      flex-direction: column;
      gap: var(--space-s);
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: var(--space-s);
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

    /* Panel deslizante permisos (igual que producto-formulario-panel) */
    .panel-backdrop {
      position: fixed; inset: 0;
      background: rgba(0,0,0,0.4); backdrop-filter: blur(2px);
      z-index: 1002;
    }
    .panel-formulario {
      position: fixed; top: 0; right: 0; height: 100vh; width: 100%; max-width: 480px;
      background: var(--bg-main, var(--background));
      box-shadow: -8px 0 30px rgba(0,0,0,0.25);
      z-index: 1003; display: flex; flex-direction: column;
      animation: panel-entrada 0.25s ease-out;
    }
    @keyframes panel-entrada { from { transform: translateX(100%); } to { transform: translateX(0); } }
    .panel-header {
      flex-shrink: 0; display: flex; align-items: flex-start; justify-content: space-between;
      gap: var(--space-m); padding: var(--space-m) var(--space-l);
      border-bottom: 1px solid var(--border-color);
    }
    .panel-header-texto h2 { margin: 2px 0; font-size: 1rem; font-weight: 800; color: var(--text-dark); }
    .panel-ref { font-size: 0.60rem; text-transform: uppercase; letter-spacing: 1px; font-weight: 800; color: var(--text-muted); }
    .btn-cerrar-panel {
      width: 30px; height: 30px; border-radius: 8px; display: grid; place-items: center;
      color: var(--text-muted); background: var(--surface-container); flex-shrink: 0;
    }
    .btn-cerrar-panel:hover { background: var(--danger-bg); color: var(--danger-text); }
    .btn-cerrar-panel svg { width: 15px; height: 15px; }
    .panel-cuerpo { flex: 1; overflow-y: auto; padding: var(--space-l); display: flex; flex-direction: column; gap: var(--space-m); }
    .panel-footer {
      flex-shrink: 0; padding: var(--space-m) var(--space-l);
      border-top: 1px solid var(--border-color); display: flex; gap: var(--space-m);
      background: var(--card-bg);
    }
    .btn-panel { flex: 1; padding: var(--space-s) var(--space-m); border-radius: 8px; font-weight: 700; font-size: 0.82rem; }
    .btn-panel.secundario { background: transparent; border: 1px solid var(--border-color); color: var(--text-dark); }
    .btn-panel.primario { background: var(--accent-green-dark, var(--primary-container)); color: var(--on-primary-container); border: none; }
    .btn-panel.primario:disabled { opacity: 0.6; cursor: wait; }
  `]
})
export class SettingsComponent implements OnInit {
  private themeService = inject(ThemeService);
  currentTheme = this.themeService.theme;
  sesion = inject(SesionService);
  private auth = inject(AuthService);
  private usuariosService = inject(UsuarioAdminService);
  private notificacion = inject(NotificacionService);

  usuario = this.sesion.usuario;
  inicial = computed(() => {
    const u = this.usuario();
    const base = u.nombreCompleto || u.username || '?';
    return base.trim().charAt(0).toUpperCase();
  });
  esAdmin = computed(() => this.sesion.esAdmin());
  rolReal = computed(() => this.sesion.usuario().rol?.toUpperCase() || '');
  previewActivo = this.sesion.previewVendedor;
  puedePreview = computed(() => this.sesion.puedePreview());
  rolLabel = computed(() => {
    const r = this.sesion.rolEfectivo();
    if (r === 'ADMIN') return 'Administrador';
    if (r === 'VENDEDOR' || r === 'EMPLEADO') return 'Vendedor';
    return r || '—';
  });

  togglePreview(): void {
    if (this.sesion.esPreviewActivo()) {
      this.permisoService.salirPreview();
      this.notificacion.exito('Vista ADMIN restaurada');
    } else {
      this.permisoService.iniciarPreviewVendedorBase();
      this.notificacion.info('Vista previa VENDEDOR activa — navega como vendedor');
    }
  }

  previsualizarComo(usuario: UsuarioAdmin): void {
    if (!this.esAdmin()) return;
    this.cargandoPermisosUsuario.set(true);
    this.permisoService.obtenerPermisosUsuario(usuario.id).subscribe({
      next: perms => {
        this.cargandoPermisosUsuario.set(false);
        this.permisoService.iniciarPreview(perms ?? []);
        this.notificacion.info(`Viendo como ${usuario.username} (${usuario.rol})`);
      },
      error: () => {
        this.cargandoPermisosUsuario.set(false);
        this.notificacion.error('No se pudieron cargar permisos de ese usuario');
      }
    });
  }

  // Gestión usuarios (solo ADMIN)
  usuarios = signal<UsuarioAdmin[]>([]);
  cargandoUsuarios = signal(false);
  filtroUsuarios = signal('');
  mostrarFormUsuario = signal(false);
  guardandoUsuario = signal(false);
  mostrarTodosTemas = signal(false);
  formUsuario: CrearUsuarioPayload = { username: '', passwordHash: '', nombreCompleto: '', rol: 'VENDEDOR', estado: 'ACTIVO' };

  // Permisos auto-generados (panel deslizante como en Inventario)
  permisoService = inject(PermisoService);
  usuarioPermisoId = signal<number | null>(null);
  permisosUsuario = signal<Set<string>>(new Set());
  cargandoPermisosUsuario = signal(false);
  guardandoPermisos = signal(false);
  panelPermisosUsuario = signal<UsuarioAdmin | null>(null);
  panelPermisosAbierto = computed(() => this.panelPermisosUsuario() !== null);

  // Diálogos de confirmación (reemplaza confirm() nativo)
  usuarioPendienteEstado = signal<{ usuario: UsuarioAdmin; nuevoEstado: string } | null>(null);
  usuarioPendienteEliminar = signal<UsuarioAdmin | null>(null);
  confirmarCerrarSesion = signal(false);

  usuariosFiltrados = computed(() => {
    const q = this.filtroUsuarios().toLowerCase().trim();
    const lista = this.usuarios();
    if (!q) return lista;
    return lista.filter(u => u.username.toLowerCase().includes(q) || u.nombreCompleto.toLowerCase().includes(q) || u.rol.toLowerCase().includes(q));
  });

  themesVisibles = computed(() => this.mostrarTodosTemas() ? this.themes : this.themes.slice(0, 4));

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
    if (this.esAdmin()) {
      this.cargarUsuarios();
      this.permisoService.cargarCatalogo();
    }
  }

  changeTheme(theme: ThemeType) {
    this.themeService.setTheme(theme);
  }

  handleBack() {
    window.history.back();
  }

  solicitarCerrarSesion(): void {
    this.confirmarCerrarSesion.set(true);
  }

  cancelarCerrarSesion(): void {
    this.confirmarCerrarSesion.set(false);
  }

  confirmarCerrarSesionAccion(): void {
    this.confirmarCerrarSesion.set(false);
    this.auth.logout();
    this.notificacion.info('Sesión cerrada');
  }

  cerrarSesion() {
    this.solicitarCerrarSesion();
  }

  cargarUsuarios(): void {
    this.cargandoUsuarios.set(true);
    this.usuariosService.listar().subscribe({
      next: (data) => { this.usuarios.set(data); this.cargandoUsuarios.set(false); },
      error: (err) => {
        this.notificacion.error(err?.error?.message || 'No se pudieron cargar los usuarios');
        this.cargandoUsuarios.set(false);
      }
    });
  }

  buscarUsuarios(v: string): void {
    this.filtroUsuarios.set(v);
  }

  toggleFormUsuario(): void {
    this.mostrarFormUsuario.update(v => !v);
    if (!this.mostrarFormUsuario()) {
      this.formUsuario = { username: '', passwordHash: '', nombreCompleto: '', rol: 'VENDEDOR', estado: 'ACTIVO' };
    }
  }

  crearUsuario(): void {
    const p = this.formUsuario;
    if (!p.username.trim() || !p.passwordHash.trim() || !p.nombreCompleto.trim() || !p.rol.trim()) {
      this.notificacion.error('Completa username, contraseña, nombre completo y rol');
      return;
    }
    this.guardandoUsuario.set(true);
    this.usuariosService.crear({ ...p, username: p.username.trim(), passwordHash: p.passwordHash.trim(), nombreCompleto: p.nombreCompleto.trim(), rol: p.rol.trim().toUpperCase() }).subscribe({
      next: () => {
        this.guardandoUsuario.set(false);
        this.mostrarFormUsuario.set(false);
        this.formUsuario = { username: '', passwordHash: '', nombreCompleto: '', rol: 'VENDEDOR', estado: 'ACTIVO' };
        this.notificacion.exito(`Usuario "${p.username.trim()}" creado correctamente`);
        this.cargarUsuarios();
      },
      error: (err) => {
        this.guardandoUsuario.set(false);
        this.notificacion.error(err?.error?.message || 'No se pudo crear el usuario');
      }
    });
  }

  // Flujo con ConfirmationDialog (igual que inventario)
  solicitarCambioEstado(u: UsuarioAdmin): void {
    const nuevo = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    if (u.id === this.usuario().idUsuario) {
      this.notificacion.error('No puedes cambiar tu propio estado');
      return;
    }
    this.usuarioPendienteEstado.set({ usuario: u, nuevoEstado: nuevo });
  }

  cancelarCambioEstado(): void {
    this.usuarioPendienteEstado.set(null);
  }

  confirmarCambioEstado(): void {
    const pendiente = this.usuarioPendienteEstado();
    if (!pendiente) return;
    const { usuario, nuevoEstado } = pendiente;
    this.usuarioPendienteEstado.set(null);
    this.usuariosService.actualizarEstado(usuario.id, nuevoEstado).subscribe({
      next: () => {
        this.notificacion.exito(`Usuario "${usuario.username}" ahora ${nuevoEstado}`);
        this.cargarUsuarios();
      },
      error: (err) => this.notificacion.error(err?.error?.message || 'No se pudo actualizar el estado')
    });
  }

  // Mantener alias para template existente
  cambiarEstadoUsuario(u: UsuarioAdmin): void {
    this.solicitarCambioEstado(u);
  }

  solicitarEliminarUsuario(u: UsuarioAdmin): void {
    if (u.id === this.usuario().idUsuario) {
      this.notificacion.error('No puedes eliminar tu propia cuenta');
      return;
    }
    if (u.username.toLowerCase() === 'sistema') {
      this.notificacion.error('No se puede eliminar el usuario SISTEMA');
      return;
    }
    this.usuarioPendienteEliminar.set(u);
  }

  cancelarEliminarUsuario(): void {
    this.usuarioPendienteEliminar.set(null);
  }

  confirmarEliminarUsuario(): void {
    const u = this.usuarioPendienteEliminar();
    if (!u) return;
    this.usuarioPendienteEliminar.set(null);
    this.usuariosService.eliminar(u.id).subscribe({
      next: () => {
        this.notificacion.exito(`Usuario "${u.username}" eliminado`);
        this.cargarUsuarios();
      },
      error: (err) => this.notificacion.error(err?.error?.message || 'No se pudo eliminar. Si tiene historial, inactívalo en su lugar.')
    });
  }

  eliminarUsuario(u: UsuarioAdmin): void {
    this.solicitarEliminarUsuario(u);
  }

  inicialDe(u: UsuarioAdmin): string {
    const base = u.nombreCompleto || u.username || '?';
    return base.trim().charAt(0).toUpperCase();
  }

  abrirPanelPermisos(u: UsuarioAdmin): void {
    if (!this.esAdmin()) { this.notificacion.error('Solo ADMIN puede gestionar permisos'); return; }
    this.panelPermisosUsuario.set(u);
    this.usuarioPermisoId.set(u.id);
    this.cargandoPermisosUsuario.set(true);
    this.permisoService.obtenerPermisosUsuario(u.id).subscribe({
      next: d => { this.permisosUsuario.set(new Set(d ?? [])); this.cargandoPermisosUsuario.set(false); },
      error: () => { this.notificacion.error('No se pudieron cargar permisos'); this.cargandoPermisosUsuario.set(false); }
    });
  }

  cerrarPanelPermisos(): void {
    this.panelPermisosUsuario.set(null);
  }

  // --- Permisos automáticos por usuario (compatibilidad) ---
  seleccionarUsuarioPermisos(id: number | null): void {
    this.usuarioPermisoId.set(id);
    if (id == null) { this.permisosUsuario.set(new Set()); return; }
    this.cargandoPermisosUsuario.set(true);
    this.permisoService.obtenerPermisosUsuario(id).subscribe({
      next: d => { this.permisosUsuario.set(new Set(d ?? [])); this.cargandoPermisosUsuario.set(false); },
      error: () => { this.notificacion.error('No se pudieron cargar permisos'); this.cargandoPermisosUsuario.set(false); }
    });
  }

  tienePermisoUsuario(clave: string): boolean {
    return this.permisosUsuario().has(clave);
  }

  togglePermisoUsuario(clave: string): void {
    const next = new Set(this.permisosUsuario());
    if (next.has(clave)) next.delete(clave);
    else next.add(clave);
    this.permisosUsuario.set(next);
  }

  guardarPermisosUsuario(): void {
    const id = this.usuarioPermisoId() ?? this.panelPermisosUsuario()?.id ?? null;
    if (id == null) { this.notificacion.error('Selecciona un usuario'); return; }
    const map: Record<string, boolean> = {};
    for (const p of this.permisoService.catalogo()) {
      map[p.clave] = this.permisosUsuario().has(p.clave);
    }
    this.guardandoPermisos.set(true);
    this.permisoService.guardarPermisosUsuario(id, map).subscribe({
      next: d => {
        this.permisosUsuario.set(new Set(d ?? []));
        this.guardandoPermisos.set(false);
        this.notificacion.exito('Permisos guardados');
        if (id === this.usuario().idUsuario) {
          this.permisoService.setDesdeLogin(d ?? []);
          try { localStorage.setItem('farmaros.permisos', JSON.stringify(d ?? [])); } catch {}
        }
        this.cerrarPanelPermisos();
      },
      error: err => {
        this.guardandoPermisos.set(false);
        this.notificacion.error(err?.error?.message || 'No se pudieron guardar permisos');
      }
    });
  }
}
