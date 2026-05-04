import { Routes } from '@angular/router';
import { BuscarProductoComponent } from './pages/ventas/buscar-producto/buscar-producto.component';
import { RegistrarVentaComponent } from './pages/ventas/registrar-venta/registrar-venta.component';
import { DetalleVentaComponent } from './pages/ventas/detalle-venta/detalle-venta.component';
import { AnularVentaComponent } from './pages/ventas/anular-venta/anular-venta.component';
import { InventoryComponent} from './inventory/pages/inventory/inventory.component';
import {HealthComponent} from './health/health.component';
import { CrearProductoComponent } from './inventory/pages/crear-producto/crear-producto.component';
import { ProductDetailComponent } from './inventory/pages/product-detail/product-detail.component';
import { EditarProductoComponent } from './inventory/pages/editar-producto/editar-producto.component';

export const routes: Routes = [
  { path: '', redirectTo: 'health', pathMatch: 'full' },
  { path: 'ventas/buscar', component: BuscarProductoComponent },
  { path: 'ventas/registrar', component: RegistrarVentaComponent },
  { path: 'ventas/:id', component: DetalleVentaComponent },
  { path: 'ventas/:id/anular', component: AnularVentaComponent },
  { path: 'inventario', component: InventoryComponent },
  { path: 'inventario/crear', component: CrearProductoComponent },
  { path: 'inventario/:id', component: ProductDetailComponent },
  { path: 'inventario/editar/:id', component: EditarProductoComponent },
  {path: 'health', component: HealthComponent },
];
