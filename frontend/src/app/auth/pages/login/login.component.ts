import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavigationService } from '../../../shared/services/navigation.service';
import { NotificacionService } from '../../../shared/services/notificacion.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private nav = inject(NavigationService);
  private notificacion = inject(NotificacionService);

  username = '';
  password = '';
  mostrarPassword = false;
  cargando = signal(false);
  errorMsg = signal<string | null>(null);

  constructor() {
    this.nav.hideNav();
  }

  togglePassword(): void {
    this.mostrarPassword = !this.mostrarPassword;
  }

  onSubmit(): void {
    const u = this.username.trim();
    const p = this.password.trim();
    if (!u || !p) {
      const msg = 'Usuario y contraseña son obligatorios';
      this.errorMsg.set(msg);
      this.notificacion.error(msg);
      return;
    }
    this.errorMsg.set(null);
    this.cargando.set(true);
    this.auth.login(u, p).subscribe({
      next: () => {
        this.nav.showNav();
        this.cargando.set(false);
        this.notificacion.exito(`Bienvenido, ${u}`);
        this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        this.cargando.set(false);
        const status = err?.status;
        const bodyMsg: string | undefined = err?.error?.message;
        let msg = bodyMsg || 'No se pudo iniciar sesión. Intente de nuevo';
        if (status === 401) msg = bodyMsg || 'Credenciales inválidas';
        else if (status === 403) msg = bodyMsg || 'Usuario inactivo. Contacte al administrador';
        else if (status === 400) msg = bodyMsg || 'Datos inválidos';
        this.errorMsg.set(msg);
        this.notificacion.error(msg);
      }
    });
  }
}
