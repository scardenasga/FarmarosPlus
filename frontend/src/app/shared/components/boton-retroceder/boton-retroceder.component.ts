import { Component, Input } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { Router, RouterModule } from '@angular/router'; 

@Component({
  selector: 'app-boton-retroceder',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './boton-retroceder.component.html',
  styleUrls: ['./boton-retroceder.component.css']
})
export class BotonRetrocederComponent {
  @Input() titulo: string = '';    
  @Input() subtitulo: string = ''; 

  constructor(private location: Location, private router: Router) {}

 haciaAtras(): void {
    // Intento 1: Historial del navegador
    if (window.history.length > 1) {
      this.location.back();
    } else {
      // Intento 2: Si el historial falla, lo obligamos a ir a la ruta principal
      this.router.navigate(['/ventas/historial']);
    }
  }
}