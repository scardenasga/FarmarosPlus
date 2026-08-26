import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NotificacionService } from '../../../shared/services/notificacion.service';
import { PurchasingService } from '../../services/purchasing.service';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { Supplier } from '../../../supplier/models/supplier.model';
import { OrdenCompraResponse } from '../../models/purchasing.model';

@Component({
  selector: 'app-purchase-edit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './purchase-edit.component.html',
  styleUrl: './purchase-edit.component.css'
})
export class PurchaseEditComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private purchasingService = inject(PurchasingService);
  private supplierService = inject(SupplierService);
  private notificacion = inject(NotificacionService);

  orden = signal<OrdenCompraResponse | null>(null);
  proveedores = signal<Supplier[]>([]);
  cargando = signal<boolean>(true);
  guardando = signal<boolean>(false);

  proveedorId: number | null = null;
  fechaEsperada = '';
  observaciones = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.supplierService.listActive().subscribe({
      next: (p) => this.proveedores.set(p ?? []),
      error: () => this.notificacion.error('No se pudieron cargar los proveedores.')
    });
    if (id) {
      this.purchasingService.obtenerOrden(id).subscribe({
        next: (orden) => {
          this.orden.set(orden);
          this.proveedorId = orden.proveedor?.idProveedor ?? null;
          this.fechaEsperada = orden.fechaEsperada ? String(orden.fechaEsperada).slice(0, 10) : '';
          this.observaciones = orden.observaciones ?? '';
          this.cargando.set(false);
        },
        error: () => {
          this.notificacion.error('No se pudo cargar la orden.');
          this.router.navigate(['/purchasing/purchase-history']);
        }
      });
    } else {
      this.router.navigate(['/purchasing/purchase-history']);
    }
  }

  guardar(): void {
    const orden = this.orden();
    if (!orden || this.guardando()) return;
    if (!this.proveedorId) {
      this.notificacion.advertencia('Selecciona un proveedor.');
      return;
    }

    // El PATCH reemplaza los detalles: se reenvian los existentes sin cambios.
    const request = {
      proveedorId: Number(this.proveedorId),
      fechaEsperada: this.fechaEsperada ? `${this.fechaEsperada}T00:00:00` : undefined,
      observaciones: this.observaciones.trim() || undefined,
      items: (orden.detalles ?? []).map(d => ({
        productoId: d.productoId,
        cantidad: d.cantidadPedida,
        precioUnitario: d.precioUnitarioPactado
      }))
    };

    this.guardando.set(true);
    this.purchasingService.actualizarOrden(orden.idOrden, request as any).subscribe({
      next: () => {
        this.notificacion.exito(`Orden OC-${orden.idOrden} actualizada`);
        this.guardando.set(false);
        this.router.navigate(['/purchasing/purchase-detail', orden.idOrden]);
      },
      error: (err) => {
        this.notificacion.error(err?.error?.message ?? 'No se pudo actualizar la orden.');
        this.guardando.set(false);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/purchasing/purchase-detail', this.orden()?.idOrden ?? '']);
  }
}