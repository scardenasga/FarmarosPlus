import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService, ThemeType } from '../../../services/theme.service';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { SesionService } from '../../../shared/services/sesion.service';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, TopBarComponent],
  templateUrl: './settings.component.html',
  styles: [`
    .settings-page {
      min-height: 100vh;
      background-color: var(--background);
    }

    .settings-content {
      padding: var(--space-l);
      max-width: 960px;
      margin: 0 auto;
    }

    .section {
      margin-bottom: var(--space-xl);
      animation: fadeInUp 0.5s ease-out;
    }

    .section-title {
      font-size: 14px;
      font-weight: 700;
      color: var(--primary);
      text-transform: uppercase;
      letter-spacing: 1.2px;
      margin-bottom: var(--space-l);
      padding-left: var(--space-xs);
    }

    .theme-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: var(--space-l);
    }

    .theme-card {
      background-color: var(--surface-container-high);
      border: 2px solid transparent;
      border-radius: 20px;
      padding: var(--space-m);
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-m);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: var(--shadow-1);
    }

    .theme-card:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-2);
      background-color: var(--surface-container-highest);
    }

    .theme-card.active {
      border-color: var(--primary);
      background-color: var(--primary-container);
      color: var(--on-primary-container);
      transform: scale(1.02);
    }

    .theme-preview {
      width: 100%;
      height: 64px;
      border-radius: 12px;
      display: flex;
      overflow: hidden;
      border: 1px solid var(--outline-variant, rgba(0,0,0,0.1));
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.1);
    }

    .preview-color {
      flex: 1;
    }

    .theme-name {
      font-size: 14px;
      font-weight: 600;
      text-align: center;
    }

    .sesion-card {
      background-color: var(--surface-container-low);
      border: 1px solid var(--border-color);
      border-radius: 20px;
      padding: var(--space-l);
      box-shadow: var(--shadow-1);
      display: flex;
      flex-direction: column;
      gap: var(--space-l);
    }

    .sesion-header {
      display: flex;
      align-items: center;
      gap: var(--space-l);
    }

    .avatar {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: var(--primary);
      color: var(--on-primary);
      display: grid;
      place-items: center;
      font-weight: 800;
      font-size: 20px;
      flex-shrink: 0;
    }

    .sesion-meta {
      flex: 1;
      min-width: 0;
    }

    .sesion-nombre {
      font-size: 16px;
      font-weight: 700;
      color: var(--on-surface);
      margin: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .sesion-username {
      font-size: 13px;
      color: var(--text-muted);
      margin: 2px 0 0 0;
    }

    .badge-rol {
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 0.6px;
      text-transform: uppercase;
      padding: 6px 10px;
      border-radius: 999px;
      flex-shrink: 0;
    }

    .badge-rol.admin {
      background: var(--primary-container);
      color: var(--on-primary-container);
    }

    .badge-rol.vendor {
      background: var(--secondary-container);
      color: var(--on-secondary-container);
    }

    .sesion-details {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: var(--space-m);
      background: var(--surface-container);
      border-radius: 16px;
      padding: var(--space-m);
    }

    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .detail-label {
      font-size: 11px;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .detail-value {
      font-size: 13px;
      font-weight: 600;
      color: var(--on-surface);
    }

    .detail-value.mono {
      font-family: monospace;
      font-size: 12px;
    }

    .btn-logout {
      height: 42px;
      border-radius: 14px;
      background: var(--error);
      color: var(--on-error);
      font-weight: 700;
      font-size: 13px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      align-self: flex-start;
      padding: 0 16px;
    }

    .btn-logout:hover {
      opacity: 0.92;
    }

    .btn-logout svg {
      width: 16px;
      height: 16px;
    }

    @keyframes fadeInUp {
      from { opacity: 0; transform: translateY(20px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class SettingsComponent {
  private themeService = inject(ThemeService);
  currentTheme = this.themeService.theme;
  sesion = inject(SesionService);
  private auth = inject(AuthService);

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

  changeTheme(theme: ThemeType) {
    this.themeService.setTheme(theme);
  }

  handleBack() {
    window.history.back();
  }

  cerrarSesion() {
    this.auth.logout();
  }
}
