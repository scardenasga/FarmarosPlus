import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { VentaService } from '../../../services/venta.service';

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
        error: () => {
          this.error = 'No se encontró la venta';
          this.cargando = false;
        }
      });
    }
  }

  abrirModal() {
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
  }

  irAAnular() {
    const ventaId = this.venta?.idVenta ?? this.venta?.id;
    this.cerrarModal();
    this.router.navigate(['/ventas', ventaId, 'anular']);
  }

  
  volver() {
   
    this.router.navigate(['/ventas/historial']);
  }

  get esAdmin(): boolean {
    return true;
  }
}