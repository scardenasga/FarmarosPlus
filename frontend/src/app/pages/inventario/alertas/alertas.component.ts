import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AlertaService, AlertaResponse } from '../../../services/alerta.service';

@Component({
  selector: 'app-alertas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './alertas.component.html',
  styleUrl: './alertas.component.css'
})
export class AlertasComponent implements OnInit {

  alertas: AlertaResponse[] = [];
  cargando = true;
  error = '';
  soloNoLeidas = true;
  marcandoTodas = false;

  constructor(private alertaService: AlertaService, private router: Router) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.alertaService.generarAlertas().subscribe({
      next: data => {
        this.alertas = this.soloNoLeidas ? data : data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar las alertas.';
        this.cargando = false;
      }
    });
  }

  cambiarFiltro(soloNoLeidas: boolean): void {
    this.soloNoLeidas = soloNoLeidas;
    this.cargando = true;
    this.error = '';
    this.alertaService.listarAlertas(soloNoLeidas).subscribe({
      next: data => {
        this.alertas = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar las alertas.';
        this.cargando = false;
      }
    });
  }

  marcarLeida(id: number): void {
    this.alertaService.marcarLeida(id).subscribe({
      next: () => this.cambiarFiltro(this.soloNoLeidas)
    });
  }

  marcarTodas(): void {
    this.marcandoTodas = true;
    this.alertaService.marcarTodasLeidas().subscribe({
      next: () => {
        this.marcandoTodas = false;
        this.cambiarFiltro(this.soloNoLeidas);
      },
      error: () => (this.marcandoTodas = false)
    });
  }

  irCategorias(): void {
    this.router.navigate(['/inventario/categorias']);
  }

  noLeidasCount(): number {
    return this.alertas.filter(a => !a.leida).length;
  }
}
