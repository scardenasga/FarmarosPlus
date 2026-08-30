import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationService } from '../../services/navigation.service';
import { PermisoService } from '../../services/permiso.service';
import { SesionService } from '../../services/sesion.service';

interface SidebarItem {
  label: string;
  link: string;
  title: string;
  iconPath: string;
  isBottom?: boolean;
  permiso?: string;
}

interface SidebarSubmenu {
  label: string;
  link: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  readonly navService = inject(NavigationService);
  private readonly router = inject(Router);
  private readonly sesion = inject(SesionService);
  private readonly permisoService = inject(PermisoService);

  readonly mainNavItems: SidebarItem[] = [
    {
      label: 'Resumen',
      title: 'Resumen del Negocio',
      link: '/dashboard',
      iconPath: 'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z',
      permiso: 'DASHBOARD_VER'
    },
    {
      label: 'Inventario',
      title: 'Inventario y Medicamentos',
      link: '/inventario',
      iconPath: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z',
      permiso: 'INVENTARIO_VER'
    },
    {
      label: 'Compras',
      title: 'Compras a Proveedores',
      link: '/compras-gestion',
      iconPath: 'M1 3h15v13H1z',
      permiso: 'COMPRAS_VER'
    },
    {
      label: 'Analítica',
      title: 'Analítica Avanzada',
      link: '/analitica',
      iconPath: 'M18 20V10M12 20V4M6 20v-6',
      permiso: 'ANALITICA_VER'
    }
  ];

  // Computed para no recalcular en cada CD y no bloquear main thread
  mainNavFiltrados = computed(() => this.mainNavItems.filter(i => !i.permiso || this.permisoService.tiene(i.permiso)));

  tienePermiso = (clave: string | undefined): boolean => !clave || this.permisoService.tiene(clave);

  ventasSubmenu = computed(() => {
    const base: SidebarSubmenu[] = [];
    if (this.permisoService.tiene('VENTAS_VER')) base.push({ label: 'Historial de ventas', link: '/ventas' });
    if (this.permisoService.tiene('VENTAS_CREAR')) base.push({ label: 'Nueva venta (POS)', link: '/ventas/pos' });
    if (this.permisoService.tiene('VENTAS_REPORTES_VER')) base.push({ label: 'Reporte de ventas', link: '/reportes/ventas' });
    return base;
  });

  ventasVisible = computed(() => this.permisoService.tiene('VENTAS_VER') || this.permisoService.tiene('VENTAS_CREAR'));

  readonly bottomItem: SidebarItem = {
    label: 'Configuración',
    title: 'Configuración del Sistema',
    link: '/configuracion',
    iconPath: 'M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z',
    isBottom: true
  };

  /** Estado del menú desplegable de ventas. */
  ventasSubmenuAbierto = signal<boolean>(false);

  currentRoute = toSignal(
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(event => (event as NavigationEnd).urlAfterRedirects),
      startWith(this.router.url)
    )
  );

  isRouteActive(link: string, exacto: boolean = false): boolean {
    const current = this.currentRoute() || '';
    if (link === '/dashboard') {
      return current === '/dashboard' || current === '/' || current === '';
    }
    // Exacto: solo marca cuando la ruta coincide sin prefijos compartidos
    // (evita que /ventas quede activo al estar en /ventas/pos).
    if (exacto) {
      return current === link;
    }
    return current.startsWith(link);
  }

  /** ¿La ruta actual pertenece al módulo de ventas o sus reportes? */
  esSeccionVentasActiva(): boolean {
    const current = this.currentRoute() || '';
    return current.startsWith('/ventas') || current.startsWith('/reportes/ventas');
  }

  toggleVentasSubmenu(event: Event): void {
    event.stopPropagation();
    this.ventasSubmenuAbierto.update(v => !v);
  }

  toggleSidebar(): void {
    this.navService.toggleSidebar();
  }

  constructor() {
    // Si la ruta inicial pertenece a ventas, el submenu nace abierto.
    if (this.esSeccionVentasActiva()) {
      this.ventasSubmenuAbierto.set(true);
    }
  }
}
