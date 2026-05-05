import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProveedorService, ProveedorResponse } from '../../../services/proveedor.service';

interface ItemCompra {
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
}

@Component({
  selector: 'app-registrar-compra',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registrar-compra.component.html',
  styleUrl: './registrar-compra.component.css'
})
export class RegistrarCompraComponent implements OnInit {

  proveedores: ProveedorResponse[] = [];
  idProveedorSeleccionado: number | null = null;
  numeroFactura = '';
  notas = '';
  items: ItemCompra[] = [];

  // Modal
  modalVisible = false;
  termino = '';
  buscando = false;
  resultados: any[] = [];
  productoSeleccionado: any = null;
  cantidadModal = 1;
  precioModal = 0;
  errorModal = '';

  // Submit
  enviando = false;
  error = '';
  cargandoProveedores = true;

  constructor(private proveedorService: ProveedorService, private router: Router) {}

  ngOnInit(): void {
    this.proveedorService.listarProveedores().subscribe({
      next: data => {
        this.proveedores = data;
        this.cargandoProveedores = false;
      },
      error: () => (this.cargandoProveedores = false)
    });
  }

  volver(): void {
    this.router.navigate(['/entregas/compras']);
  }

  abrirModal(): void {
    this.termino = '';
    this.resultados = [];
    this.productoSeleccionado = null;
    this.cantidadModal = 1;
    this.precioModal = 0;
    this.errorModal = '';
    this.modalVisible = true;
  }

  cerrarModal(): void {
    this.modalVisible = false;
  }

  buscar(): void {
    if (!this.termino.trim()) return;
    this.buscando = true;
    this.proveedorService.buscarProductos(this.termino).subscribe({
      next: data => {
        this.resultados = data;
        this.buscando = false;
      },
      error: () => (this.buscando = false)
    });
  }

  seleccionarProducto(prod: any): void {
    this.productoSeleccionado = prod;
    this.cantidadModal = 1;
    this.precioModal = prod.costo ?? 0;
    this.errorModal = '';
  }

  agregarItem(): void {
    if (!this.productoSeleccionado) { this.errorModal = 'Seleccioná un producto.'; return; }
    if (this.cantidadModal < 1) { this.errorModal = 'La cantidad debe ser al menos 1.'; return; }
    if (this.precioModal < 0) { this.errorModal = 'El precio no puede ser negativo.'; return; }

    const existente = this.items.find(i => i.idProducto === this.productoSeleccionado.id);
    if (existente) {
      existente.cantidad += this.cantidadModal;
    } else {
      this.items.push({
        idProducto: this.productoSeleccionado.id,
        nombreProducto: this.productoSeleccionado.nombre,
        cantidad: this.cantidadModal,
        precioUnitario: this.precioModal
      });
    }
    this.cerrarModal();
  }

  quitarItem(index: number): void {
    this.items.splice(index, 1);
  }

  totalCompra(): number {
    return this.items.reduce((s, i) => s + i.cantidad * i.precioUnitario, 0);
  }

  puedeEnviar(): boolean {
    return this.idProveedorSeleccionado !== null && this.items.length > 0 && !this.enviando;
  }

  enviar(): void {
    if (!this.puedeEnviar()) return;
    this.enviando = true;
    this.error = '';

    this.proveedorService.registrarCompra({
      idProveedor: this.idProveedorSeleccionado!,
      usuarioResponsable: 'admin',
      numeroFactura: this.numeroFactura || undefined,
      notas: this.notas || undefined,
      detalles: this.items.map(i => ({
        idProducto: i.idProducto,
        cantidad: i.cantidad,
        precioUnitario: i.precioUnitario
      }))
    }).subscribe({
      next: () => this.router.navigate(['/entregas/compras']),
      error: (err) => {
        this.error = err?.error?.message ?? 'Error al registrar la compra.';
        this.enviando = false;
      }
    });
  }
}
