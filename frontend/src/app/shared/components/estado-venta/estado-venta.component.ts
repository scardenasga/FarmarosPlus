import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-estado-venta',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './estado-venta.component.html',
  styleUrl: './estado-venta.component.css'
})
export class EstadoVentaComponent {
  @Input() estado: string = '';

  get textoEstado(): string {
    if (!this.estado) return 'Desconocido';
    return this.estado.toLowerCase() === 'anulada' ? 'Anulada' : 'Completada';
  }
}