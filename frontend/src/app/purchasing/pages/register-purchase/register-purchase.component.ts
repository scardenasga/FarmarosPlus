import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { PurchasingService } from '../../services/purchasing.service';
import { Supplier, SupplierDetalleResponse, SupplierProductRel } from '../../../supplier/models/supplier.model';
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
  busquedaProveedor = signal<string>('');
  mostrarListaProveedores = signal<boolean>(true);
  detalleProveedor = signal<SupplierDetalleResponse | null>(null);
  busquedaProductoProveedor = signal<string>('');
  cargandoDetalleProveedor = signal<boolean>(false);
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

  proveedoresFiltrados = computed(() => {
    const termino = this.busquedaProveedor().trim().toLowerCase();
    if (!termino) return this.proveedores();
    return this.proveedores().filter(p =>
      [p.nombre, p.nit ?? '', p.contacto ?? '', p.telefono ?? '']
        .some(valor => valor.toLowerCase().includes(termino))
    );
  });

  productosProveedorFiltrados = computed(() => {
    const termino = this.busquedaProductoProveedor().trim().toLowerCase();
    const productos = this.detalleProveedor()?.productos ?? [];
    if (!termino) return [];
    return productos.filter(prod =>
      [prod.nombre, prod.codigoBarras, prod.codigoProductoProveedor ?? '', prod.descripcion ?? '']
        .some(valor => valor.toLowerCase().includes(termino))
    );
  });

  totalCompra = computed(() => {
    return this.items().reduce((s, i) => s + i.cantidad * i.precioUnitario, 0);
  });

  puedeEnviar = computed(() => {
    return this.idProveedorSeleccionado() !== null && this.items().length > 0 && !this.enviando();
  });

  seleccionarProveedor(proveedor: Supplier): void {
    this.idProveedorSeleccionado.set(proveedor.idProveedor);
    this.busquedaProveedor.set('');
    this.mostrarListaProveedores.set(false);
    this.items.set([]);
    this.cargarDetalleProveedor(proveedor.idProveedor);
  }

  proveedorSeleccionado(): Supplier | null {
    const id = this.idProveedorSeleccionado();
    return id === null ? null : this.proveedores().find(p => p.idProveedor === id) ?? null;
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

  cargarCompra(id: number): void {
    this.purchasingService.obtenerOrden(id).subscribe({
      next: compra => {
        this.idProveedorSeleccionado.set(compra.proveedor.idProveedor);
        this.numeroFactura.set('');
        this.notas.set(compra.observaciones ?? '');
        this.items.set(compra.detalles.map(d => ({
          idProducto: d.productoId!,
          nombreProducto: d.nombreProducto,
          cantidad: d.cantidadPedida,
          precioUnitario: d.precioUnitarioPactado
        })));
        this.cargarDetalleProveedor(compra.proveedor.idProveedor);
        this.mostrarListaProveedores.set(false);
      },
      error: () => this.error.set('No se pudo cargar la compra para editarla.')
    });
  }

  cargarDetalleProveedor(proveedorId: number): void {
    this.cargandoDetalleProveedor.set(true);
    this.detalleProveedor.set(null);
    this.busquedaProductoProveedor.set('');
    this.productoSeleccionado.set(null);
    this.supplierService.getDetail(proveedorId).subscribe({
      next: detalle => {
        this.detalleProveedor.set(detalle);
        this.cargandoDetalleProveedor.set(false);
      },
      error: () => this.cargandoDetalleProveedor.set(false)
    });
  }

  buscarAlEscribir(termino: string): void {
    this.busquedaProductoProveedor.set(termino);
  }

  seleccionarProductoProveedor(producto: SupplierProductRel): void {
    this.productoSeleccionado.set(producto);
    this.cantidadModal.set(1);
    this.precioModal.set(producto.precioReferencia ?? 0);
    this.errorModal.set('');
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
    this.productoSeleccionado.set(null);
    this.resultados.set([]);
    this.termino.set('');
    this.errorModal.set('');
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
