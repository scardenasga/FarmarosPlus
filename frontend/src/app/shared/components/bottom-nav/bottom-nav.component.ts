import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-bottom-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './bottom-nav.component.html',
  styleUrls: ['./bottom-nav.component.css']
})
export class BottomNavComponent {
  
  // Centralizamos las rutas y los iconos
  navItems = [
    { label: 'Inicio', link: '/home', iconPath: 'M3 9.5L12 3l9 6.5V20a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9.5z M9 21V12h6v9' },
    { label: 'Inventario', link: '/inventario', iconPath: 'M2 7h20v14H2z M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2' },
    { label: 'Ventas', link: '/ventas/historial', iconPath: 'M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6' },
    { label: 'Entregas', link: '/compras/notificaciones', iconPath: 'M1 3h15v13H1z M16 8h4l3 5v3h-7V8z' },
    { label: 'Menú', link: '/menu', iconPath: 'M3 6h18M3 12h18M3 18h18' }
  ];
}