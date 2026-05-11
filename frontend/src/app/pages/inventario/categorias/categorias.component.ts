import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CategoriaService, CategoriaResponse } from '../../../services/categoria.service';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.component.html',
  styleUrl: './categorias.component.css'
})
export class CategoriasComponent implements OnInit {

  categorias: CategoriaResponse[] = [];
  cargando = true;
  error = '';

  modalVisible = false;
  modoEdicion = false;
  categoriaEnEdicion: CategoriaResponse | null = null;
  formNombre = '';
  formDescripcion = '';
  formError = '';
  guardando = false;

  confirmandoEliminar: CategoriaResponse | null = null;
  eliminando = false;

  constructor(private categoriaService: CategoriaService, private router: Router) {}

  volver(): void {
    this.router.navigate(['/inventario']);
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.categoriaService.listar().subscribe({
      next: data => {
        this.categorias = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar las categorías.';
        this.cargando = false;
      }
    });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.categoriaEnEdicion = null;
    this.formNombre = '';
    this.formDescripcion = '';
    this.formError = '';
    this.modalVisible = true;
  }

  abrirEditar(cat: CategoriaResponse): void {
    this.modoEdicion = true;
    this.categoriaEnEdicion = cat;
    this.formNombre = cat.nombre;
    this.formDescripcion = cat.descripcion ?? '';
    this.formError = '';
    this.modalVisible = true;
  }

  cerrarModal(): void {
    this.modalVisible = false;
    this.guardando = false;
  }

  guardar(): void {
    if (!this.formNombre.trim()) {
      this.formError = 'El nombre es obligatorio.';
      return;
    }
    this.guardando = true;
    this.formError = '';

    const accion = this.modoEdicion && this.categoriaEnEdicion
      ? this.categoriaService.actualizar(this.categoriaEnEdicion.id, this.formNombre, this.formDescripcion)
      : this.categoriaService.crear(this.formNombre, this.formDescripcion);

    accion.subscribe({
      next: () => {
        this.cerrarModal();
        this.cargar();
      },
      error: (err) => {
        this.formError = err?.error?.message ?? 'Error al guardar la categoría.';
        this.guardando = false;
      }
    });
  }

  pedirConfirmacionEliminar(cat: CategoriaResponse): void {
    this.confirmandoEliminar = cat;
    this.error = '';
  }

  cancelarEliminar(): void {
    this.confirmandoEliminar = null;
  }

  confirmarEliminar(): void {
    if (!this.confirmandoEliminar) return;
    this.eliminando = true;
    this.categoriaService.eliminar(this.confirmandoEliminar.id).subscribe({
      next: () => {
        this.confirmandoEliminar = null;
        this.eliminando = false;
        this.cargar();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se puede eliminar esta categoría.';
        this.confirmandoEliminar = null;
        this.eliminando = false;
      }
    });
  }
}
