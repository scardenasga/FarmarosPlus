import { Component, input, output, signal, computed, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { RecomendacionService } from '../../services/recomendacion.service';
import { RecomendacionItem, RecomendacionResponse } from '../../models/recomendacion.model';

@Component({
  selector: 'app-sugerencias-pos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sugerencias-pos.component.html',
  styleUrl: './sugerencias-pos.component.css'
})
export class SugerenciasPosComponent {
  /** IDs de productos actualmente en el carrito (orden no importa, se deduplica). */
  productoIds = input.required<number[]>();
  /** Máximo de chips a mostrar (1..5). */
  limit = input<number>(5);

  /** Emite el item seleccionado para que el padre ejecute PosVentaComponent.agregar(producto). */
  agregar = output<RecomendacionItem>();

  private recomendacionService = inject(RecomendacionService);

  cargando = signal(false);
  origen = signal<RecomendacionResponse['origen'] | null>(null);
  totalVentas = signal(0);
  recomendaciones = signal<RecomendacionItem[]>([]);
  actualizadoEn = signal<string | null>(null);

  /** Etiqueta discreta para datos iniciales (<30 ventas). */
  esDatosIniciales = computed(() => (this.totalVentas() > 0 && this.totalVentas() < 30));

  private sub?: Subscription;
  private debounceTimer: any = null;

  /** Identificador estable del carrito para detectar cambios reales. */
  private idsEstable = computed(() => {
    const ids = [...(this.productoIds() ?? [])].filter((v, i, a) => a.indexOf(v) === i).sort((a, b) => a - b);
    return ids.join(',');
  });

  constructor() {
    effect((onCleanup) => {
      const estable = this.idsEstable();
      const limite = this.limit();

      // Cancelar timer anterior
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer);
        this.debounceTimer = null;
      }
      // Cancelar request anterior
      if (this.sub) {
        this.sub.unsubscribe();
        this.sub = undefined;
      }

      if (!estable) {
        this.recomendaciones.set([]);
        this.origen.set(null);
        this.cargando.set(false);
        return;
      }

      this.cargando.set(true);

      this.debounceTimer = setTimeout(() => {
        const ids = estable.split(',').filter(Boolean).map(Number);
        this.sub = this.recomendacionService.obtenerRecomendaciones(ids, limite).subscribe({
          next: (resp) => {
            this.origen.set(resp.origen);
            this.totalVentas.set(resp.totalVentasAnalizadas ?? 0);
            this.actualizadoEn.set(resp.actualizadoEn ?? null);
            // Solo mostrar si hay recomendaciones; si origen SIN_DATOS o lista vacía -> panel oculto (silencioso)
            this.recomendaciones.set(resp.recomendaciones ?? []);
            this.cargando.set(false);
          },
          error: () => {
            // Error silencioso: no mostrar panel, no notificar
            this.recomendaciones.set([]);
            this.origen.set(null);
            this.cargando.set(false);
          }
        });
      }, 250);

      onCleanup(() => {
        if (this.debounceTimer) {
          clearTimeout(this.debounceTimer);
          this.debounceTimer = null;
        }
        if (this.sub) {
          this.sub.unsubscribe();
          this.sub = undefined;
        }
      });
    });
  }

  onAgregar(item: RecomendacionItem): void {
    this.agregar.emit(item);
  }
}
