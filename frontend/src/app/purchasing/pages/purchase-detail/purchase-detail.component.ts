import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PurchasingService } from '../../services/purchasing.service';
import { OrdenCompraResponse, RecepcionCompraResumen } from '../../models/purchasing.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-purchase-detail', standalone: true,
  imports: [CommonModule, FormsModule, TopBarComponent, ConfirmationDialogComponent],
  templateUrl: './purchase-detail.component.html', styleUrl: './purchase-detail.component.css'
})
export class PurchaseDetailComponent implements OnInit {
  private route = inject(ActivatedRoute); private router = inject(Router); private service = inject(PurchasingService);
  orden = signal<OrdenCompraResponse | null>(null); recepciones = signal<RecepcionCompraResumen[]>([]);
  cargando = signal(true); error = signal(''); confirmar = signal(false); recepcionVisible = signal(false); pagoVisible = signal<number | null>(null); guardandoPago = signal(false); guardandoRecepcion = signal(false);
  recepcionEstado = 'COMPLETA'; recepcionTotal = 0; observaciones = ''; montoPago = 0; pagoAlRecibir = true; id = 0;

  ngOnInit(): void { this.id = Number(this.route.snapshot.paramMap.get('id')); this.cargar(); }
  cargar(): void {
    this.cargando.set(true); this.service.obtenerOrden(this.id).subscribe({
      next: o => { this.orden.set(o); this.recepcionTotal = o.totalEsperado; this.cargando.set(false); this.cargarRecepciones(); },
      error: () => { this.error.set('No se pudo cargar el detalle de la orden.'); this.cargando.set(false); }
    });
  }
  cargarRecepciones(): void { this.service.listarRecepcionesPorOrden(this.id).subscribe({ next: data => this.recepciones.set(data) }); }
  volver(): void { this.router.navigate(['/purchasing/purchase-history']); }
  editar(): void { this.router.navigate(['/purchasing/purchase-edit', this.id]); }
  cancelar(): void { this.service.cancelarOrden(this.id).subscribe({ next: () => this.cargar(), error: e => this.error.set(e?.error?.message ?? 'No se pudo cancelar la orden.') }); this.confirmar.set(false); }
  abrirRecepcion(): void {
    this.recepcionEstado = 'COMPLETA'; this.recepcionTotal = this.orden()?.totalEsperado ?? 0;
    this.montoPago = this.recepcionTotal; this.pagoAlRecibir = true; this.recepcionVisible.set(true);
  }
  registrarRecepcion(): void {
    if (this.guardandoRecepcion() || this.recepcionTotal <= 0) return;
    this.guardandoRecepcion.set(true);
    this.service.registrarRecepcion({ ordenId: this.id, estado: this.recepcionEstado as any, totalRecepcion: this.recepcionTotal, observaciones: this.observaciones || undefined }).subscribe({
      next: (respuesta: any) => {
        const idRecepcion = respuesta?.idRecepcion;
        if (this.recepcionEstado === 'COMPLETA' && this.pagoAlRecibir && this.montoPago > 0 && idRecepcion) {
          this.service.actualizarEstadoPago(idRecepcion, 'PAGADO', this.montoPago).subscribe({
            next: () => this.finalizarRecepcion(),
            error: e => { this.guardandoRecepcion.set(false); this.error.set(e?.error?.message ?? 'La recepción se guardó, pero no se pudo registrar el pago.'); }
          });
        } else this.finalizarRecepcion();
      },
      error: e => { this.guardandoRecepcion.set(false); this.error.set(e?.error?.message ?? 'No se pudo registrar la recepción.'); }
    });
  }
  private finalizarRecepcion(): void { this.recepcionVisible.set(false); this.guardandoRecepcion.set(false); this.observaciones = ''; this.cargar(); }
  abrirPago(recepcion: RecepcionCompraResumen): void { this.montoPago = recepcion.totalRecepcion; this.pagoVisible.set(recepcion.idRecepcion); }
  guardarPago(recepcion: RecepcionCompraResumen): void { if (this.montoPago <= 0 || this.guardandoPago()) return; this.guardandoPago.set(true); this.service.actualizarEstadoPago(recepcion.idRecepcion, 'PAGADO', this.montoPago).subscribe({ next: () => { this.pagoVisible.set(null); this.guardandoPago.set(false); this.cargarRecepciones(); }, error: e => { this.error.set(e?.error?.message ?? 'No se pudo actualizar el estado de pago.'); this.guardandoPago.set(false); } }); }
}
