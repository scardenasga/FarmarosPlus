import { Routes } from '@angular/router';

// --- TUS COMPONENTES DE VENTAS (Recuperados) ---
import { InventoryComponent} from './inventory/pages/inventory/inventory.component';
import {HealthComponent} from './health/health.component';
import { CrearProductoComponent } from './inventory/pages/crear-producto/crear-producto.component';
import { ProductDetailComponent } from './inventory/pages/product-detail/product-detail.component';
import { EditarProductoComponent } from './inventory/pages/editar-producto/editar-producto.component';
import { CategoriasComponent } from './inventory/pages/categorias/categorias.component';
import { IngresoStockComponent } from './inventory/pages/ingreso-stock/ingreso-stock.component';
import { HistorialDevolucionesComponent } from './pages/entregas/historial-devoluciones/historial-devoluciones.component';
import { RegistrarDevolucionComponent } from './pages/entregas/registrar-devolucion/registrar-devolucion.component';
import { HistorialComprasComponent } from './pages/entregas/historial-compras/historial-compras.component';
import { RegistrarCompraComponent } from './pages/entregas/registrar-compra/registrar-compra.component';
import {HistorialVentasComponent} from './ventas/pages/historial-ventas/historial-ventas.component';
import {BuscarProductoComponent} from './ventas/pages/buscar-producto/buscar-producto.component';
import {RegistrarVentaComponent} from './ventas/pages/registrar-venta/registrar-venta.component';
import {FiltrarHistorialComponent} from './ventas/pages/filtrar-historial/filtrar-historial.component';
import {DetalleVentaComponent} from './ventas/pages/detalle-venta/detalle-venta.component';
import {AnularVentaComponent} from './ventas/pages/anular-venta/anular-venta.component';
import {NotificacionPedidosComponent} from './pages/compras/notificacion-pedidos/notificacion-pedidos.component';
import {PrevisualizarOrdenComponent} from './pages/compras/previsualizar-orden/previsualizar-orden.component';
import {SettingsComponent} from './configuracion/pages/settings/settings.component';
import { SupplierListComponent } from './supplier/pages/supplier-list/supplier-list.component';
import { CreateSupplierComponent } from './supplier/pages/create-supplier/create-supplier.component';
import { SupplierDetailComponent } from './supplier/pages/supplier-detail/supplier-detail.component';
import { EditSupplierComponent } from './supplier/pages/edit-supplier/edit-supplier.component';
import { PurchasingDashboardComponent } from './purchasing/pages/purchasing-dashboard/purchasing-dashboard.component';
import {AlertasComponent} from './inventory/pages/alertas/alertas.component';

export const routes: Routes = [
  { path: '', redirectTo: 'health', pathMatch: 'full' },

  { path: 'ventas', component: HistorialVentasComponent },
  { path: 'ventas/crear', component: BuscarProductoComponent },
  { path: 'ventas/registrar', component: RegistrarVentaComponent },
  { path: 'ventas/historial/filtrar', component: FiltrarHistorialComponent },
  { path: 'ventas/:id', component: DetalleVentaComponent },
  { path: 'ventas/:id/anular', component: AnularVentaComponent },

  { path: 'alertas', component: AlertasComponent },


  { path: 'entregas', component: HistorialDevolucionesComponent },
  { path: 'entregas/nueva', component: RegistrarDevolucionComponent },
  { path: 'entregas/compras', component: HistorialComprasComponent },
  { path: 'entregas/compras/nueva', component: RegistrarCompraComponent },

  { path: 'inventario/categorias', component: CategoriasComponent },
  { path: 'proveedores', component: SupplierListComponent },
  { path: 'proveedores/nuevo', component: CreateSupplierComponent },
  { path: 'proveedores/:id', component: SupplierDetailComponent },
  { path: 'proveedores/:id/editar', component: EditSupplierComponent },

  { path: 'compras-gestion', component: PurchasingDashboardComponent },

  { path: 'inventario', component: InventoryComponent },
  { path: 'inventario/crear', component: CrearProductoComponent },
  { path: 'inventario/ingreso', component: IngresoStockComponent },
  { path: 'inventario/:id', component: ProductDetailComponent },
  { path: 'inventario/editar/:id', component: EditarProductoComponent },

  { path: 'compras/notificaciones', component: NotificacionPedidosComponent },
  { path: 'compras/previsualizar-orden', component: PrevisualizarOrdenComponent },
  { path: 'previsualizar-orden/:id', component: PrevisualizarOrdenComponent },

  { path: 'health', component: HealthComponent },
  { path: 'configuracion', component: SettingsComponent },
];
