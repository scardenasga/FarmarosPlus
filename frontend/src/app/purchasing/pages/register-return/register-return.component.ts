import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { PurchasingService } from '../../services/purchasing.service';
import { Supplier, SupplierDetalleResponse, SupplierProductRel } from '../../../supplier/models/supplier.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { VentaResponse } from '../../models/purchasing.model';

type TipoDevolucion = 'proveedor' | 'cliente';

interface ItemDevolucion {
  idProducto: number;
  nombreProducto: string;
  idLote: number | null;
  numeroLote: string | null;
  cantidadDisponible: number;
  cantidad: number;
  idDetalleVenta?: number;
}

interface LoteProveedorResult {
  id: number;
  numeroLote: string | null;
  cantidad: number;
  fechaVencimiento: string | null;
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
  private route = inject(ActivatedRoute);

  tipoDevolucion = signal<TipoDevolucion>('proveedor');

  // Proveedor
  proveedores = signal<Supplier[]>([]);
  busquedaProveedor = signal<string>('');
  idProveedorSeleccionado = signal<number | null>(null);
  mostrarListaProveedores = signal<boolean>(true);
  detalleProveedor = signal<SupplierDetalleResponse | null>(null);
  busquedaProductoProveedor = signal<string>('');

  // Cliente
  nombreCliente = signal<string>('');
  documentoCliente = signal<string>('');
  idVenta = signal<string>('');
  ventaCargada = signal<VentaResponse | null>(null);

  // Productos a devolver
  items = signal<ItemDevolucion[]>([]);
  productoSeleccionado = signal<SupplierProductRel | null>(null);
  lotes = signal<LoteProveedorResult[]>([]);
  idLoteSeleccionado = signal<number | null>(null);
  cantidadNueva = signal<number>(1);
  cargandoLotes = signal<boolean>(false);
  cargandoVenta = signal<boolean>(false);

  motivo = signal<string>('');
  enviando = signal<boolean>(false);
  error = signal<string>('');
  errorVenta = signal<string>('');
  errorProducto = signal<string>('');
  cargandoProveedores = signal<boolean>(false);
  cargandoDetalleProveedor = signal<boolean>(false);

  esProveedor = computed(() => this.tipoDevolucion() === 'proveedor');
  esCliente = computed(() => this.tipoDevolucion() === 'cliente');

  proveedoresFiltrados = computed(() => {
    const termino = this.busquedaProveedor().trim().toLowerCase();
    if (!termino) return this.proveedores();
    return this.proveedores().filter(p =>
      [p.nombre, p.nit ?? '', p.contacto ?? '', p.telefono ?? '']
        .some(valor => valor.toLowerCase().includes(termino))
    );
  });

  productosProveedorFiltrados = computed(() => {
    const detalle = this.detalleProveedor();
    const termino = this.busquedaProductoProveedor().trim().toLowerCase();
    const productos = detalle?.productos ?? [];
    if (!termino) {
      return [];
    }
    return productos.filter(prod =>
      [prod.nombre, prod.codigoBarras, prod.codigoProductoProveedor ?? '', prod.descripcion ?? '']
        .some(valor => valor.toLowerCase().includes(termino))
    );
  });

  puedeEnviar = computed(() => {
    const tieneDetalles = this.items().some(item => item.cantidad > 0);
    const baseValida = this.esProveedor()
      ? this.idProveedorSeleccionado() !== null
      : this.ventaCargada() !== null;
    return baseValida && tieneDetalles && !this.enviando();
  });

  productoSeleccionadoLote = computed(() => {
    return this.lotes().find(l => l.id === this.idLoteSeleccionado()) ?? null;
  });

  proveedorSeleccionado(): Supplier | null {
    const id = this.idProveedorSeleccionado();
    if (id === null) {
      return null;
    }
    return this.proveedores().find(proveedor => proveedor.idProveedor === id) ?? null;
  }

  productosDelProveedor(): SupplierProductRel[] {
    return this.detalleProveedor()?.productos ?? [];
  }

  cantidadItemsCliente(): number {
    return this.items().filter(item => item.cantidad > 0).length;
  }

