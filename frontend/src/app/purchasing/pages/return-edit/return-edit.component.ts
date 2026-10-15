import { Component, OnInit, OnDestroy, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PurchasingService } from '../../services/purchasing.service';
import { DevolucionClienteResponse, DevolucionResponse } from '../../models/purchasing.model';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';

type TipoDevolucion = 'proveedor' | 'cliente';

@Component({
  selector: 'app-return-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmationDialogComponent],
  templateUrl: './return-edit.component.html',
  styleUrl: './return-edit.component.css'
})
export class ReturnEditComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private purchasingService = inject(PurchasingService);

  tipoDevolucion = signal<TipoDevolucion>('proveedor');
  devolucion = signal<DevolucionResponse | DevolucionClienteResponse | null>(null);
  cargando = signal<boolean>(true);
  error = signal<string>('');
  guardando = signal<boolean>(false);
  showConfirmation = signal<boolean>(false);

  // Proveedor
  motivo = signal<string>('');
  observaciones = signal<string>('');

  // Cliente
  nombreCliente = signal<string>('');
  documentoCliente = signal<string>('');

  id = 0;

  esProveedor = computed(() => this.tipoDevolucion() === 'proveedor');

  puedeGuardar = computed(() => {
    return !this.guardando() && this.devolucion() !== null;
  });

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
          this.motivo.set(data.motivo ?? '');
          this.observaciones.set(data.observaciones ?? '');
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
        this.nombreCliente.set(data.nombreCliente ?? '');
        this.documentoCliente.set(data.documentoCliente ?? '');
        this.motivo.set(data.motivo ?? '');
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la devolución.');
        this.cargando.set(false);
      }
    });
  }

  nombrePrincipal(): string {
    const dev = this.devolucion();
    if (!dev) return '';
    return 'nombreProveedor' in dev
      ? dev.nombreProveedor
      : (dev.nombreCliente || 'Cliente sin identificar');
  }

  solicitarGuardar(): void {
    this.error.set('');
    this.showConfirmation.set(true);
  }

  cancelarGuardar(): void {
    this.showConfirmation.set(false);
  }

  confirmarGuardar(): void {
    if (!this.puedeGuardar()) return;
    this.guardando.set(true);
    this.error.set('');

    if (this.esProveedor()) {
      this.purchasingService.actualizarDevolucion(this.id, {
        motivo: this.motivo().trim() || undefined,
        observaciones: this.observaciones().trim() || undefined
      }).subscribe({
        next: () => this.irADetalle(),
        error: err => this.manejarError(err)
      });
      return;
    }

    this.purchasingService.actualizarDevolucionCliente(this.id, {
      nombreCliente: this.nombreCliente().trim() || undefined,
      documentoCliente: this.documentoCliente().trim() || undefined,
      motivo: this.motivo().trim() || undefined
    }).subscribe({
      next: () => this.irADetalle(),
      error: err => this.manejarError(err)
    });
  }

  private irADetalle(): void {
    this.router.navigate(['/purchasing/return-detail', this.id], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }

  private manejarError(err: any): void {
    this.error.set(err?.error?.message ?? 'No se pudo guardar la devolución.');
    this.guardando.set(false);
    this.showConfirmation.set(false);
  }

  volver(): void {
    this.router.navigate(['/purchasing/return-detail', this.id], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }
}
