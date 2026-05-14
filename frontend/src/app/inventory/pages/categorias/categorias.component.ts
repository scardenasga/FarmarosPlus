import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CategoriaService } from '../../../services/categoria.service';
import { Categoria } from '../../../inventory/models/product.model';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NavigationService } from '../../../shared/services/navigation.service';
import { FabButtonComponent } from '../../../shared/components/fab-button/fab-button.component';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TopBarComponent, FormInputComponent, ConfirmationDialogComponent, FabButtonComponent],
  templateUrl: './categorias.component.html',
  styleUrl: './categorias.component.css'
})
export class CategoriasComponent implements OnInit, OnDestroy {
  private categoriaService = inject(CategoriaService);
  private router = inject(Router);
  private navService = inject(NavigationService);
  private fb = inject(FormBuilder);

  categorias = signal<Categoria[]>([]);
  cargando = signal<boolean>(true);
  
  modalVisible = signal<boolean>(false);
  modoEdicion = signal<boolean>(false);
  categoriaEnEdicion = signal<Categoria | null>(null);
  
  categoriaForm!: FormGroup;
  
  showErrorDialog = signal<boolean>(false);
  errorMessage = signal<string>('');

  confirmandoEliminar = signal<Categoria | null>(null);
  eliminando = signal<boolean>(false);

  ngOnInit(): void {
    this.navService.hideNav();
    this.initForm();
    this.cargar();
  }

  ngOnDestroy(): void {
    this.navService.showNav();
  }

  private initForm(): void {
    this.categoriaForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: ['']
    });
  }

  cargar(): void {
    this.cargando.set(true);
    this.categoriaService.listar().subscribe({
      next: data => {
        this.categorias.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.showError('No se pudo cargar las categorías.');
        this.cargando.set(false);
      }
    });
  }

  abrirCrear(): void {
    this.modoEdicion.set(false);
    this.categoriaEnEdicion.set(null);
    this.categoriaForm.reset();
    this.modalVisible.set(true);
  }

  abrirEditar(cat: Categoria): void {
    this.modoEdicion.set(true);
    this.categoriaEnEdicion.set(cat);
    this.categoriaForm.patchValue({
      nombre: cat.nombre,
      descripcion: cat.descripcion
    });
    this.modalVisible.set(true);
  }

  cerrarModal(): void {
    this.modalVisible.set(false);
  }

  guardar(): void {
    if (this.categoriaForm.invalid) {
      this.categoriaForm.markAllAsTouched();
      return;
    }

    const { nombre, descripcion } = this.categoriaForm.value;
    const catEnEdicion = this.categoriaEnEdicion();

    const accion = this.modoEdicion() && catEnEdicion
      ? this.categoriaService.actualizar(catEnEdicion.id, nombre, descripcion)
      : this.categoriaService.crear(nombre, descripcion);

    accion.subscribe({
      next: () => {
        this.cerrarModal();
        this.cargar();
      },
      error: (err) => {
        this.showError(err?.error?.message ?? 'Error al guardar la categoría.');
      }
    });
  }

  pedirConfirmacionEliminar(cat: Categoria): void {
    this.confirmandoEliminar.set(cat);
  }

  cancelarEliminar(): void {
    this.confirmandoEliminar.set(null);
  }

  confirmarEliminar(): void {
    const cat = this.confirmandoEliminar();
    if (!cat) return;

    this.eliminando.set(true);
    this.categoriaService.eliminar(cat.id).subscribe({
      next: () => {
        this.confirmandoEliminar.set(null);
        this.eliminando.set(false);
        this.cargar();
      },
      error: (err) => {
        this.confirmandoEliminar.set(null);
        this.eliminando.set(false);
        this.showError(err?.error?.message ?? 'No se puede eliminar esta categoría.');
      }
    });
  }

  showError(msg: string): void {
    this.errorMessage.set(msg);
    this.showErrorDialog.set(true);
  }

  closeError(): void {
    this.showErrorDialog.set(false);
  }

  getControl(name: string) {
    return this.categoriaForm.get(name) as any;
  }

  volver(): void {
    this.router.navigate(['/inventario']);
  }
}
