import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VentaCardComponent } from '../venta-card/venta-card.component';

@Component({
  selector: 'app-venta-list',
  standalone: true,
  imports: [CommonModule, VentaCardComponent],
  templateUrl: './venta-list.component.html',
  styleUrl: './venta-list.component.css'
})
export class VentaListComponent {
  ventasAgrupadas = input.required<{ fecha: string; ventas: any[] }[]>();
  ventaSelected = output<number>();

  onVentaSelect(id: number) {
    this.ventaSelected.emit(id);
  }
}
