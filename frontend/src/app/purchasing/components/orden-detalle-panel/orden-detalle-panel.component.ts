import { Component, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificacionService } from '../../../shared/services/notificacion.service';
import { PurchasingService } from '../../services/purchasing.service';
import { OrdenCompraResponse, RecepcionCompraResumen } from '../../models/purchasing.model';

@Component({
  selector: 'app-orden-detalle-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orden-detalle-panel.component.html',
  styleUrl: './orden-detalle-panel.component.css'
})
export class OrdenDetallePanelComponent implements OnChanges {
  private purchasingService = inject(PurchasingService);
  private notificacion = inject(NotificacionService);

  ordenId = input<number | null>(null);
  abierto = input<boolean>(false);

  cerrado = output<void>();
  actualizado = output<void>();

  orden = signal<OrdenCompraResponse | null>(null);
  recepciones = signal<RecepcionCompraResumen[]>([]);
  cargando = signal(true);
  pagando = signal<number | null>(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['abierto'] && this.abierto() && this.ordenId() != null) {
      this.cargar();
    }
  }

  cargar(): void {
    const id = this.ordenId();
    if (id == null) return;
    this.cargando.set(true);

    this.purchasingService.obtenerOrden(id).subscribe({
      next: (orden) => {
        this.orden.set(orden);
        this.purchasingService.listarRecepcionesPorOrden(id).subscribe({
          next: (recs) => { this.recepciones.set(recs ?? []); this.cargando.set(false); },
          error: () => { this.recepciones.set([]); this.cargando.set(false); }
        });
      },
      error: () => {
        this.notificacion.error('No se pudo cargar el detalle de la orden.');
        this.cargando.set(false);
      }
    });
  }

  cerrar(): void {
    this.cerrado.emit();
  }

  textoEstado(e: string): string {
    return ({ PENDIENTE: 'Pendiente', CERRADA: 'Cerrada', NO_RECIBIDA: 'No recibida', CANCELADA: 'Cancelada' } as any)[e] ?? e;
  }

  pagar(recepcion: RecepcionCompraResumen): void {
    if (this.pagando() != null) return;
    this.pagando.set(recepcion.idRecepcion);
    this.purchasingService.actualizarEstadoPago(
      recepcion.idRecepcion,
      'PAGADO',
      recepcion.totalRecepcion
    ).subscribe({
      next: () => {
        this.notificacion.exito('Pago registrado correctamente');
        this.pagando.set(null);
        // Avisa al historial para que refresque el estado de pago en la tabla.
        this.actualizado.emit();
        this.cargar();
      },
      error: (e) => {
        this.notificacion.error(e?.error?.message ?? 'No se pudo registrar el pago.');
        this.pagando.set(null);
      }
    });
  }
}