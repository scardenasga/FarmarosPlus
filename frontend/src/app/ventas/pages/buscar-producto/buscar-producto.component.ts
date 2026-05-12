import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { VentaService } from '../../services/venta.service';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { ProductSearchCardComponent } from '../../components/product-search-card/product-search-card.component';

@Component({
  selector: 'app-buscar-producto',
  standalone: true,
  imports: [
    CommonModule,
    TopBarComponent,
    SearchBarComponent,
    ProductSearchCardComponent
  ],
  templateUrl: './buscar-producto.component.html',
  styleUrl: './buscar-producto.component.css'
})
export class BuscarProductoComponent {
  private ventaService = inject(VentaService);
  private router = inject(Router);

  criterio = signal<string>('nombre');
  busqueda = signal<string>('');
  productos = signal<any[]>([]);
  productosEnVenta = signal<any[]>([]);
  sinResultados = signal<boolean>(false);
  errorLote = signal<string>('');

  totalProductosSeleccionados = computed(() => this.productosEnVenta().length);
  haySeleccion = computed(() => this.productosEnVenta().length > 0);

  handleBack() {
    this.router.navigate(['/ventas']);
  }

  handleSearch(termino: string) {
    this.busqueda.set(termino);
    if (!termino.trim()) {
      this.productos.set([]);
      this.sinResultados.set(false);
      return;
    }

    this.ventaService.buscarProductos(termino, this.criterio()).subscribe({
      next: (data) => {
        this.productos.set(data);
        this.sinResultados.set(data.length === 0);
      },
      error: () => {
        this.productos.set([]);
        this.sinResultados.set(true);
      }
    });
  }

  agregarProducto(producto: any) {
    const existente = this.productosEnVenta().find(p => p.id === producto.id);
    if (existente) {
      this.aumentarCantidad(producto);
      return;
    }

    this.errorLote.set('');
    this.ventaService.obtenerLotesDisponibles(producto.id).subscribe({
      next: (lotes) => {
        // 1. Comentamos el bloqueo (como ya hiciste)
        /*if (!lotes || lotes.length === 0) {
          this.errorLote.set(`"${producto.nombre}" no tiene lotes disponibles`);
          return;
        }*/

        // 2. USAMOS PROTECCIÓN: Si hay lote lo pone, si no, pone null.
        // Esto evita el error de "Cannot read properties of undefined (reading 'id')"
        const primerLoteId = (lotes && lotes.length > 0) ? lotes[0].id : null;
        const primerNumeroLote = (lotes && lotes.length > 0) ? lotes[0].numeroLote : 'SIN LOTE';

        this.productosEnVenta.update(prev => [
          ...prev,
          {
            ...producto,
            cantidad: 1,
            loteId: primerLoteId,      // Ya no explota si es null
            numeroLote: primerNumeroLote
          }
        ]);
      },
      error: () => {
        // 3. Incluso si el servidor da error de lotes, deja agregar el producto
        this.productosEnVenta.update(prev => [
          ...prev,
          {
            ...producto,
            cantidad: 1,
            loteId: null,
            numeroLote: 'SIN LOTE'
          }
        ]);
      }
    });
  }

  getCantidad(producto: any): number {
    const p = this.productosEnVenta().find(p => p.id === producto.id);
    return p ? p.cantidad : 0;
  }

  aumentarCantidad(producto: any) {
    this.productosEnVenta.update(prev => 
      prev.map(p => p.id === producto.id ? { ...p, cantidad: p.cantidad + 1 } : p)
    );
  }

  reducirCantidad(producto: any) {
    const p = this.productosEnVenta().find(p => p.id === producto.id);
    if (!p) return;

    if (p.cantidad <= 1) {
      this.productosEnVenta.update(prev => prev.filter(x => x.id !== producto.id));
    } else {
      this.productosEnVenta.update(prev => 
        prev.map(item => item.id === producto.id ? { ...item, cantidad: item.cantidad - 1 } : item)
      );
    }
  }

  irARegistrarVenta() {
    sessionStorage.setItem('productosVenta', JSON.stringify(this.productosEnVenta()));
    this.router.navigate(['/ventas/registrar']);
  }
}
