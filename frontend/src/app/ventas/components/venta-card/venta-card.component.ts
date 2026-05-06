import { Component, input, output } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { EstadoVentaComponent } from '../../../shared/components/estado-venta/estado-venta.component';

@Component({
  selector: 'app-venta-card',
  standalone: true,
  imports: [CommonModule, EstadoVentaComponent, DatePipe, DecimalPipe],
  templateUrl: './venta-card.component.html',
  styleUrl: './venta-card.component.css'
})
export class VentaCardComponent {
  venta = input.required<any>();
  selected = output<number>();

  onSelect() {
    this.selected.emit(this.venta().idVenta);
  }
}
