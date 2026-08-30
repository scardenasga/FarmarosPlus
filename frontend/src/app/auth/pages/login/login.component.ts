import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavigationService } from '../../../shared/services/navigation.service';

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
      this.errorMsg.set('Usuario y contraseña son obligatorios');
      return;
    }
    this.errorMsg.set(null);
    this.cargando.set(true);
    this.auth.login(u, p).subscribe({
      next: () => {
        this.nav.showNav();
        this.cargando.set(false);
        this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        this.cargando.set(false);
        const status = err?.status;
        const bodyMsg: string | undefined = err?.error?.message;
        if (status === 401) {
          this.errorMsg.set(bodyMsg || 'Credenciales inválidas');
        } else if (status === 403) {
          this.errorMsg.set(bodyMsg || 'Usuario inactivo. Contacte al administrador');
        } else if (status === 400) {
          this.errorMsg.set(bodyMsg || 'Datos inválidos');
        } else {
          this.errorMsg.set(bodyMsg || 'No se pudo iniciar sesión. Intente de nuevo');
        }
      }
    });
  }
}
