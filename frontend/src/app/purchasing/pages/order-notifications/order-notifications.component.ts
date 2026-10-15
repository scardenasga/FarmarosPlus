import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; 
import { PurchasingService } from '../../services/purchasing.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { AlertaDetallada } from '../../models/purchasing.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

@Component({
  selector: 'app-order-notifications',
  standalone: true,
  imports: [CommonModule, SearchBarComponent, TopBarComponent],
  templateUrl: './order-notifications.component.html',
  styleUrl: './order-notifications.component.css'
})
export class OrderNotificationsComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private router = inject(Router);

  alertas = signal<AlertaDetallada[]>([]);
  searchTerm = signal<string>('');

  alertasFiltradas = computed(() => {
    const term = this.searchTerm().toLowerCase();
    return this.alertas().filter(a =>
      a.codigoGenerado.toLowerCase().includes(term) ||
      a.proveedorNombre.toLowerCase().includes(term)
    );
  });

  ngOnInit(): void {
    this.cargarNotificaciones();
  }

  irAPrevisualizar(idOrden: number): void {
    this.router.navigate(['/compras/previsualizar', idOrden]);
  }

  cargarNotificaciones(): void {
    this.purchasingService.getResumenSeguimiento().subscribe({
      next: (data) => {
        // Solo guardamos los que el backend marque como pendientes
        this.alertas.set(data.alertasDetalladas.filter(a => a.estadoActual === 'PENDIENTE'));
      },
      error: (err) => console.error('Error:', err)
    });
  }

  onSearch(termino: string): void {
    this.searchTerm.set(termino);
  }

  handleBack(): void {
    this.router.navigate(['/compras-gestion']);
  }
}
