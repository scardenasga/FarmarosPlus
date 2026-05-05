import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProveedorService, ProveedorResponse } from '../../../services/proveedor.service';

interface ItemDevolucion {
  idProducto: number;
  nombreProducto: string;
  idLote: number;
  numeroLote: string;
  cantidadDisponible: number;
  cantidad: number;
}

@Component({
  selector: 'app-registrar-devolucion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registrar-devolucion.component.html',
  styleUrl: './registrar-devolucion.component.css'
})
export class RegistrarDevolucionComponent implements OnInit {

  // Datos del formulario
  proveedores: ProveedorResponse[] = [];
  idProveedorSeleccionado: number | null = null;
  motivo = '';
  items: ItemDevolucion[] = [];

  // Modal agregar producto
  modalVisible = false;
  paso: 'buscar' | 'lote' = 'buscar';
  termino = '';
  buscando = false;
  resultados: any[] = [];
  productoSeleccionado: any = null;
  lotes: any[] = [];
  idLoteSeleccionado: number | null = null;
  cantidadModal = 1;
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
    this.router.navigate(['/entregas']);
  }

  // --- Modal de búsqueda de producto ---

  abrirModal(): void {
    this.paso = 'buscar';
    this.termino = '';
    this.resultados = [];
    this.productoSeleccionado = null;
    this.lotes = [];
    this.idLoteSeleccionado = null;
    this.cantidadModal = 1;
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
    this.paso = 'lote';
    this.errorModal = '';
    this.proveedorService.obtenerLotes(prod.id).subscribe({
      next: data => (this.lotes = data),
      error: () => (this.errorModal = 'No se pudieron cargar los lotes.')
    });
  }

  loteSeleccionado(): any {
    return this.lotes.find(l => l.id === this.idLoteSeleccionado);
  }

  agregarItem(): void {
    const lote = this.loteSeleccionado();
    if (!lote) { this.errorModal = 'Seleccioná un lote.'; return; }
    if (this.cantidadModal < 1) { this.errorModal = 'La cantidad debe ser al menos 1.'; return; }
    if (this.cantidadModal > lote.cantidad) {
      this.errorModal = `Stock disponible en este lote: ${lote.cantidad}.`;
      return;
    }

    const existente = this.items.find(i => i.idLote === lote.id);
    if (existente) {
      const total = existente.cantidad + this.cantidadModal;
      if (total > lote.cantidad) {
        this.errorModal = `No podés devolver más de ${lote.cantidad} unidades de este lote.`;
        return;
      }
      existente.cantidad = total;
    } else {
      this.items.push({
        idProducto: this.productoSeleccionado.id,
        nombreProducto: this.productoSeleccionado.nombre,
        idLote: lote.id,
        numeroLote: lote.numeroLote,
        cantidadDisponible: lote.cantidad,
        cantidad: this.cantidadModal
      });
    }

    this.cerrarModal();
  }

  quitarItem(index: number): void {
    this.items.splice(index, 1);
  }

  // --- Envío ---

  puedeEnviar(): boolean {
    return this.idProveedorSeleccionado !== null && this.items.length > 0 && !this.enviando;
  }

  enviar(): void {
    if (!this.puedeEnviar()) return;
    this.enviando = true;
    this.error = '';

    this.proveedorService.registrarDevolucion({
      idProveedor: this.idProveedorSeleccionado!,
      usuarioResponsable: 'admin',
      motivo: this.motivo,
      detalles: this.items.map(i => ({
        idProducto: i.idProducto,
        idLote: i.idLote,
        cantidad: i.cantidad
      }))
    }).subscribe({
      next: () => this.router.navigate(['/entregas']),
      error: (err) => {
        this.error = err?.error?.message ?? 'Error al registrar la devolución.';
        this.enviando = false;
      }
    });
  }
}
