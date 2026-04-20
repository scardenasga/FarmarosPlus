import { Routes } from '@angular/router';
import { ShellComponent } from './shared/layout/shell/shell.component';
import { AlertasComponent } from './features/alertas/alertas.component';
import { ConfiguracionAlertaComponent } from './features/configuracion-alerta/configuracion-alerta.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', redirectTo: 'alertas', pathMatch: 'full' },
      { path: 'alertas', component: AlertasComponent },
      { path: 'configuracion', component: ConfiguracionAlertaComponent }
    ]
  }
];
