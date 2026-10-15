import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-input-fecha',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filtro-fecha.component.html',
  styleUrl: './filtro-fecha.component.css'
})
export class InputFechaComponent {
  // Recibe la fecha inicial (ej: '2024-12-12')
  @Input() fecha: string = '';
  
  // Emite la nueva fecha cuando el usuario la cambia
  @Output() fechaChange = new EventEmitter<string>();

  onDateChange(event: any) {
    const valor = event.target.value;
    this.fecha = valor;
    this.fechaChange.emit(valor);
  }
}