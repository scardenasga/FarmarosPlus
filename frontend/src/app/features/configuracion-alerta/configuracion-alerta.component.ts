import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlertaService } from '../../core/services/alerta.service';

@Component({
  selector: 'app-configuracion-alerta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './configuracion-alerta.component.html'
})
export class ConfiguracionAlertaComponent implements OnInit {
  dias = 30;
  guardado = false;
  error = '';
  cargando = false;

  constructor(private alertaService: AlertaService) {}

  ngOnInit(): void {
    this.alertaService.obtenerConfiguracion().subscribe({
      next: config => (this.dias = config.diasProximoVencimiento),
      error: () => (this.error = 'No se pudo cargar la configuración.')
    });
  }

  guardar(): void {
    if (this.dias < 1) return;
    this.cargando = true;
    this.guardado = false;
    this.error = '';
    this.alertaService.actualizarConfiguracion(this.dias).subscribe({
      next: () => {
        this.cargando = false;
        this.guardado = true;
        setTimeout(() => (this.guardado = false), 3000);
      },
      error: () => {
        this.cargando = false;
        this.error = 'Error al guardar la configuración.';
      }
    });
  }
}
