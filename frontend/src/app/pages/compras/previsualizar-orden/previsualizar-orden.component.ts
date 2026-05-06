import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompraService } from '../../../ventas/services/compra.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { BottomNavComponent } from '../../../shared/components/bottom-nav/bottom-nav.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-previsualizar-orden',
  standalone: true,
  imports: [CommonModule, FormsModule, SearchBarComponent, BottomNavComponent],
  templateUrl: './previsualizar-orden.component.html',
  styleUrls: ['./previsualizar-orden.component.css']
})
export class PrevisualizarOrdenComponent implements OnInit {
  productosOrden: any[] = [];
  productosFiltrados: any[] = [];
  proveedorId: number = 1;
  nombreProveedor: string = 'Cargando...';
  total: number = 0;

  constructor(private compraService: CompraService, private router: Router) {}

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.compraService.obtenerPrevisualizacion(this.proveedorId).subscribe({
      next: (res) => {
        this.nombreProveedor = res.proveedorNombre;
        this.productosOrden = (res.items || []).map((item: any) => ({
          nombreProducto: item.nombre,
          cantidadSugerida: item.cantidadSugerida || 0,
          costo: item.precioUnitario || 0,
          justificacion: item.motivo || 'Stock Bajo'
        }));
        this.productosFiltrados = [...this.productosOrden];
        this.calcularTotal();
      }
    });
  }

  eliminarProducto(index: number) {
    this.productosOrden.splice(index, 1);
    this.productosFiltrados = [...this.productosOrden];
    this.calcularTotal();
  }

  calcularTotal() {
    this.total = this.productosOrden.reduce((acc, item) => {
      return acc + (Number(item.cantidadSugerida || 0) * Number(item.costo || 0));
    }, 0);
  }

 onSearch(termino: string) {
  const t = termino.toLowerCase().trim();

  // 1. Si no hay texto, volvemos a la lista original y salimos
  if (!t) {
    this.productosFiltrados = [...this.productosOrden];
    return;
  }

  // 2. Filtro local: Evitamos ir al servidor si ya tenemos el producto en pantalla
  const locales = this.productosOrden.filter(p =>
    p.nombreProducto.toLowerCase().includes(t)
  );

  if (locales.length > 0) {
    this.productosFiltrados = locales;
    return; // Si lo encontramos localmente, NO llamamos a la API
  }

  // 3. Solo llamamos a la API si el término es de 3 o más letras
  if (t.length >= 3) {
    this.compraService.buscarProductos(t).subscribe({
      next: (res) => {
        this.productosFiltrados = res || [];
      },
      error: (err) => {
        // Aquí capturamos el error 500 para que la app no explote
        console.warn('El servidor tiene problemas con esta búsqueda, pero la app sigue viva.');
        this.productosFiltrados = [];
      }
    });
  }
}
  confirmarOrdenCompleta() {
    this.compraService.confirmarPedidoFinal({
      proveedorId: this.proveedorId,
      items: this.productosOrden,
      totalFinal: this.total
    }).subscribe(() => {
      alert('Orden confirmada');
      this.router.navigate(['/compras/historial']);
    });
  }
}
