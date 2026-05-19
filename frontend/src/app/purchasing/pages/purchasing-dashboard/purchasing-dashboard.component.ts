import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

interface DashboardCard {
  title: string;
  description: string;
  icon: string;
  link: string;
  tag: string;
  color: string;
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

  cards = signal<DashboardCard[]>([
    {
      title: 'Proveedores',
      tag: 'Directorio',
      description: 'Gestión de contactos, catálogos y condiciones de pago.',
      icon: '🏢',
      link: '/proveedores',
      color: 'var(--primary)',
      iconBg: 'color-mix(in srgb, var(--primary), transparent 85%)'
    },
    {
      title: 'Gestión de Compras',
      tag: 'Abastecimiento',
      description: 'Historial de recepciones y registro de nueva mercancía.',
      icon: '📦',
      link: '/purchasing/purchase-history',
      color: 'var(--secondary)',
      iconBg: 'color-mix(in srgb, var(--secondary), transparent 85%)'
    },
    {
      title: 'Gestión de Devoluciones',
      tag: 'Calidad',
      description: 'Historial y registro de retorno de productos.',
      icon: '🔄',
      link: '/purchasing/return-history',
      color: 'var(--error)',
      iconBg: 'color-mix(in srgb, var(--error), transparent 85%)'
    }
  ]);

  navigateTo(link: string): void {
    this.router.navigate([link]);
  }

  handleBack(): void {
    this.router.navigate(['/']);
  }

  handleAlerts(): void {
    this.router.navigate(['/purchasing/order-notifications']);
  }
}
