import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { inject } from '@angular/core';
import { PermisoService } from '../../shared/services/permiso.service';
import { NotificacionService } from '../../shared/services/notificacion.service';

export const permisoGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const permisos = inject(PermisoService);
  const router = inject(Router);
  const noti = inject(NotificacionService);

  const requeridos = route.data['permisos'] as string[] | string | undefined;
  const lista: string[] = !requeridos ? [] : Array.isArray(requeridos) ? requeridos : [requeridos];
  if (lista.length === 0) return true;

  const ok = lista.some(p => permisos.tiene(p));
  if (ok) return true;

  noti.error('No tienes permisos para esta sección');
  router.navigateByUrl('/dashboard');
  return false;
};
