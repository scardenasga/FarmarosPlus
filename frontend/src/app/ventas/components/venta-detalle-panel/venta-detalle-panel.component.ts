import { Component, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SesionService } from '../../../shared/services/sesion.service';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { EstadoVentaComponent } from '../../../shared/components/estado-venta/estado-venta.component';
import { VentaService } from '../../services/venta.service';
import {
  metodoPagoPrincipal,
  nombreVendedor,
  Venta
} from '../../models/venta.model';

@Component({
  selector: 'app-venta-detalle-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, EstadoVentaComponent],
  templateUrl: './venta-detalle-panel.component.html',
  styleUrl: './venta-detalle-panel.component.css'
})
export class VentaDetallePanelComponent implements OnChanges {
  private ventaService = inject(VentaService);
  private sesion = inject(SesionService);
  private notificacion = inject(NotificacionService);

  /** Id de la venta a mostrar; null oculta el contenido. */
  ventaId = input<number | null>(null);
  visible = input<boolean>(false);

  closed = output<void>();
  /** Emite la venta actualizada tras una anulación exitosa. */
  anulada = output<Venta>();

  venta = signal<Venta | null>(null);
  cargando = signal<boolean>(false);
  error = signal<string>('');

  descargandoFactura = signal<boolean>(false);
  mostrandoAnulacion = signal<boolean>(false);
  motivoAnulacion = '';
  anulando = signal<boolean>(false);
  errorAnulacion = signal<string>('');

  ngOnChanges(changes: SimpleChanges): void {
    const cambioVisible = changes['visible'];
    const cambioId = changes['ventaId'];

    if (!this.visible()) {
      this.limpiar();
      return;
    }

    const id = this.ventaId();
    if (id != null && (cambioVisible || cambioId)) {
      this.cargarVenta(id);
    }
  }

  cargarVenta(id: number) {
    this.cargando.set(true);
    this.error.set('');
    this.venta.set(null);
    this.resetAcciones();

    this.ventaService.obtenerVenta(id).subscribe({
      next: (venta) => {
        this.venta.set(venta);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el detalle de la venta.');
        this.cargando.set(false);
      }
    });
  }

  cerrar() {
    this.closed.emit();
  }

  estaCompletada(estado: string): boolean {
    return String(estado ?? '').toUpperCase() === 'COMPLETADA';
  }

  puedeAnular(venta: Venta): boolean {
    return this.estaCompletada(venta.estado) && this.sesion.esAdminORegente();
  }

  vendedor(venta: Venta): string {
    return nombreVendedor(venta);
  }

  metodoPago(venta: Venta): string {
    return metodoPagoPrincipal(venta);
  }

  detallesDe(venta: Venta) {
    return venta.detalles ?? [];
  }

  descargarFactura() {
    const id = this.venta()?.id;
    if (!id || this.descargandoFactura()) return;

    this.descargandoFactura.set(true);
    this.ventaService.descargarFactura(id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `factura-${id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.descargandoFactura.set(false);
      },
      error: () => {
        this.errorAnulacion.set('No se pudo generar la factura.');
        this.notificacion.error('No se pudo generar la factura PDF.');
        this.descargandoFactura.set(false);
      }
    });
  }

  toggleAnulacion() {
    this.mostrandoAnulacion.update(v => !v);
    this.errorAnulacion.set('');
  }

  confirmarAnulacion() {
    const venta = this.venta();
    if (!venta) return;

    if (!this.motivoAnulacion.trim()) {
      this.errorAnulacion.set('El motivo de anulación es obligatorio');
      return;
    }

    this.anulando.set(true);
    this.errorAnulacion.set('');

    const request = {
      confirmacion: true,
      usuarioId: this.sesion.idUsuario(),
      usuarioResponsable: this.sesion.username(),
      motivoAnulacion: this.motivoAnulacion.trim()
    };

    this.ventaService.anularVenta(venta.id, request).subscribe({
      next: (actualizada) => {
        this.venta.set(actualizada);
        this.motivoAnulacion = '';
        this.mostrandoAnulacion.set(false);
        this.anulando.set(false);
        this.notificacion.exito(`Venta #${actualizada.id} anulada correctamente`);
        this.anulada.emit(actualizada);
      },
      error: (err) => {
        const mensaje = err.error?.message || 'Error al anular la venta';
        this.errorAnulacion.set(mensaje);
        this.notificacion.error(mensaje);
        this.anulando.set(false);
      }
    });
  }

  cancelarAnulacion() {
    this.mostrandoAnulacion.set(false);
    this.motivoAnulacion = '';
    this.errorAnulacion.set('');
  }

  private resetAcciones() {
    this.mostrandoAnulacion.set(false);
    this.motivoAnulacion = '';
    this.errorAnulacion.set('');
    this.descargandoFactura.set(false);
  }

  private limpiar() {
    this.venta.set(null);
    this.cargando.set(false);
    this.error.set('');
    this.resetAcciones();
  }
}
