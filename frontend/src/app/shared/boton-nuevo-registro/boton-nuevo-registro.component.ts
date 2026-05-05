import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-boton-nuevo-registro',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './boton-nuevo-registro.component.html',
  styleUrl: './boton-nuevo-registro.component.css'
})
export class BotonNuevoRegistroComponent {
  // Esta es la variable que recibirá la ruta desde el historial
  @Input() rutaDestino: string = '';
}