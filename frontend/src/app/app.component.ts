import { Component, inject, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { NotificacionToastComponent } from './shared/components/notificacion-toast/notificacion-toast.component';
import { NavigationService } from './shared/services/navigation.service';
import { SesionService } from './shared/services/sesion.service';
import { PermisoService } from './shared/services/permiso.service';
import { ThemeService } from './services/theme.service';
import { AlertaService } from './services/alerta.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SidebarComponent, NotificacionToastComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'frontend';
  navService = inject(NavigationService);
  sesion = inject(SesionService);
  permisoService = inject(PermisoService);
  themeService = inject(ThemeService);
  private alertaService = inject(AlertaService);
  private router = inject(Router);

  ngOnInit(): void {
    this.alertaService.generarYActualizar();
    // Restaurar permisos desde localStorage (sin bloquear con HTTP)
    try {
      const raw = localStorage.getItem('farmaros.permisos');
      if (raw) this.permisoService.setDesdeLogin(JSON.parse(raw));
    } catch {}
    // Catálogo y permisos por usuario se cargan lazy solo en Configuración
    // para no retrasar el arranque inicial
    // Ocultar sidebar en /login, mostrar en resto
    const syncNav = (url: string) => {
      if (url.startsWith('/login')) this.navService.hideNav();
      else this.navService.showNav();
    };
    syncNav(this.router.url);
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(e => {
      syncNav((e as NavigationEnd).urlAfterRedirects);
    });
  }
}
