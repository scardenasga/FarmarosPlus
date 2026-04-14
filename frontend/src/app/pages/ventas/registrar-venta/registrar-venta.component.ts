import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VentaService } from '../../../services/venta.service';

@Component({
  selector: 'app-registrar-venta',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './registrar-venta.component.html',
  styleUrl: './registrar-venta.component.css'
})
export class RegistrarVentaComponent implements OnInit {

  productos: any[] = [];
  metodoPago: string = 'EFECTIVO';
  usuarioId: number = 1;
  cargando: boolean = false;
  error: string = '';
  hoy: Date = new Date();

  constructor(private ventaService: VentaService, private router: Router) {}

  ngOnInit() {
    const data = sessionStorage.getItem('productosVenta');
    if (data) {
      this.productos = JSON.parse(data);
      console.log('PRODUCTOS CARGADOS:', JSON.stringify(this.productos));
    } else {
      this.router.navigate(['/ventas/buscar']);
    }
  }

  cambiarCantidad(producto: any, delta: number) {
    producto.cantidad += delta;
    if (producto.cantidad <= 0) {
      this.productos = this.productos.filter(p => p.id !== producto.id);
    }
  }

  get subtotal(): number {
    return this.productos.reduce((acc, p) => acc + (p.precioVenta * p.cantidad), 0);
  }

  get total(): number {
    return this.subtotal;
  }

  confirmarVenta() {
    this.cargando = true;
    this.error = '';

    const request = {
      usuarioId: this.usuarioId,
      detalles: this.productos.map(p => ({
        productoId: p.id,
        loteId: p.loteId,
        cantidad: p.cantidad,
        precioUnitario: p.precioVenta
      })),
      pagos: [{ tipo: this.metodoPago, monto: this.total }],
      descuento: 0.0
    };

    console.log('REQUEST ENVIADO:', JSON.stringify(request));

    this.ventaService.registrarVenta(request).subscribe({
      next: (venta) => {
        sessionStorage.removeItem('productosVenta');
        this.router.navigate(['/ventas', venta.id]); // ← fix aquí
      },
      error: (err) => {
        this.error = err.error?.message || 'Error al registrar la venta';
        this.cargando = false;
      }
    });
  }

  cancelar() {
    this.router.navigate(['/ventas/buscar']);
  }
}