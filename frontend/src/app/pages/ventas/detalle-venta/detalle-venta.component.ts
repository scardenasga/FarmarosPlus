import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { VentaService } from '../../../services/venta.service';

@Component({
  selector: 'app-detalle-venta',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './detalle-venta.component.html',
  styleUrl: './detalle-venta.component.css'
})
export class DetalleVentaComponent implements OnInit {

  venta: any = null;
  cargando: boolean = true;
  error: string = '';

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

  irAAnular() {
    this.router.navigate(['/ventas', this.venta.id, 'anular']);
  }

  volver() {
    this.router.navigate(['/ventas/buscar']);
  }

  get esAdmin(): boolean {
    return true; // luego se conecta con el login real
  }
}