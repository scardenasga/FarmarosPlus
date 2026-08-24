import { Component, OnInit, inject } from '@angular/core';
import { CommonModule} from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BotonRetrocederComponent } from '../../../shared/components/boton-retroceder/boton-retroceder.component';
import { SesionService } from '../../../shared/services/sesion.service';
import { VentaService } from '../../services/venta.service';

@Component({
  selector: 'app-registrar-venta',
  standalone: true,
  imports: [CommonModule, FormsModule, BotonRetrocederComponent],
  templateUrl: './registrar-venta.component.html',
  styleUrl: './registrar-venta.component.css'
})
export class RegistrarVentaComponent implements OnInit {

  productos: any[] = [];
  metodoPago: string = 'EFECTIVO';
  private sesion = inject(SesionService);
  cargando: boolean = false;
  error: string = '';
  hoy: Date = new Date();

  constructor(private ventaService: VentaService, private router: Router) {}

  ngOnInit() {
    const data = sessionStorage.getItem('productosVenta');
    if (data) {
      this.productos = JSON.parse(data);
    } else {
      this.router.navigate(['/ventas/crear']);
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

  get ivaTotal(): number {
    return this.productos.reduce((acc, p) => {
      const subtotalLinea = p.precioVenta * p.cantidad;
      return acc + (subtotalLinea * (p.porcentajeIva || 0) / 100);
    }, 0);
  }

  get total(): number {
    return this.subtotal + this.ivaTotal;
  }

 confirmarVenta() {
  this.cargando = true;
  this.error = '';

  const request = {
    usuarioId: this.sesion.idUsuario(),
    detalles: this.productos.map(p => ({
        productoId: p.id,
        loteId: p.loteId?? null,
        cantidad: p.cantidad,
        precioUnitario: p.precioVenta
      
    })),
    pagos: [{ tipo: this.metodoPago, monto: this.total }],
    descuento: 0.0
  };

  this.ventaService.registrarVenta(request).subscribe({
    next: (venta) => {
      sessionStorage.removeItem('productosVenta');
      this.router.navigate(['/ventas', venta.id]);
    },
    error: (err) => {
      console.error('ERROR DEL SERVIDOR:', err);
      this.error = err.error?.message || 'El servidor rechaza la venta sin lote';
      this.cargando = false;
    }
  });
}

  cancelar() {
    this.router.navigate(['/ventas/crear']);
  }
}
