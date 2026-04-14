import { Routes } from '@angular/router';
import { BuscarProductoComponent } from './pages/ventas/buscar-producto/buscar-producto.component';
import { RegistrarVentaComponent } from './pages/ventas/registrar-venta/registrar-venta.component';
import { DetalleVentaComponent } from './pages/ventas/detalle-venta/detalle-venta.component';
import { AnularVentaComponent } from './pages/ventas/anular-venta/anular-venta.component';

export const routes: Routes = [
  { path: '', redirectTo: 'ventas/buscar', pathMatch: 'full' },
  { path: 'ventas/buscar', component: BuscarProductoComponent },
  { path: 'ventas/registrar', component: RegistrarVentaComponent },
  { path: 'ventas/:id', component: DetalleVentaComponent },
  { path: 'ventas/:id/anular', component: AnularVentaComponent }
];