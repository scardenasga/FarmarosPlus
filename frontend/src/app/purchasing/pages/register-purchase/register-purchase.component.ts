import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { PurchasingService } from '../../services/purchasing.service';
import { Supplier } from '../../../supplier/models/supplier.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

interface ItemCompra {
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
}

@Component({
  selector: 'app-register-purchase',
  standalone: true,
  imports: [CommonModule, FormsModule, TopBarComponent],
  templateUrl: './register-purchase.component.html',
  styleUrl: './register-purchase.component.css'
})
export class RegisterPurchaseComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private supplierService = inject(SupplierService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  proveedores = signal<Supplier[]>([]);
  idProveedorSeleccionado = signal<number | null>(null);
  numeroFactura = signal<string>('');
  notas = signal<string>('');
  items = signal<ItemCompra[]>([]);

  // Modal
  modalVisible = signal<boolean>(false);
  termino = signal<string>('');
  buscando = signal<boolean>(false);
  resultados = signal<any[]>([]);
  productoSeleccionado = signal<any | null>(null);
  cantidadModal = signal<number>(1);
  precioModal = signal<number>(0);
  errorModal = signal<string>('');

  // Submit
  enviando = signal<boolean>(false);
  error = signal<string>('');
  cargandoProveedores = signal<boolean>(true);
  editando = signal<boolean>(false);
  idCompra = signal<number | null>(null);

  totalCompra = computed(() => {
    return this.items().reduce((s, i) => s + i.cantidad * i.precioUnitario, 0);
  });

  puedeEnviar = computed(() => {
    return this.idProveedorSeleccionado() !== null && this.items().length > 0 && !this.enviando();
  });

  onProveedorChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const value = target.value;
    this.idProveedorSeleccionado.set(value === 'null' ? null : Number(value));
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.editando.set(true);
      this.idCompra.set(id);
      this.cargarCompra(id);
    }
    this.supplierService.listActive().subscribe({
      next: data => {
        this.proveedores.set(data);
        this.cargandoProveedores.set(false);
      },
      error: () => this.cargandoProveedores.set(false)
    });
  }

  volver(): void {
    this.router.navigate(['/purchasing/purchase-history']);
  }

  abrirModal(): void {
    this.termino.set('');
    this.resultados.set([]);
    this.productoSeleccionado.set(null);
    this.cantidadModal.set(1);
    this.precioModal.set(0);
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
    this.cantidadModal.set(1);
    this.precioModal.set(prod.costo ?? 0);
    this.errorModal.set('');
  }

  agregarItem(): void {
    const prod = this.productoSeleccionado();
    if (!prod) { this.errorModal.set('Seleccione un producto.'); return; }
    if (this.cantidadModal() < 1) { this.errorModal.set('La cantidad debe ser al menos 1.'); return; }
    if (this.precioModal() < 0) { this.errorModal.set('El precio no puede ser negativo.'); return; }

    this.items.update(prev => {
      const existente = prev.find(i => i.idProducto === prod.id);
      if (existente) {
        return prev.map(i => i.idProducto === prod.id 
          ? { ...i, cantidad: i.cantidad + this.cantidadModal() } 
          : i);
      } else {
        return [...prev, {
          idProducto: prod.id,
          nombreProducto: prod.nombre,
          cantidad: this.cantidadModal(),
          precioUnitario: this.precioModal()
        }];
      }
    });
    this.cerrarModal();
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

    const datos = {
      proveedorId: this.idProveedorSeleccionado()!,
      items: this.items().map(i => ({ productoId: i.idProducto, cantidad: i.cantidad, precioUnitario: i.precioUnitario })),
      observaciones: [this.numeroFactura() ? `Factura prevista: ${this.numeroFactura()}` : '', this.notas()].filter(Boolean).join(' · ') || undefined
    };
    const peticion = this.editando()
      ? this.purchasingService.actualizarOrden(this.idCompra()!, datos)
      : this.purchasingService.registrarOrden(datos);
    peticion.subscribe({
      next: () => this.router.navigate(['/purchasing/purchase-history']),
      error: (err) => {
        this.error.set(err?.error?.message ?? 'No se pudo guardar la compra.');
        this.enviando.set(false);
      }
    });
  }
}
