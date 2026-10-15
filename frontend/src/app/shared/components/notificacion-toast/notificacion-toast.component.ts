import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificacionService, NotificacionTipo } from '../../../shared/services/notificacion.service';

@Component({
  selector: 'app-notificacion-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notificacion-toast.component.html',
  styleUrl: './notificacion-toast.component.css'
})
export class NotificacionToastComponent {
  private notificacionService = inject(NotificacionService);

  readonly notificaciones = this.notificacionService.lista;

  cerrar(id: number): void {
    this.notificacionService.cerrar(id);
  }

  esTipo(n: NotificacionTipo, tipo: NotificacionTipo): boolean {
    return n === tipo;
  }
}
