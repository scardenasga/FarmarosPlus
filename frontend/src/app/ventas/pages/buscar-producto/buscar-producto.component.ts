import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VentaService } from '../../services/venta.service';

@Component({
  selector: 'app-buscar-producto',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './buscar-producto.component.html',
  styleUrl: './buscar-producto.component.css'
})
export class BuscarProductoComponent {

  criterio: string = 'nombre';
  busqueda: string = '';
  productos: any[] = [];
  productosEnVenta: any[] = [];
  sinResultados: boolean = false;
  errorLote: string = '';

  constructor(private ventaService: VentaService, private router: Router) {}


  volver() {
    this.router.navigate(['/ventas/historial']);
  }

  buscar() {
    if (!this.busqueda.trim()) {
      this.productos = [];
      this.sinResultados = false;
      return;
    }
    this.ventaService.buscarProductos(this.busqueda, this.criterio).subscribe({
      next: (data) => {
        this.productos = data;
        this.sinResultados = data.length === 0;
      },
      error: () => {
        this.productos = [];
        this.sinResultados = true;
      }
    });
  }

  agregarProducto(producto: any) {
    const existente = this.productosEnVenta.find(p => p.id === producto.id);
    if (existente) {
      existente.cantidad++;
      return;
    }

    this.errorLote = '';
    this.ventaService.obtenerLotesDisponibles(producto.id).subscribe({
      next: (lotes) => {
        console.log('LOTES:', JSON.stringify(lotes));
        if (!lotes || lotes.length === 0) {
          this.errorLote = `"${producto.nombre}" no tiene lotes disponibles`;
          return;
        }
        this.productosEnVenta.push({
          ...producto,
          cantidad: 1,
          loteId: lotes[0].id,
          numeroLote: lotes[0].numeroLote
        });
        console.log('PRODUCTO AGREGADO:', JSON.stringify(this.productosEnVenta));
      },
      error: () => {
        this.errorLote = `No se pudo obtener el lote de "${producto.nombre}"`;
      }
    });
  }

  irARegistrarVenta() {
    sessionStorage.setItem('productosVenta', JSON.stringify(this.productosEnVenta));
    this.router.navigate(['/ventas/registrar']);
  }

  get totalSeleccionados(): number {
    return this.productosEnVenta.reduce((acc, p) => acc + p.cantidad, 0);
  }

  estaEnVenta(producto: any): boolean {
    return this.productosEnVenta.some(p => p.id === producto.id);
  }

  getCantidad(producto: any): number {
    const p = this.productosEnVenta.find(p => p.id === producto.id);
    return p ? p.cantidad : 0;
  }

  aumentarCantidad(producto: any) {
    const p = this.productosEnVenta.find(p => p.id === producto.id);
    if (p) p.cantidad++;
  }

  reducirCantidad(producto: any) {
    const p = this.productosEnVenta.find(p => p.id === producto.id);
    if (!p) return;
    p.cantidad--;
    if (p.cantidad <= 0) {
      this.productosEnVenta = this.productosEnVenta.filter(x => x.id !== producto.id);
    }
  }
}
