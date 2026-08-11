import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardResponse } from '../../models/dashboard.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit, OnDestroy {

  private dashboardService = inject(DashboardService);
  private router = inject(Router);
  private refreshSub?: Subscription;

  dashboard = signal<DashboardResponse | null>(null);
  loading = signal(true);
  error = signal('');
  lastUpdated = signal('');
  mostrarAlertas = signal(false);
  mostrarTransacciones = signal(false);
  mostrarStockBajo = signal(false);

  selectedPreset = signal<'7d' | '30d' | '90d' | 'custom'>('30d');

  fechaInicio = '';
  fechaFin = '';

  private currentPeriod: any = null;

  ngOnInit() {
    this.selectPreset('30d');
    this.refreshSub = interval(60000).subscribe(() => {
      this.cargarDashboard();
    });
  }

  ngOnDestroy() {
    this.refreshSub?.unsubscribe();
  }

  selectPreset(preset: '7d' | '30d' | '90d' | 'custom') {
    this.selectedPreset.set(preset);
    if (preset === 'custom') return;

    const fin = new Date();
    const inicio = new Date();
    const days = preset === '7d' ? 7 : preset === '90d' ? 90 : 30;
    inicio.setDate(fin.getDate() - days);

    this.currentPeriod = {
      preset,
      fechaInicio: this.toIso(inicio),
      fechaFin: this.toIso(fin)
    };

    this.cargarDashboard();
  }

  aplicarFechasPersonalizadas() {
    if (!this.fechaInicio || !this.fechaFin) return;

    this.currentPeriod = {
      preset: 'custom',
      fechaInicio: this.fechaInicio,
      fechaFin: this.fechaFin
    };

    this.cargarDashboard();
  }

  cargarDashboard() {
    if (!this.currentPeriod) return;

    this.loading.set(true);
    this.error.set('');

    this.dashboardService
      .obtenerDashboard(this.currentPeriod.fechaInicio, this.currentPeriod.fechaFin)
      .subscribe({
        next: (data) => {
          this.dashboard.set(data);
          this.lastUpdated.set(new Date().toLocaleTimeString('es-CO'));
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el dashboard.');
          this.loading.set(false);
        }
      });
  }

  maxVenta(ventas: any[]): number {
    return Math.max(...ventas.map((v: any) => v.total), 1);
  }

  maxStock(categorias: any[]): number {
    return Math.max(...categorias.map((c: any) => c.stockTotal), 1);
  }

  handleAlerts() {
    this.router.navigate(['/alertas']);
  }

  mostrarDetalleTransacciones() {
    this.mostrarTransacciones.set(true);
  }

  revisarStock() {
    this.router.navigate(['/inventario']);
  }

  mostrarDetalleStockBajo() {
    this.mostrarStockBajo.set(true);
  }

  irAHealth() {
    this.router.navigate(['/health']);
  }

  private toIso(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}