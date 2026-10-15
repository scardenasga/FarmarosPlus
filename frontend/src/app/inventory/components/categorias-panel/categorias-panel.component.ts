import { Component, HostListener, OnChanges, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { NotificacionService } from '../../../shared/services/notificacion.service';
import { CategoriaService } from '../../../services/categoria.service';
import { Categoria } from '../../models/product.model';

@Component({
  selector: 'app-categorias-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormInputComponent, ConfirmationDialogComponent],
  templateUrl: './categorias-panel.component.html',
  styleUrl: './categorias-panel.component.css'
})
export class CategoriasPanelComponent implements OnChanges {
  private categoriaService = inject(CategoriaService);
  private notificacion = inject(NotificacionService);
  private fb = inject(FormBuilder);

  abierto = input<boolean>(false);

  cerrado = output<void>();
  /** Se emite tras crear/editar/eliminar para que el inventario refresque sus chips. */
  cambios = output<void>();

  categorias = signal<Categoria[]>([]);
  cargando = signal<boolean>(true);

  modoEdicion = signal<boolean>(false);
  categoriaEnEdicion = signal<Categoria | null>(null);

  categoriaForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    descripcion: ['']
  });

  guardando = signal<boolean>(false);

  confirmandoEliminar = signal<Categoria | null>(null);
  eliminando = signal<boolean>(false);
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['abierto'] && this.abierto()) {
      this.cancelarEdicion();
      this.cargar();
    }
  }

  /** Cierra con la tecla Esc. */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.abierto()) this.cerrado.emit();
  }

  cargar(notificarPadre: boolean = false): void {
    this.cargando.set(true);
    this.categoriaService.listar().subscribe({
      next: data => {
        this.categorias.set(data ?? []);
        this.cargando.set(false);
        if (notificarPadre) this.cambios.emit();
      },
      error: () => {
        this.notificacion.error('No se pudieron cargar las categorias.');
        this.cargando.set(false);
      }
    });
  }

  getControl(name: string): FormControl {
    return this.categoriaForm.get(name) as FormControl;
  }
  cancelarEdicion(): void {
    this.modoEdicion.set(false);
    this.categoriaEnEdicion.set(null);
    this.categoriaForm.reset({ nombre: '', descripcion: '' });
  }

  abrirEditar(cat: Categoria): void {
    this.modoEdicion.set(true);
    this.categoriaEnEdicion.set(cat);
    this.categoriaForm.patchValue({
      nombre: cat.nombre,
      descripcion: cat.descripcion ?? ''
    });
  }

  guardar(): void {
    if (this.categoriaForm.invalid) {
      this.categoriaForm.markAllAsTouched();
      return;
    }

    const valores = this.categoriaForm.value;
    const nombre = String(valores.nombre ?? '').trim();
    const descripcion = String(valores.descripcion ?? '').trim();
    const enEdicion = this.categoriaEnEdicion();
    this.guardando.set(true);

    const accion = this.modoEdicion() && enEdicion
      ? this.categoriaService.actualizar(enEdicion.id, nombre, descripcion)
      : this.categoriaService.crear(nombre, descripcion);

    accion.subscribe({
      next: () => {
        this.guardando.set(false);
        const msg = this.modoEdicion()
          ? 'Categoria "' + nombre + '" actualizada'
          : 'Categoria "' + nombre + '" creada';
        this.notificacion.exito(msg);
        this.cancelarEdicion();
        this.cargar(true);
      },
      error: (err: any) => {
        this.guardando.set(false);
        this.notificacion.error(err?.error?.message ?? 'Error al guardar la categoria.');
      }
    });
  }

  pedirConfirmacionEliminar(cat: Categoria): void {
    this.confirmandoEliminar.set(cat);
  }

  cancelarEliminar(): void {
    if (!this.eliminando()) this.confirmandoEliminar.set(null);
  }

  confirmarEliminar(): void {
    const cat = this.confirmandoEliminar();
    if (!cat || this.eliminando()) return;

    this.eliminando.set(true);
    this.categoriaService.eliminar(cat.id).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.confirmandoEliminar.set(null);
        this.notificacion.exito('Categoria "' + cat.nombre + '" eliminada');
        this.cargar(true);
      },
      error: (err: any) => {
        this.eliminando.set(false);
        this.confirmandoEliminar.set(null);
        this.notificacion.error(err?.error?.message ?? 'No se puede eliminar esta categoria.');
      }
    });
  }
}
