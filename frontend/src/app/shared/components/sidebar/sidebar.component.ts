import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationService } from '../../services/navigation.service';

interface SidebarItem {
  label: string;
  link: string;
  title: string;
  iconPath: string;
  isBottom?: boolean;
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

  readonly mainNavItems: SidebarItem[] = [
    {
      label: 'Resumen',
      title: 'Resumen del Negocio',
      link: '/dashboard',
      iconPath: 'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z'
    },
    {
      label: 'Ventas (POS)',
      title: 'Punto de Venta (POS)',
      link: '/ventas',
      iconPath: 'M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6'
    },
    {
      label: 'Inventario',
      title: 'Inventario y Medicamentos',
      link: '/inventario',
      iconPath: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z'
    },
    {
      label: 'Compras',
      title: 'Compras a Proveedores',
      link: '/compras-gestion',
      iconPath: 'M1 3h15v13H1z'
    },
    {
      label: 'Analítica',
      title: 'Analítica Avanzada',
      link: '/analitica',
      iconPath: 'M18 20V10M12 20V4M6 20v-6'
    }
  ];

  readonly bottomItem: SidebarItem = {
    label: 'Configuración',
    title: 'Configuración del Sistema',
    link: '/configuracion',
    iconPath: 'M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z',
    isBottom: true
  };

  currentRoute = toSignal(
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(event => (event as NavigationEnd).urlAfterRedirects),
      startWith(this.router.url)
    )
  );

  isRouteActive(link: string): boolean {
    const current = this.currentRoute() || '';
    if (link === '/dashboard') {
      return current === '/dashboard' || current === '/' || current === '';
    }
    return current.startsWith(link);
  }

  toggleSidebar(): void {
    this.navService.toggleSidebar();
  }
}
