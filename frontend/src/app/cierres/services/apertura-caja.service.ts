import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AperturaCajaService {
  private keyFor(dateStr: string): string {
    return `farmaros_apertura_${dateStr}`;
  }

  private todayStr(): string {
    return new Date().toISOString().slice(0, 10);
  }

  getMontoHoy(): number | null {
    const v = localStorage.getItem(this.keyFor(this.todayStr()));
    if (v == null) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }

  guardarMontoHoy(monto: number): void {
    localStorage.setItem(this.keyFor(this.todayStr()), String(monto));
  }

  existeAperturaHoy(): boolean {
    return this.getMontoHoy() !== null;
  }

  limpiarHoy(): void {
    localStorage.removeItem(this.keyFor(this.todayStr()));
  }
}
