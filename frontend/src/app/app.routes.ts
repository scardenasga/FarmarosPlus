import { Routes } from '@angular/router';

// --- TUS COMPONENTES DE VENTAS (Recuperados) ---
import { BuscarProductoComponent } from './pages/ventas/buscar-producto/buscar-producto.component';
import { RegistrarVentaComponent } from './pages/ventas/registrar-venta/registrar-venta.component';
import { DetalleVentaComponent } from './pages/ventas/detalle-venta/detalle-venta.component';
import { AnularVentaComponent } from './pages/ventas/anular-venta/anular-venta.component';
import { HistorialVentasComponent } from './pages/ventas/historial-ventas/historial-ventas.component';
import { FiltrarHistorialComponent } from './pages/ventas/filtrar-historial/filtrar-historial.component';
import { NotificacionPedidosComponent } from './pages/compras/notificacion-pedidos/notificacion-pedidos.component';
import { PrevisualizarOrdenComponent } from './pages/compras/previsualizar-orden/previsualizar-orden.component';
import { InventoryComponent} from './inventory/pages/inventory/inventory.component';
import {HealthComponent} from './health/health.component';
import { CrearProductoComponent } from './inventory/pages/crear-producto/crear-producto.component';
import { ProductDetailComponent } from './inventory/pages/product-detail/product-detail.component';
import { EditarProductoComponent } from './inventory/pages/editar-producto/editar-producto.component';
import { HistorialVentasComponent } from './pages/ventas/historial-ventas/historial-ventas.component';
import { FiltrarHistorialComponent } from './pages/ventas/filtrar-historial/filtrar-historial.component';
import { CategoriasComponent } from './pages/inventario/categorias/categorias.component';
import { AlertasComponent } from './pages/inventario/alertas/alertas.component';
import { HistorialDevolucionesComponent } from './pages/entregas/historial-devoluciones/historial-devoluciones.component';
import { RegistrarDevolucionComponent } from './pages/entregas/registrar-devolucion/registrar-devolucion.component';
import { HistorialComprasComponent } from './pages/entregas/historial-compras/historial-compras.component';
import { RegistrarCompraComponent } from './pages/entregas/registrar-compra/registrar-compra.component';

export const routes: Routes = [
  { path: '', redirectTo: 'health', pathMatch: 'full' },  
  { path: 'ventas/historial', component: HistorialVentasComponent },
  { path: 'ventas/buscar', component: BuscarProductoComponent },
  { path: 'ventas/registrar', component: RegistrarVentaComponent },
  { path: 'ventas/historial/filtrar', component: FiltrarHistorialComponent },
  { path: 'ventas/:id', component: DetalleVentaComponent },
  { path: 'ventas/:id/anular', component: AnularVentaComponent },
  { path: 'compras/notificaciones', component: NotificacionPedidosComponent },
  { path: 'compras/previsualizar-orden', component: PrevisualizarOrdenComponent },
  { path: 'alertas', component: AlertasComponent },
  { path: 'inventario/categorias', component: CategoriasComponent },
  { path: 'entregas', component: HistorialDevolucionesComponent },
  { path: 'entregas/nueva', component: RegistrarDevolucionComponent },
  { path: 'entregas/compras', component: HistorialComprasComponent },
  { path: 'entregas/compras/nueva', component: RegistrarCompraComponent }.
  { path: 'inventario', component: InventoryComponent },
  { path: 'inventario/crear', component: CrearProductoComponent },
  { path: 'inventario/:id', component: ProductDetailComponent },
  { path: 'inventario/editar/:id', component: EditarProductoComponent },
  { path: 'health', component: HealthComponent },
];
