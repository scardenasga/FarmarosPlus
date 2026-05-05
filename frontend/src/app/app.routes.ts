import { Routes } from '@angular/router';

// --- TUS COMPONENTES DE VENTAS (Recuperados) ---
import { BuscarProductoComponent } from './pages/ventas/buscar-producto/buscar-producto.component';
import { RegistrarVentaComponent } from './pages/ventas/registrar-venta/registrar-venta.component';
import { DetalleVentaComponent } from './pages/ventas/detalle-venta/detalle-venta.component';
import { AnularVentaComponent } from './pages/ventas/anular-venta/anular-venta.component';
import { HistorialVentasComponent } from './pages/ventas/historial-ventas/historial-ventas.component';
import { FiltrarHistorialComponent } from './pages/ventas/filtrar-historial/filtrar-historial.component';

// --- LA VISTA DE TU COMPAÑERO (Inventario) ---
import { InventoryComponent } from './inventory/pages/inventory/inventory.component';
import { NotificacionPedidosComponent } from './pages/compras/notificacion-pedidos/notificacion-pedidos.component';
import { PrevisualizarOrdenComponent } from './pages/compras/previsualizar-orden/previsualizar-orden.component';

  // 3. Rutas de Ventas (Tu historial y filtros)

export const routes: Routes = [
  { path: '', redirectTo: 'ventas/historial', pathMatch: 'full' }, 
  
  { path: 'ventas/historial', component: HistorialVentasComponent },
  { path: 'ventas/buscar', component: BuscarProductoComponent },
  { path: 'ventas/registrar', component: RegistrarVentaComponent },
  { path: 'ventas/historial/filtrar', component: FiltrarHistorialComponent },
  { path: 'ventas/:id', component: DetalleVentaComponent },
  { path: 'ventas/:id/anular', component: AnularVentaComponent },
  { path: 'compras/notificaciones', component: NotificacionPedidosComponent },
  { path: 'compras/previsualizar-orden', component: PrevisualizarOrdenComponent },
  { path: 'inventario', component: InventoryComponent },
  { path: 'previsualizar-orden/:id', component: PrevisualizarOrdenComponent }
];