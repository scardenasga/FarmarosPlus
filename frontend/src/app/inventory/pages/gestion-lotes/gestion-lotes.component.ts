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

  onSearchChange(index: number, event: any) {
    const term = event.target.value;
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
  }

  guardarTodo() {
    const items = this.rows()
      .filter(r => r.producto && r.cantidad && r.numeroLote && r.fechaVencimiento)
      .map(r => ({
        identificador: r.producto!.codigoBarras,
        cantidad: r.cantidad,
        numeroLote: r.numeroLote,
        fechaVencimiento: r.fechaVencimiento,
        nuevoCosto: r.nuevoCosto,
        nuevoPrecioVenta: r.nuevoPrecioVenta
      }));

    if (items.length === 0) {
      alert('Por favor complete al menos una fila correctamente');
      return;
    }

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
