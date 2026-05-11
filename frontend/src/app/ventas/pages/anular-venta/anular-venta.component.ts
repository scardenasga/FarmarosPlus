import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { VentaService } from '../../services/venta.service';

@Component({
  selector: 'app-anular-venta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './anular-venta.component.html',
  styleUrl: './anular-venta.component.css'
})
export class AnularVentaComponent implements OnInit {

  venta: any = null;
  motivoAnulacion: string = '';
  agregarMotivo: boolean = false;
  cargando: boolean = false;
  cargandoVenta: boolean = true;
  error: string = '';
  usuarioId: number = 1;
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
          this.cargandoVenta = false;
        },
        error: () => {
          this.error = 'No se encontró la venta';
          this.cargandoVenta = false;
        }
      });
    }
  }

  confirmarAnulacion() {
    if (this.agregarMotivo && !this.motivoAnulacion.trim()) {
      this.error = 'El motivo de anulación es obligatorio';
      return;
    }

    this.cargando = true;
    this.error = '';

    const request = {
      confirmacion: true,
      usuarioId: this.usuarioId,
      usuarioResponsable: 'Tatiana',
      motivoAnulacion: this.motivoAnulacion || 'Anulación sin motivo especificado'
    };

    this.ventaService.anularVenta(this.venta.id, request).subscribe({
      next: () => {

        this.router.navigate(['/ventas']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Error al anular la venta';
        this.cargando = false;
      }
    });
  }

  cancelar() {
    if (this.venta?.idVenta) {
      this.router.navigate(['/ventas', this.venta.id]);
    } else {
      this.router.navigate(['/ventas']);
    }
  }
}
