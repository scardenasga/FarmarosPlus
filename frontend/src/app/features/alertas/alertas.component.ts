import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertaResponse, AlertaService } from '../../core/services/alerta.service';

@Component({
  selector: 'app-alertas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alertas.component.html'
})
export class AlertasComponent implements OnInit {
  alertas: AlertaResponse[] = [];
  soloNoLeidas = false;
  cargando = false;
  error = '';

  constructor(private alertaService: AlertaService) {}

  ngOnInit(): void {
    this.generarYCargar();
  }

  generarYCargar(): void {
    this.cargando = true;
    this.error = '';
    this.alertaService.generarAlertas().subscribe({
      next: alertas => {
        this.cargando = false;
        this.alertas = this.soloNoLeidas ? alertas.filter(a => !a.leida) : alertas;
      },
      error: () => {
        this.cargando = false;
        this.error = 'No se pudo conectar con el servidor.';
      }
    });
  }

  toggleFiltro(): void {
    this.soloNoLeidas = !this.soloNoLeidas;
    this.cargando = true;
    this.alertaService.listarAlertas(this.soloNoLeidas).subscribe({
      next: alertas => {
        this.cargando = false;
        this.alertas = alertas;
      },
      error: () => {
        this.cargando = false;
        this.error = 'Error al cargar las alertas.';
      }
    });
  }

  marcarLeida(id: number): void {
    this.alertaService.marcarLeida(id).subscribe({
      next: () => {
        const alerta = this.alertas.find(a => a.idAlerta === id);
        if (alerta) alerta.leida = true;
        if (this.soloNoLeidas) this.alertas = this.alertas.filter(a => a.idAlerta !== id);
      }
    });
  }

  marcarTodasLeidas(): void {
    this.alertaService.marcarTodasLeidas().subscribe({
      next: () => {
        this.alertas.forEach(a => (a.leida = true));
        if (this.soloNoLeidas) this.alertas = [];
      }
    });
  }

  get noLeidasCount(): number {
    return this.alertas.filter(a => !a.leida).length;
  }
}
