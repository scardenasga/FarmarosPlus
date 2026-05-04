import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ProveedorService, DevolucionResponse } from '../../../services/proveedor.service';

@Component({
  selector: 'app-historial-devoluciones',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './historial-devoluciones.component.html',
  styleUrl: './historial-devoluciones.component.css'
})
export class HistorialDevolucionesComponent implements OnInit {

  devoluciones: DevolucionResponse[] = [];
  cargando = true;
  error = '';

  constructor(private proveedorService: ProveedorService, private router: Router) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.proveedorService.listarDevoluciones().subscribe({
      next: data => {
        this.devoluciones = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el historial.';
        this.cargando = false;
      }
    });
  }

  nueva(): void {
    this.router.navigate(['/entregas/nueva']);
  }

  totalProductos(dev: DevolucionResponse): number {
    return dev.detalles.reduce((sum, d) => sum + d.cantidad, 0);
  }
}
