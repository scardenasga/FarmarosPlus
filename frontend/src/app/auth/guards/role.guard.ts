import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { SesionService } from '../../shared/services/sesion.service';
import { NotificacionService } from '../../shared/services/notificacion.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  const noti = inject(NotificacionService);

  const rolesPermitidos = (route.data['roles'] as string[] | undefined)?.map(r => r.toUpperCase());
  if (!rolesPermitidos || rolesPermitidos.length === 0) return true;

  const rol = sesion.rolEfectivo();
  if (rolesPermitidos.includes(rol)) return true;

  noti.error('No tienes permisos para acceder a esta sección');
  router.navigateByUrl('/ventas');
  return false;
};
