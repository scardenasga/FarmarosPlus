import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProveedorService, ProveedorResponse, CompraResponse } from '../../../services/proveedor.service';

@Component({
  selector: 'app-historial-compras',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './historial-compras.component.html',
  styleUrl: './historial-compras.component.css'
})
export class HistorialComprasComponent implements OnInit {

  compras: CompraResponse[] = [];
  proveedores: ProveedorResponse[] = [];
  filtroIdProveedor: number | null = null;
  cargando = true;
  error = '';
  idExpandida: number | null = null;

  constructor(private proveedorService: ProveedorService, private router: Router) {}

  ngOnInit(): void {
    this.proveedorService.listarProveedores().subscribe({
      next: data => (this.proveedores = data),
      error: () => {}
    });
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.idExpandida = null;
    const filtro = this.filtroIdProveedor ?? undefined;
    this.proveedorService.listarCompras(filtro).subscribe({
      next: data => {
        this.compras = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el historial.';
        this.cargando = false;
      }
    });
  }

  aplicarFiltro(): void {
    this.cargar();
  }

  toggleDetalles(id: number): void {
    this.idExpandida = this.idExpandida === id ? null : id;
  }

  nueva(): void {
    this.router.navigate(['/entregas/compras/nueva']);
  }

  volver(): void {
    this.router.navigate(['/entregas']);
  }

  totalUnidades(compra: CompraResponse): number {
    return compra.detalles.reduce((s, d) => s + d.cantidad, 0);
  }
}
