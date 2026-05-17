import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

interface DashboardCard {
  title: string;
  description: string;
  icon: string;
  link: string;
  tag: string;
  gradient: string;
  iconBg: string;
}

@Component({
  selector: 'app-purchasing-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, TopBarComponent],
  templateUrl: './purchasing-dashboard.component.html',
  styleUrl: './purchasing-dashboard.component.css'
})
export class PurchasingDashboardComponent {
  private router = inject(Router);

  cards: DashboardCard[] = [
    {
      title: 'Proveedores',
      tag: 'Directorio',
      description: 'Central de contactos, catálogos y condiciones comerciales.',
      icon: '🏢',
      link: '/proveedores',
      gradient: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
      iconBg: 'rgba(99, 102, 241, 0.15)'
    },
    {
      title: 'Órdenes',
      tag: 'Operaciones',
      description: 'Generación de pedidos inteligentes y seguimiento de estados.',
      icon: '📋',
      link: '/compras/notificaciones',
      gradient: 'linear-gradient(135deg, #3b82f6 0%, #2dd4bf 100%)',
      iconBg: 'rgba(59, 130, 246, 0.15)'
    },
    {
      title: 'Recepciones',
      tag: 'Logística',
      description: 'Control de ingresos, verificación de lotes y facturación.',
      icon: '📦',
      link: '/entregas/compras',
      gradient: 'linear-gradient(135deg, #f59e0b 0%, #ef4444 100%)',
      iconBg: 'rgba(245, 158, 11, 0.15)'
    },
    {
      title: 'Devoluciones',
      tag: 'Calidad',
      description: 'Gestión de garantías y retorno de productos no conformes.',
      icon: '🔄',
      link: '/entregas',
      gradient: 'linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%)',
      iconBg: 'rgba(236, 72, 153, 0.15)'
    }
  ];

  navigateTo(link: string): void {
    this.router.navigate([link]);
  }
}
