import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { PurchasingService } from '../../services/purchasing.service';
import { DevolucionClienteResponse, DevolucionResponse } from '../../models/purchasing.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';

type TipoDevolucion = 'proveedor' | 'cliente';

@Component({
  selector: 'app-return-history',
  standalone: true,
  imports: [CommonModule, RouterModule, TopBarComponent, FabButtonComponent, ConfirmationDialogComponent],
  templateUrl: './return-history.component.html',
  styleUrl: './return-history.component.css'
})
export class ReturnHistoryComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private router = inject(Router);

  tipoDevolucion = signal<TipoDevolucion>('proveedor');
  devoluciones = signal<(DevolucionResponse | DevolucionClienteResponse)[]>([]);
  cargando = signal<boolean>(true);
  error = signal<string>('');

  devolucionAEliminar = signal<DevolucionResponse | DevolucionClienteResponse | null>(null);
  eliminando = signal<boolean>(false);

  esProveedor = computed(() => this.tipoDevolucion() === 'proveedor');
  esCliente = computed(() => this.tipoDevolucion() === 'cliente');

  ngOnInit(): void {
    this.cargar();
  }

  onTipoChange(tipo: TipoDevolucion): void {
    if (this.tipoDevolucion() === tipo) {
      return;
    }

    this.tipoDevolucion.set(tipo);
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    if (this.esProveedor()) {
      this.purchasingService.listarDevoluciones().subscribe({
        next: (data: DevolucionResponse[]) => {
          this.devoluciones.set(data);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el historial.');
          this.cargando.set(false);
        }
      });
      return;
    }

    this.purchasingService.listarDevolucionesClientes().subscribe({
      next: (data: DevolucionClienteResponse[]) => {
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
    this.router.navigate(['/purchasing/register-return'], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }

  verDetalle(dev: DevolucionResponse | DevolucionClienteResponse): void {
    this.router.navigate(['/purchasing/return-detail', dev.id], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }

  editar(dev: DevolucionResponse | DevolucionClienteResponse): void {
    this.router.navigate(['/purchasing/return-edit', dev.id], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }

  solicitarEliminar(dev: DevolucionResponse | DevolucionClienteResponse): void {
    this.devolucionAEliminar.set(dev);
  }

  cancelarEliminar(): void {
    if (this.eliminando()) return;
    this.devolucionAEliminar.set(null);
  }

  confirmarEliminar(): void {
    const dev = this.devolucionAEliminar();
    if (!dev || this.eliminando()) return;

    this.eliminando.set(true);
    this.error.set('');

    const obs = this.esProveedor()
      ? this.purchasingService.eliminarDevolucion(dev.id, 'admin')
      : this.purchasingService.eliminarDevolucionCliente(dev.id, 'admin');

    obs.subscribe({
      next: () => {
        this.devoluciones.update(prev => prev.filter(d => d.id !== dev.id));
        this.devolucionAEliminar.set(null);
        this.eliminando.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'No se pudo eliminar la devolución.');
        this.devolucionAEliminar.set(null);
        this.eliminando.set(false);
      }
    });
  }

  volver(): void {
    this.router.navigate(['/compras-gestion']);
  }

  totalProductos(dev: DevolucionResponse | DevolucionClienteResponse): number {
    return dev.detalles.reduce((sum, d) => sum + d.cantidad, 0);
  }

  nombrePrincipal(dev: DevolucionResponse | DevolucionClienteResponse): string {
    return 'nombreProveedor' in dev
      ? dev.nombreProveedor
      : (dev.nombreCliente || 'Cliente sin identificar');
  }

  detalleSecundario(dev: DevolucionResponse | DevolucionClienteResponse): string | null {
    if ('idVenta' in dev) {
      return `Venta #${dev.idVenta} · ${dev.documentoCliente || 'Sin documento'}`;
    }
    return null;
  }
}
