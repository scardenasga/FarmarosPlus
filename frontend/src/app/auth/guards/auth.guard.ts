import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { SesionService } from '../../shared/services/sesion.service';

export const authGuard: CanActivateFn = () => {
  const sesion = inject(SesionService);
  const router = inject(Router);

  // Usa helper que verifica localStorage; evita considerar SISTEMA fallback como autenticado
  if (sesion.isAuthenticated()) {
    return true;
  }
  router.navigateByUrl('/login');
  return false;
};

export const loginGuard: CanActivateFn = () => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  if (sesion.isAuthenticated()) {
    router.navigateByUrl('/dashboard');
    return false;
  }
  return true;
};
