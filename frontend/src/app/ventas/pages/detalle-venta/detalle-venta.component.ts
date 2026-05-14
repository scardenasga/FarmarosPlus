import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { VentaService } from '../../services/venta.service';

@Component({
  selector: 'app-detalle-venta',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './detalle-venta.component.html',
  styleUrls: ['./detalle-venta.component.css']
})
export class DetalleVentaComponent implements OnInit {

  venta: any = null;
  cargando = true;
  error = '';
  mostrarModal = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ventaService: VentaService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.ventaService.obtenerVenta(+id).subscribe({
        next: (data) => {
          this.venta = data;
          this.cargando = false;
        },
        error: (err) => {
          console.error("Error cargando venta:", err);
          this.error = 'No se encontró la información de la venta';
          this.cargando = false;
        }
      });
    }
  }

  abrirModal() {
    this.mostrarModal = true;
  }
  confirmarVenta() {
  this.router.navigate(['/ventas']);
}

  cerrarModal() {
    this.mostrarModal = false;
  }

  irAAnular() {
    const ventaId = this.venta?.idVenta || this.venta?.id;

    if (!ventaId) {
      alert("Error: No se puede identificar la venta.");
      return;
    }

    this.cerrarModal();
    this.router.navigate(['/ventas' , ventaId, 'anular']);
  }

  descargarFactura() {
    const ventaId = this.venta?.idVenta || this.venta?.id;
    this.ventaService.descargarFactura(ventaId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `factura-${ventaId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('No se pudo generar la factura.')
    });
  }

  volver() {
    this.router.navigate(['/ventas']);
  }

  get esAdmin(): boolean {
    return true;
  }
}
