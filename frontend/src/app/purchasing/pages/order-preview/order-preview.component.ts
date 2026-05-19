import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PurchasingService } from '../../services/purchasing.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { BottomNavBarComponent } from '../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

@Component({
  selector: 'app-order-preview',
  standalone: true,
  imports: [CommonModule, FormsModule, SearchBarComponent, BottomNavBarComponent, TopBarComponent],
  templateUrl: './order-preview.component.html',
  styleUrl: './order-preview.component.css'
})
export class OrderPreviewComponent implements OnInit {
  private purchasingService = inject(PurchasingService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  productosOrden = signal<any[]>([]);
  searchTerm = signal<string>('');
  nombreProveedor = signal<string>('Cargando...');
  proveedorId = signal<number>(0);
  isSubmitting = signal<boolean>(false);

  productosFiltrados = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    if (!term) return this.productosOrden();
    
    return this.productosOrden().filter(p =>
      p.nombreProducto.toLowerCase().includes(term)
    );
  });

  total = computed(() => {
    return this.productosOrden().reduce((acc, item) => {
      return acc + (Number(item.cantidadSugerida || 0) * Number(item.costo || 0));
    }, 0);
  });

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.proveedorId.set(id);
      this.cargarDatos();
    } else {
      this.router.navigate(['/compras/notificaciones']);
    }
  }

  cargarDatos() {
    this.purchasingService.obtenerPrevisualizacion(this.proveedorId()).subscribe({
      next: (res) => {
        this.nombreProveedor.set(res.proveedorNombre);
        this.productosOrden.set((res.items || []).map((item: any) => ({
          nombreProducto: item.nombre,
          cantidadSugerida: item.cantidadSugerida || 0,
          costo: item.precioUnitario || 0,
          justificacion: item.motivo || 'Stock Bajo'
        })));
      }
    });
  }

  eliminarProducto(index: number) {
    this.productosOrden.update(prev => {
      const copy = [...prev];
      copy.splice(index, 1);
      return copy;
    });
  }

  onSearch(termino: string) {
    this.searchTerm.set(termino);
  }

  confirmarOrdenCompleta() {
    this.isSubmitting.set(true);
    this.purchasingService.confirmarPedidoFinal({
      proveedorId: this.proveedorId(),
      items: this.productosOrden(),
      totalFinal: this.total()
    }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/purchasing/purchase-history']);
      },
      error: () => this.isSubmitting.set(false)
    });
  }

  handleBack() {
    this.router.navigate(['/compras/notificaciones']);
  }
}
