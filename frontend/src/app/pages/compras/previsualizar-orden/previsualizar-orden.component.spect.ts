import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router'; // Añadido por si acaso
import { CompraService } from '../../../ventas/services/compra.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { BottomNavComponent } from '../../../shared/components/bottom-nav/bottom-nav.component';

@Component({
  selector: 'app-previsualizar-orden',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SearchBarComponent, BottomNavComponent],
  templateUrl: './previsualizar-orden.component.html',
  styleUrls: ['./previsualizar-orden.component.css'] // Verifica que el archivo CSS exista
})
export class PrevisualizarOrdenComponent implements OnInit {
  productosOrden: any[] = [];
  productosFiltrados: any[] = [];
  proveedorId: number = 1;
  nombreProveedor: string = '';
  total: number = 0;

  constructor(private compraService: CompraService) {}

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.compraService.obtenerPrevisualizacion(this.proveedorId).subscribe({
      next: (res) => {
        this.nombreProveedor = res.nombreProveedor;
        this.productosOrden = res.items || [];
        this.productosFiltrados = [...this.productosOrden];
        this.calcularTotal();
      }
    });
  }

  onSearch(termino: string) {
    if (!termino) {
      this.productosFiltrados = [...this.productosOrden];
    } else {
      this.productosFiltrados = this.productosOrden.filter(p =>
        p.nombreProducto.toLowerCase().includes(termino.toLowerCase())
      );
    }
    this.calcularTotal();
  }

  eliminarProducto(index: number) {
    this.productosFiltrados.splice(index, 1);
    this.calcularTotal();
  }

  calcularTotal() {
    this.total = this.productosFiltrados.reduce((acc, item) =>
      acc + (item.cantidadSugerida * item.costo), 0);
  }

  confirmarOrdenCompleta() {
    console.log('Orden confirmada');
  }
}