  ngOnInit(): void {
    const tipo = this.route.snapshot.queryParamMap.get('tipo');
    if (tipo === 'cliente') {
      this.tipoDevolucion.set('cliente');
    }

    if (this.esProveedor()) {
      this.cargarProveedores();
    }
  }

  volver(): void {
    this.router.navigate(['/purchasing/return-history'], {
      queryParams: { tipo: this.tipoDevolucion() }
    });
  }

  onTipoChange(tipo: TipoDevolucion): void {
    if (this.tipoDevolucion() === tipo) return;
    this.tipoDevolucion.set(tipo);
    this.reiniciarFormulario();
    if (tipo === 'proveedor') {
      this.cargarProveedores();
    }
  }

  seleccionarProveedor(proveedor: Supplier): void {
    this.idProveedorSeleccionado.set(proveedor.idProveedor);
    this.mostrarListaProveedores.set(false);
    this.cargarDetalleProveedor(proveedor.idProveedor);
  }

  cargarProveedores(): void {
    this.cargandoProveedores.set(true);
    this.supplierService.listActive().subscribe({
      next: data => {
        this.proveedores.set(data);
        this.cargandoProveedores.set(false);
      },
      error: () => this.cargandoProveedores.set(false)
    });
  }

  cargarDetalleProveedor(proveedorId: number): void {
    this.cargandoDetalleProveedor.set(true);
    this.detalleProveedor.set(null);
    this.busquedaProductoProveedor.set('');
    this.productoSeleccionado.set(null);
    this.lotes.set([]);
    this.idLoteSeleccionado.set(null);
    this.cantidadNueva.set(1);
    this.errorProducto.set('');

    this.supplierService.getDetail(proveedorId).subscribe({
      next: detalle => {
        this.detalleProveedor.set(detalle);
        this.cargandoDetalleProveedor.set(false);
      },
      error: () => {
        this.cargandoDetalleProveedor.set(false);
        this.errorProducto.set('No se pudo cargar el detalle del proveedor.');
      }
    });
  }

  cargarVenta(): void {
    const valor = Number(this.idVenta());
    if (!valor) {
      this.errorVenta.set('Ingrese el id de la venta.');
      return;
    }

    this.cargandoVenta.set(true);
    this.errorVenta.set('');
    this.ventaCargada.set(null);
    this.items.set([]);

    this.purchasingService.obtenerVenta(valor).subscribe({
      next: venta => {
        this.ventaCargada.set(venta);
        this.items.set(
          venta.detalles.map(detalle => ({
            idProducto: detalle.producto.id,
            nombreProducto: detalle.producto.nombre,
            idLote: detalle.lote?.idLote ?? null,
            numeroLote: detalle.lote?.numeroLote ?? null,
            cantidadDisponible: detalle.cantidad,
            cantidad: 0,
            idDetalleVenta: detalle.id
          }))
        );
        this.cargandoVenta.set(false);
      },
      error: () => {
        this.errorVenta.set('No se pudo cargar la venta.');
        this.cargandoVenta.set(false);
      }
    });
  }

  seleccionarProducto(prod: SupplierProductRel): void {
    this.productoSeleccionado.set(prod);
    this.idLoteSeleccionado.set(null);
    this.cantidadNueva.set(1);
    this.errorProducto.set('');
    this.lotes.set([]);
    this.cargandoLotes.set(true);

    this.purchasingService.obtenerLotes(prod.id).subscribe({
      next: data => {
        this.lotes.set(data as LoteProveedorResult[]);
        this.cargandoLotes.set(false);
      },
      error: () => {
        this.cargandoLotes.set(false);
        this.errorProducto.set('No se pudieron cargar los lotes.');
      }
    });
  }

