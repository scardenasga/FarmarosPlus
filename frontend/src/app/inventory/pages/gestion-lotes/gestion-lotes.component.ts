import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { InventoryService } from '../../services/inventory.service';
import { Product } from '../../models/product.model';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

interface RowGestionLote {
  idTemp: number;
  busqueda: string;
  producto: Product | null;
  cantidad: number | null;
  numeroLote: string;
  fechaVencimiento: string;
  nuevoCosto: number | null;
  nuevoPrecioVenta: number | null;
  buscando: boolean;
  resultados: Product[];
}

interface DropdownPos {
  top: number;
  left: number;
  width: number;
}

@Component({
  selector: 'app-gestion-lotes',
  standalone: true,
  imports: [CommonModule, FormsModule, TopBarComponent],
  templateUrl: './gestion-lotes.component.html',
  styleUrl: './gestion-lotes.component.css'
})
export class GestionLotesComponent implements OnInit {
  private inventoryService = inject(InventoryService);
  private router = inject(Router);

  rows = signal<RowGestionLote[]>([]);
  cargando = signal<boolean>(false);

  /** idTemp de las filas que quedaron incompletas en el último intento de guardado. */
  filasInvalidas = signal<Set<number>>(new Set());

  /** Índice de la fila cuyo dropdown de búsqueda está abierto (null = ninguno). */
  activeRowIndex = signal<number | null>(null);

  /** Posición en pantalla (fixed) donde debe pintarse el dropdown activo. */
  dropdownPos = signal<DropdownPos | null>(null);

  private searchSubject = new Subject<{index: number, term: string}>();

  ngOnInit() {
    this.addRow();

    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(({index, term}) => {
      this.performSearch(index, term);
    });
  }

  addRow() {
    const newRow: RowGestionLote = {
      idTemp: Date.now(),
      busqueda: '',
      producto: null,
      cantidad: null,
      numeroLote: '',
      fechaVencimiento: '',
      nuevoCosto: null,
      nuevoPrecioVenta: null,
      buscando: false,
      resultados: []
    };
    this.rows.update(r => [...r, newRow]);
  }

  removeRow(index: number) {
    this.rows.update(r => r.filter((_, i) => i !== index));
    if (this.rows().length === 0) this.addRow();
  }

  /** Marca una fila como válida de nuevo en cuanto el usuario la edita. */
  private limpiarInvalidez(idTemp: number) {
    if (!this.filasInvalidas().has(idTemp)) return;
    this.filasInvalidas.update(set => {
      const copia = new Set(set);
      copia.delete(idTemp);
      return copia;
    });
  }

  onCampoChange(index: number) {
    this.limpiarInvalidez(this.rows()[index].idTemp);
  }

  /** Calcula dónde debe aparecer el dropdown fijo, a partir del input real en pantalla. */
  private actualizarPosicionDropdown(inputEl: HTMLElement) {
    const rect = inputEl.getBoundingClientRect();
    this.dropdownPos.set({
      top: rect.bottom + 4,
      left: rect.left,
      width: rect.width
    });
  }

  onInputFocus(index: number, event: any) {
    this.activeRowIndex.set(index);
    this.actualizarPosicionDropdown(event.target);
  }

  onInputBlur(index: number) {
    // Delay para permitir que el click sobre un resultado se registre antes de cerrar.
    setTimeout(() => {
      if (this.activeRowIndex() === index) {
        this.activeRowIndex.set(null);
      }
    }, 200);
  }

  onSearchChange(index: number, event: any) {
    const term = event.target.value;
    this.activeRowIndex.set(index);
    this.actualizarPosicionDropdown(event.target);

    if (term.length < 3) {
      this.rows.update(r => {
        r[index].resultados = [];
        return [...r];
      });
      return;
    }
    this.searchSubject.next({index, term});
  }

  performSearch(index: number, term: string) {
    this.rows.update(r => {
      r[index].buscando = true;
      return [...r];
    });

    this.inventoryService.searchProducts(term).subscribe({
      next: (prods) => {
        this.rows.update(r => {
          r[index].resultados = prods;
          r[index].buscando = false;
          return [...r];
        });
      },
      error: () => {
        this.rows.update(r => {
          r[index].buscando = false;
          return [...r];
        });
      }
    });
  }

  selectProduct(index: number, product: Product) {
    this.rows.update(r => {
      r[index].producto = product;
      r[index].busqueda = product.nombre;
      r[index].resultados = [];
      r[index].nuevoCosto = product.costo || null;
      r[index].nuevoPrecioVenta = product.precioVenta || null;
      return [...r];
    });
    this.activeRowIndex.set(null);
    this.limpiarInvalidez(this.rows()[index].idTemp);
  }

  /** Determina qué campos obligatorios faltan en una fila. Vacío si está completa. */
  private camposFaltantes(r: RowGestionLote): string[] {
    const faltantes: string[] = [];
    if (!r.producto) faltantes.push('producto');
    if (!r.cantidad) faltantes.push('cantidad');
    if (!r.numeroLote) faltantes.push('número de lote');
    if (!r.fechaVencimiento) faltantes.push('fecha de vencimiento');
    return faltantes;
  }

  guardarTodo() {
    const filas = this.rows();
    const validas: RowGestionLote[] = [];
    const invalidas: { numeroFila: number; motivo: string }[] = [];
    const idsInvalidos = new Set<number>();

    filas.forEach((r, i) => {
      const faltantes = this.camposFaltantes(r);
      if (faltantes.length === 0) {
        validas.push(r);
      } else {
        invalidas.push({ numeroFila: i + 1, motivo: faltantes.join(', ') });
        idsInvalidos.add(r.idTemp);
      }
    });

    this.filasInvalidas.set(idsInvalidos);

    if (validas.length === 0) {
      const detalle = invalidas
        .map(inv => `- Fila ${inv.numeroFila}: falta ${inv.motivo}`)
        .join('\n');
      alert(`No se guardó ninguna fila. Complete los siguientes campos:\n\n${detalle}`);
      return;
    }

    if (invalidas.length > 0) {
      const detalle = invalidas
        .map(inv => `- Fila ${inv.numeroFila}: falta ${inv.motivo}`)
        .join('\n');
      const continuar = confirm(
        `Las siguientes filas no se guardarán por datos incompletos:\n\n${detalle}\n\n¿Desea continuar guardando solo las filas completas?`
      );
      if (!continuar) return;
    }

    const items = validas.map(r => ({
      identificador: r.producto!.codigoBarras,
      cantidad: r.cantidad,
      numeroLote: r.numeroLote,
      fechaVencimiento: r.fechaVencimiento,
      nuevoCosto: r.nuevoCosto,
      nuevoPrecioVenta: r.nuevoPrecioVenta
    }));

    this.cargando.set(true);
    this.inventoryService.registrarGestionLotes(items).subscribe({
      next: () => {
        this.router.navigate(['/inventario']);
      },
      error: (err) => {
        alert('Error al registrar gestión de lotes: ' + (err.error?.message || 'Error desconocido'));
        this.cargando.set(false);
      }
    });
  }

  onBack() {
    this.router.navigate(['/inventario']);
  }
}