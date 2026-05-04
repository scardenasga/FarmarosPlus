import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-boton-filtro',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './boton-filtro.component.html',
  styleUrl: './boton-filtro.component.css'
})
export class BotonFiltroComponent {
  // Con esto avisamos al padre que queremos filtrar
  @Output() clicFiltro = new EventEmitter<void>();

  ejecutarAccion() {
    this.clicFiltro.emit();
  }
}