  agregarItemProveedor(): void {
    const producto = this.productoSeleccionado();
    const lote = this.productoSeleccionadoLote();

    if (!producto) {
      this.errorProducto.set('Seleccione un producto.');
      return;
    }
    if (!lote) {
      this.errorProducto.set('Seleccione un lote.');
      return;
    }
    if (this.cantidadNueva() < 1) {
      this.errorProducto.set('La cantidad debe ser al menos 1.');
      return;
    }
    if (this.cantidadNueva() > lote.cantidad) {
      this.errorProducto.set(`Stock disponible en este lote: ${lote.cantidad}.`);
      return;
    }

    this.items.update(prev => {
      const existente = prev.find(item => item.idLote === lote.id);
      if (existente) {
        const total = existente.cantidad + this.cantidadNueva();
        if (total > lote.cantidad) {
          this.errorProducto.set(`No puede devolver más de ${lote.cantidad} unidades de este lote.`);
          return prev;
        }
        return prev.map(item => item.idLote === lote.id ? { ...item, cantidad: total } : item);
      }

      return [
        ...prev,
        {
          idProducto: producto.id,
          nombreProducto: producto.nombre,
          idLote: lote.id,
          numeroLote: lote.numeroLote,
          cantidadDisponible: lote.cantidad,
          cantidad: this.cantidadNueva()
        }
      ];
    });

    this.productoSeleccionado.set(null);
    this.lotes.set([]);
    this.idLoteSeleccionado.set(null);
    this.cantidadNueva.set(1);
    this.busquedaProductoProveedor.set('');
  }

  quitarItem(index: number): void {
    this.items.update(prev => {
      const copy = [...prev];
      copy.splice(index, 1);
      return copy;
    });
  }

  actualizarCantidadProveedor(index: number, cantidad: number): void {
    this.items.update(prev =>
      prev.map((item, currentIndex) =>
        currentIndex === index ? { ...item, cantidad: Number(cantidad) || 0 } : item
      )
    );
  }

  enviar(): void {
    if (!this.puedeEnviar()) return;
    this.enviando.set(true);
    this.error.set('');

    if (this.esProveedor()) {
      this.purchasingService.registrarDevolucion({
        idProveedor: this.idProveedorSeleccionado()!,
        usuarioResponsable: 'admin',
        motivo: this.motivo(),
        detalles: this.items().map(i => ({
          idProducto: i.idProducto,
          idLote: i.idLote!,
          cantidad: i.cantidad
        }))
      }).subscribe({
        next: () => this.router.navigate(['/purchasing/return-history'], {
          queryParams: { tipo: 'proveedor' }
        }),
        error: err => {
          this.error.set(err?.error?.message ?? 'Error al registrar la devolución.');
          this.enviando.set(false);
        }
      });
      return;
    }

    const detalles = this.items()
      .filter(item => item.cantidad > 0)
      .map(item => ({
        idDetalleVenta: item.idDetalleVenta!,
        cantidad: item.cantidad
      }));

    this.purchasingService.registrarDevolucionCliente({
      idVenta: Number(this.idVenta()),
      usuarioResponsable: 'admin',
      nombreCliente: this.nombreCliente().trim() || undefined,
      documentoCliente: this.documentoCliente().trim() || undefined,
      motivo: this.motivo().trim() || undefined,
      detalles
    }).subscribe({
      next: () => this.router.navigate(['/purchasing/return-history'], {
        queryParams: { tipo: 'cliente' }
      }),
      error: err => {
        this.error.set(err?.error?.message ?? 'Error al registrar la devolución.');
        this.enviando.set(false);
      }
    });
  }

  private reiniciarFormulario(): void {
    this.idProveedorSeleccionado.set(null);
    this.busquedaProveedor.set('');
    this.mostrarListaProveedores.set(true);
    this.detalleProveedor.set(null);
    this.busquedaProductoProveedor.set('');
    this.motivo.set('');
    this.nombreCliente.set('');
    this.documentoCliente.set('');
    this.idVenta.set('');
    this.ventaCargada.set(null);
    this.items.set([]);
    this.productoSeleccionado.set(null);
    this.lotes.set([]);
    this.idLoteSeleccionado.set(null);
    this.cantidadNueva.set(1);
    this.error.set('');
    this.errorVenta.set('');
    this.errorProducto.set('');
    this.cargandoDetalleProveedor.set(false);
  }
}
