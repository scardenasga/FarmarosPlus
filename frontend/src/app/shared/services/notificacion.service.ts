import { Injectable, signal } from '@angular/core';

export type NotificacionTipo = 'exito' | 'error' | 'info' | 'advertencia';

export interface Notificacion {
  id: number;
  tipo: NotificacionTipo;
  mensaje: string;
}

/**
 * Servicio central de notificaciones emergentes (toasts).
 * Úsalo desde cualquier componente para informar errores,
 * confirmaciones o avisos sin acoplar la vista.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionService {

  private readonly _lista = signal<Notificacion[]>([]);
  /** Notificaciones activas en pantalla (solo lectura). */
  readonly lista = this._lista.asReadonly();

  private secuencia = 0;

  exito(mensaje: string): void {
    this.mostrar('exito', mensaje);
  }

  error(mensaje: string): void {
    // Los errores permanecen más tiempo en pantalla.
    this.mostrar('error', mensaje, 6000);
  }

  info(mensaje: string): void {
    this.mostrar('info', mensaje);
  }

  advertencia(mensaje: string): void {
    this.mostrar('advertencia', mensaje, 5000);
  }

  cerrar(id: number): void {
    this._lista.update(lista => lista.filter(n => n.id !== id));
  }

  private mostrar(tipo: NotificacionTipo, mensaje: string, duracionMs = 4000): void {
    const notificacion: Notificacion = { id: ++this.secuencia, tipo, mensaje };
    this._lista.update(lista => [...lista, notificacion]);
    setTimeout(() => this.cerrar(notificacion.id), duracionMs);
  }
}
