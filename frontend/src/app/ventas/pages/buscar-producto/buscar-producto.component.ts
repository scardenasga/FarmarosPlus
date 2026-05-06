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
    this.router.navigate(['/ventas/historial']);
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
        if (!lotes || lotes.length === 0) {
          this.errorLote.set(`"${producto.nombre}" no tiene lotes disponibles`);
          return;
        }
        this.productosEnVenta.update(prev => [
          ...prev,
          {
            ...producto,
            cantidad: 1,
            loteId: lotes[0].id,
            numeroLote: lotes[0].numeroLote
          }
        ]);
      },
      error: () => {
        this.errorLote.set(`No se pudo obtener el lote de "${producto.nombre}"`);
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
