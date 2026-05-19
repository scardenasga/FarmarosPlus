import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { PurchasingService } from '../../services/purchasing.service';
import { DevolucionResponse } from '../../models/purchasing.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';

@Component({
  selector: 'app-return-history',
  standalone: true,
  imports: [CommonModule, RouterModule, TopBarComponent, FabButtonComponent],
  templateUrl: './return-history.component.html',
  styleUrl: './return-history.component.css'
})
export class ReturnHistoryComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private router = inject(Router);

  devoluciones = signal<DevolucionResponse[]>([]);
  cargando = signal<boolean>(true);
  error = signal<string>('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.purchasingService.listarDevoluciones().subscribe({
      next: data => {
        this.devoluciones.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial.');
        this.cargando.set(false);
      }
    });
  }

  nueva(): void {
    this.router.navigate(['/purchasing/register-return']);
  }

  volver(): void {
    this.router.navigate(['/compras-gestion']);
  }

  totalProductos(dev: DevolucionResponse): number {
    return dev.detalles.reduce((sum, d) => sum + d.cantidad, 0);
  }
}
