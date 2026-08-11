import { Component, OnInit, OnDestroy, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PurchasingService } from '../../services/purchasing.service';
import { DevolucionClienteResponse, DevolucionResponse } from '../../models/purchasing.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';

type TipoDevolucion = 'proveedor' | 'cliente';

@Component({
  selector: 'app-return-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, TopBarComponent, ConfirmationDialogComponent],
  templateUrl: './return-detail.component.html',
  styleUrl: './return-detail.component.css'
})
export class ReturnDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private purchasingService = inject(PurchasingService);

  devolucion = signal<DevolucionResponse | DevolucionClienteResponse | null>(null);
  tipoDevolucion = signal<TipoDevolucion>('proveedor');
  cargando = signal<boolean>(true);
  error = signal<string>('');
  eliminando = signal<boolean>(false);
  showDeleteConfirmation = signal<boolean>(false);

  id = 0;

  esProveedor = computed(() => this.tipoDevolucion() === 'proveedor');

  ngOnInit(): void {
    this.navService.hideNav();
    const idParam = this.route.snapshot.paramMap.get('id');
    const tipoParam = this.route.snapshot.queryParamMap.get('tipo');
    if (tipoParam === 'cliente') {
      this.tipoDevolucion.set('cliente');
    }

    this.id = Number(idParam);
    if (!this.id) {
      this.volver();
      return;
    }
    this.cargar();
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    if (this.esProveedor()) {
      this.purchasingService.obtenerDevolucion(this.id).subscribe({
        next: data => {
          this.devolucion.set(data);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar la devolución.');
          this.cargando.set(false);
        }
      });
      return;
    }

    this.purchasingService.obtenerDevolucionCliente(this.id).subscribe({
      next: data => {
        this.devolucion.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la devolución.');
        this.cargando.set(false);
      }
    });
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

  editar(): void {
    this.router.navigate(['/purchasing/return-edit', this.id], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }

  solicitarEliminar(): void {
    this.showDeleteConfirmation.set(true);
  }

  cancelarEliminar(): void {
    this.showDeleteConfirmation.set(false);
  }

  confirmarEliminar(): void {
    const dev = this.devolucion();
    if (!dev || this.eliminando()) return;

    this.eliminando.set(true);
    this.error.set('');

    const obs = this.esProveedor()
      ? this.purchasingService.eliminarDevolucion(this.id, 'admin')
      : this.purchasingService.eliminarDevolucionCliente(this.id, 'admin');

    obs.subscribe({
      next: () => {
        this.router.navigate(['/purchasing/return-history'], {
          queryParams: { tipo: this.tipoDevolucion() }
        });
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'No se pudo eliminar la devolución.');
        this.eliminando.set(false);
        this.showDeleteConfirmation.set(false);
      }
    });
  }

  volver(): void {
    this.router.navigate(['/purchasing/return-history'], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }
}
