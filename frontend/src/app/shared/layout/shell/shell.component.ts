import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../navbar/navbar.component';
import { AlertaService } from '../../../core/services/alerta.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent],
  templateUrl: './shell.component.html'
})
export class ShellComponent implements OnInit {
  alertasNoLeidas = 0;

  constructor(private alertaService: AlertaService) {}

  ngOnInit(): void {
    this.alertaService.listarAlertas(true).subscribe({
      next: alertas => (this.alertasNoLeidas = alertas.length),
      error: () => (this.alertasNoLeidas = 0)
    });
  }
}
