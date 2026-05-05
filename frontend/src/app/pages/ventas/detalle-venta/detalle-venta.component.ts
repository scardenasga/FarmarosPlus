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

  cerrarModal() {
    this.mostrarModal = false;
  }

  irAAnular() {
    // Usamos el ID de la venta obtenido de los datos cargados
    const ventaId = this.venta?.idVenta || this.venta?.id;
    
    if (!ventaId) {
      alert("Error: No se puede identificar la venta.");
      return;
    }

    this.cerrarModal();
    // Navegación corregida para evitar el 404
    // Asegúrate de que en tu app-routing esta sea la ruta correcta
    this.router.navigate(['/ventas/anular', ventaId]);
  }

  volver() {
    // Te regresa al historial/principal
    this.router.navigate(['/ventas/historial']);
  }

  get esAdmin(): boolean {
    // Cambiar por tu lógica de permisos real si es necesario
    return true; 
  }
}