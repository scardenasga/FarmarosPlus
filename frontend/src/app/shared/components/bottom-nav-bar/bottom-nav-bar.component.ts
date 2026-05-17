import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

interface NavItem {
  label: string;
  link: string;
  icon: string;
}

@Component({
  selector: 'app-bottom-nav-bar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: 'bottom-nav-bar.component.html',
  styleUrl: './bottom-nav-bar.component.css',
})
export class BottomNavBarComponent {
  private router = inject(Router);

  navItems: NavItem[] = [
    {
      label: 'Home',
      link: '/health',
      icon: 'M10 20V14H14V20H19V12H22L12 3L2 12H5V20H10Z'
    },
    {
      label: 'Inventario',
      link: '/inventario',
      icon: 'M20 13H4v-2h16v2zM20 17H4v-2h16v2zM20 9H4V7h16v2z'
    },
    {
      label: 'Ventas',
      link: '/ventas',
      icon: 'M11.5 2C6.81 2 3 5.81 3 10.5S6.81 19 11.5 19h.5v3c0 .55.45 1 1 1h1l3.06-5.52c.84-.13 1.63-.42 2.35-.84l4.3 4.3c.39.39 1.02.39 1.41 0 .39-.39.39-1.02 0-1.41l-4.29-4.3c.42-.72.71-1.51.84-2.35L24 13v-1c0-.55-.45-1-1-1h-3v-.5C20 5.81 16.19 2 11.5 2zm0 14.5c-3.59 0-6.5-2.91-6.5-6.5S7.91 3.5 11.5 3.5 18 6.41 18 10s-2.91 6.5-6.5 6.5z'
    },
    {
      label: 'Compras',
      link: '/compras-gestion',
      icon: 'M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z'
    },
    {
      label: 'Config',
      link: '/configuracion',
      icon: 'M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.81,11.69,4.81,12c0,0.31,0.02,0.65,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.5c-1.93,0-3.5-1.57-3.5-3.5 s1.57-3.5,3.5-3.5s3.5,1.57,3.5,3.5S13.93,15.5,12,15.5z'
    }
  ];

  currentRoute = toSignal(
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(event => (event as NavigationEnd).urlAfterRedirects),
      startWith(this.router.url)
    )
  );

  activeIndex = computed(() => {
    const url = this.currentRoute();
    const index = this.navItems.findIndex(item => url?.startsWith(item.link));
    return index >= 0 ? index : 0;
  });
}
