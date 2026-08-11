import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { PurchasingService } from '../../services/purchasing.service';
import { Supplier } from '../../../supplier/models/supplier.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

interface ItemDevolucion {
  idProducto: number;
  nombreProducto: string;
  idLote: number;
  numeroLote: string;
  cantidadDisponible: number;
  cantidad: number;
}

@Component({
  selector: 'app-register-return',
  standalone: true,
  imports: [CommonModule, FormsModule, TopBarComponent],
  templateUrl: './register-return.component.html',
  styleUrl: './register-return.component.css'
})
export class RegisterReturnComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private supplierService = inject(SupplierService);
  private router = inject(Router);

  proveedores = signal<Supplier[]>([]);
  idProveedorSeleccionado = signal<number | null>(null);
  motivo = signal<string>('');
  items = signal<ItemDevolucion[]>([]);

  // Modal agregar producto
  modalVisible = signal<boolean>(false);
  paso = signal<'buscar' | 'lote'>('buscar');
  termino = signal<string>('');
  buscando = signal<boolean>(false);
  resultados = signal<any[]>([]);
  productoSeleccionado = signal<any | null>(null);
  lotes = signal<any[]>([]);
  idLoteSeleccionado = signal<number | null>(null);
  cantidadModal = signal<number>(1);
  errorModal = signal<string>('');

  // Submit
  enviando = signal<boolean>(false);
  error = signal<string>('');
  cargandoProveedores = signal<boolean>(true);

  puedeEnviar = computed(() => {
    return this.idProveedorSeleccionado() !== null && this.items().length > 0 && !this.enviando();
  });

  onProveedorChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const value = target.value;
    this.idProveedorSeleccionado.set(value === 'null' ? null : Number(value));
  }

  ngOnInit(): void {
    this.supplierService.listActive().subscribe({
      next: data => {
        this.proveedores.set(data);
        this.cargandoProveedores.set(false);
      },
      error: () => this.cargandoProveedores.set(false)
    });
  }

  volver(): void {
    this.router.navigate(['/purchasing/return-history']);
  }

  abrirModal(): void {
    this.paso.set('buscar');
    this.termino.set('');
    this.resultados.set([]);
    this.productoSeleccionado.set(null);
    this.lotes.set([]);
    this.idLoteSeleccionado.set(null);
    this.cantidadModal.set(1);
    this.errorModal.set('');
    this.modalVisible.set(true);
  }

  cerrarModal(): void {
    this.modalVisible.set(false);
  }

  buscar(): void {
    const term = this.termino().trim();
    if (!term) return;
    this.buscando.set(true);
    this.purchasingService.buscarProductos(term).subscribe({
      next: data => {
        this.resultados.set(data);
        this.buscando.set(false);
      },
      error: () => this.buscando.set(false)
    });
  }

  seleccionarProducto(prod: any): void {
    this.productoSeleccionado.set(prod);
    this.paso.set('lote');
    this.errorModal.set('');
    this.purchasingService.obtenerLotes(prod.id).subscribe({
      next: data => this.lotes.set(data),
      error: () => this.errorModal.set('No se pudieron cargar los lotes.')
    });
  }

  loteSeleccionado = computed(() => {
    return this.lotes().find(l => l.id === this.idLoteSeleccionado());
  });

  agregarItem(): void {
    const lote = this.loteSeleccionado();
    const prod = this.productoSeleccionado();
    if (!lote) { this.errorModal.set('Seleccione un lote.'); return; }
    if (this.cantidadModal() < 1) { this.errorModal.set('La cantidad debe ser al menos 1.'); return; }
    if (this.cantidadModal() > lote.cantidad) {
      this.errorModal.set(`Stock disponible en este lote: ${lote.cantidad}.`);
      return;
    }

    this.items.update(prev => {
      const existente = prev.find(i => i.idLote === lote.id);
      if (existente) {
        const total = existente.cantidad + this.cantidadModal();
        if (total > lote.cantidad) {
          this.errorModal.set(`No puede devolver más de ${lote.cantidad} unidades de este lote.`);
          return prev;
        }
        return prev.map(i => i.idLote === lote.id ? { ...i, cantidad: total } : i);
      } else {
        return [...prev, {
          idProducto: prod.id,
          nombreProducto: prod.nombre,
          idLote: lote.id,
          numeroLote: lote.numeroLote,
          cantidadDisponible: lote.cantidad,
          cantidad: this.cantidadModal()
        }];
      }
    });

    if (!this.errorModal()) {
      this.cerrarModal();
    }
  }

  quitarItem(index: number): void {
    this.items.update(prev => {
      const copy = [...prev];
      copy.splice(index, 1);
      return copy;
    });
  }

  enviar(): void {
    if (!this.puedeEnviar()) return;
    this.enviando.set(true);
    this.error.set('');

    this.purchasingService.registrarDevolucion({
      idProveedor: this.idProveedorSeleccionado()!,
      usuarioResponsable: 'admin',
      motivo: this.motivo(),
      detalles: this.items().map(i => ({
        idProducto: i.idProducto,
        idLote: i.idLote,
        cantidad: i.cantidad
      }))
    }).subscribe({
      next: () => this.router.navigate(['/purchasing/return-history']),
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Error al registrar la devolución.');
        this.enviando.set(false);
      }
    });
  }
}
