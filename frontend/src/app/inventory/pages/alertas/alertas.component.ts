import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AlertaService, AlertaResponse } from '../../../services/alerta.service';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

@Component({
  selector: 'app-alertas',
  standalone: true,
  imports: [CommonModule, TopBarComponent],
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
      next: () => this.cambiarFiltro(this.soloNoLeidas),
      error: () => this.cambiarFiltro(this.soloNoLeidas)
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
      next: () => {
        this.cambiarFiltro(this.soloNoLeidas);
        this.alertaService.actualizarContador();
      }
    });
  }

  marcarTodas(): void {
    this.marcandoTodas = true;
    this.alertaService.marcarTodasLeidas().subscribe({
      next: () => {
        this.marcandoTodas = false;
        this.cambiarFiltro(this.soloNoLeidas);
        this.alertaService.actualizarContador();
      },
      error: () => (this.marcandoTodas = false)
    });
  }

  volver(): void {
    this.router.navigate(['/inventario']);
  }

  irCategorias(): void {
    this.router.navigate(['/inventario/categorias']);
  }

  noLeidasCount(): number {
    return this.alertas.filter(a => !a.leida).length;
  }
}
