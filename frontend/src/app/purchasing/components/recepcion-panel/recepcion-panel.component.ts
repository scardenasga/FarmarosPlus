import { Component, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { PurchasingService } from '../../services/purchasing.service';
import { OrdenCompraResumen } from '../../models/purchasing.model';

@Component({
  selector: 'app-recepcion-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmationDialogComponent],
  templateUrl: './recepcion-panel.component.html',
  styleUrl: './recepcion-panel.component.css'
})
export class RecepcionPanelComponent implements OnChanges {
  private purchasingService = inject(PurchasingService);
  private notificacion = inject(NotificacionService);

  orden = input.required<OrdenCompraResumen>();
  abierto = input<boolean>(false);

  cerrado = output<void>();
  guardado = output<void>();

  estados = ['COMPLETA', 'PARCIAL', 'RECHAZADA'];

  estadoRecepcion = 'COMPLETA';
  totalRecepcion = 0;
  observaciones = '';
  pagarAlRecibir = true;
  montoPago = 0;

  registrando = signal(false);
  mostrarConfirmacion = signal(false);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['abierto'] && this.abierto()) {
      this.estadoRecepcion = 'COMPLETA';
      this.totalRecepcion = this.orden().totalEsperado ?? 0;
      this.montoPago = this.totalRecepcion;
      this.observaciones = '';
      this.pagarAlRecibir = true;
      this.registrando.set(false);
      this.mostrarConfirmacion.set(false);
    }
  }

  solicitarRegistrar(): void {
    if (this.totalRecepcion <= 0) {
      this.notificacion.advertencia('El total de la recepcion debe ser mayor a cero.');
      return;
    }
    if (this.pagarAlRecibir && this.montoPago <= 0) {
      this.notificacion.advertencia('Indica el monto pagado o desactiva el pago al recibir.');
      return;
    }
    this.mostrarConfirmacion.set(true);
  }

  cancelarConfirmacion(): void {
    if (!this.registrando()) this.mostrarConfirmacion.set(false);
  }

  confirmarRegistro(): void {
    if (this.registrando()) return;

    this.registrando.set(true);
    const orden = this.orden();

    this.purchasingService.registrarRecepcion({
      ordenId: orden.idOrden,
      estado: this.estadoRecepcion as 'COMPLETA' | 'PARCIAL' | 'RECHAZADA',
      totalRecepcion: Number(this.totalRecepcion),
      observaciones: this.observaciones || undefined
    }).subscribe({
      next: (respuesta: any) => {
        const idRecepcion = respuesta?.idRecepcion;
        if (this.pagarAlRecibir && idRecepcion && this.montoPago > 0) {
          this.purchasingService.actualizarEstadoPago(idRecepcion, 'PAGADO', Number(this.montoPago)).subscribe({
            next: () => this.finalizar(),
            error: (e) => this.fallo(e)
          });
        } else this.finalizar();
      },
      error: (e) => this.fallo(e)
    });
  }

  private fallo(e: any): void {
    this.notificacion.error(e?.error?.message ?? 'No se pudo registrar la recepcion.');
    this.registrando.set(false);
    this.mostrarConfirmacion.set(false);
  }

  private finalizar(): void {
    this.notificacion.exito(`Recepcion de OC-${this.orden().idOrden} registrada`);
    this.registrando.set(false);
    this.mostrarConfirmacion.set(false);
    this.guardado.emit();
    this.cerrado.emit();
  }
}