import { Routes } from '@angular/router';

// --- TUS COMPONENTES DE VENTAS (Recuperados) ---
import { InventoryComponent} from './inventory/pages/inventory/inventory.component';
import {HealthComponent} from './health/health.component';
import { CrearProductoComponent } from './inventory/pages/crear-producto/crear-producto.component';
import { ProductDetailComponent } from './inventory/pages/product-detail/product-detail.component';
import { EditarProductoComponent } from './inventory/pages/editar-producto/editar-producto.component';
import { CategoriasComponent } from './inventory/pages/categorias/categorias.component';
import { IngresoStockComponent } from './inventory/pages/ingreso-stock/ingreso-stock.component';
import {HistorialVentasComponent} from './ventas/pages/historial-ventas/historial-ventas.component';
import {BuscarProductoComponent} from './ventas/pages/buscar-producto/buscar-producto.component';
import {RegistrarVentaComponent} from './ventas/pages/registrar-venta/registrar-venta.component';
import {FiltrarHistorialComponent} from './ventas/pages/filtrar-historial/filtrar-historial.component';
import {DetalleVentaComponent} from './ventas/pages/detalle-venta/detalle-venta.component';
import {AnularVentaComponent} from './ventas/pages/anular-venta/anular-venta.component';
import {SettingsComponent} from './configuracion/pages/settings/settings.component';
import { SupplierListComponent } from './supplier/pages/supplier-list/supplier-list.component';
import { CreateSupplierComponent } from './supplier/pages/create-supplier/create-supplier.component';
import { SupplierDetailComponent } from './supplier/pages/supplier-detail/supplier-detail.component';
import { EditSupplierComponent } from './supplier/pages/edit-supplier/edit-supplier.component';
import { PurchasingDashboardComponent } from './purchasing/pages/purchasing-dashboard/purchasing-dashboard.component';
import {AlertasComponent} from './inventory/pages/alertas/alertas.component';

// --- NUEVOS COMPONENTES DE COMPRAS Y DEVOLUCIONES ---
import { ReturnHistoryComponent } from './purchasing/pages/return-history/return-history.component';
import { RegisterReturnComponent } from './purchasing/pages/register-return/register-return.component';
import { PurchaseHistoryComponent } from './purchasing/pages/purchase-history/purchase-history.component';
import { RegisterPurchaseComponent } from './purchasing/pages/register-purchase/register-purchase.component';
import { OrderNotificationsComponent } from './purchasing/pages/order-notifications/order-notifications.component';
import { OrderPreviewComponent } from './purchasing/pages/order-preview/order-preview.component';

export const routes: Routes = [
  { path: '', redirectTo: 'health', pathMatch: 'full' },

  { path: 'ventas', component: HistorialVentasComponent },
  { path: 'ventas/crear', component: BuscarProductoComponent },
  { path: 'ventas/registrar', component: RegistrarVentaComponent },
  { path: 'ventas/historial/filtrar', component: FiltrarHistorialComponent },
  { path: 'ventas/:id', component: DetalleVentaComponent },
  { path: 'ventas/:id/anular', component: AnularVentaComponent },

  { path: 'alertas', component: AlertasComponent },

  // --- COMPRAS Y ENTREGAS (Reorganizado) ---
  { path: 'purchasing/return-history', component: ReturnHistoryComponent },
  { path: 'purchasing/register-return', component: RegisterReturnComponent },
  { path: 'purchasing/purchase-history', component: PurchaseHistoryComponent },
  { path: 'purchasing/register-purchase', component: RegisterPurchaseComponent },
  { path: 'purchasing/order-notifications', component: OrderNotificationsComponent },
  { path: 'purchasing/order-preview/:id', component: OrderPreviewComponent },

  // Redirecciones para compatibilidad
  { path: 'entregas', redirectTo: 'purchasing/return-history', pathMatch: 'full' },
  { path: 'entregas/nueva', redirectTo: 'purchasing/register-return', pathMatch: 'full' },
  { path: 'entregas/compras', redirectTo: 'purchasing/purchase-history', pathMatch: 'full' },
  { path: 'entregas/compras/nueva', redirectTo: 'purchasing/register-purchase', pathMatch: 'full' },
  { path: 'compras/notificaciones', redirectTo: 'purchasing/order-notifications', pathMatch: 'full' },
  { path: 'compras/previsualizar-orden', redirectTo: 'purchasing/order-notifications', pathMatch: 'full' }, // Corregido el destino
  { path: 'previsualizar-orden/:id', redirectTo: 'purchasing/order-preview/:id', pathMatch: 'full' },

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

  { path: 'health', component: HealthComponent },
  { path: 'configuracion', component: SettingsComponent },
];
