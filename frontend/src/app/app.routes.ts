import { Routes } from '@angular/router';

// --- COMPONENTES DE INVENTARIO ---
import { InventoryComponent} from './inventory/pages/inventory/inventory.component';
import {HealthComponent} from './health/health.component';
import { ProductDetailComponent } from './inventory/pages/product-detail/product-detail.component';
import { HistorialVentasComponent} from './ventas/pages/historial-ventas/historial-ventas.component';
import {FiltrarHistorialComponent} from './ventas/pages/filtrar-historial/filtrar-historial.component';
import {PosVentaComponent} from './ventas/pages/pos-venta/pos-venta.component';
import {DetalleVentaComponent} from './ventas/pages/detalle-venta/detalle-venta.component';
import {AnularVentaComponent} from './ventas/pages/anular-venta/anular-venta.component';
import {SettingsComponent} from './configuracion/pages/settings/settings.component';
import { SupplierListComponent } from './supplier/pages/supplier-list/supplier-list.component';
import { CreateSupplierComponent } from './supplier/pages/create-supplier/create-supplier.component';
import { SupplierDetailComponent } from './supplier/pages/supplier-detail/supplier-detail.component';
import { EditSupplierComponent } from './supplier/pages/edit-supplier/edit-supplier.component';
import { PurchasingDashboardComponent } from './purchasing/pages/purchasing-dashboard/purchasing-dashboard.component';
import {AlertasComponent} from './inventory/pages/alertas/alertas.component';
import { ReporteVentasComponent } from './reportes/pages/reporte-ventas/reporte-ventas.component';

// --- NUEVOS COMPONENTES DE COMPRAS Y DEVOLUCIONES ---
import { ReturnHistoryComponent } from './purchasing/pages/return-history/return-history.component';
import { RegisterReturnComponent } from './purchasing/pages/register-return/register-return.component';
import { ReturnDetailComponent } from './purchasing/pages/return-detail/return-detail.component';
import { ReturnEditComponent } from './purchasing/pages/return-edit/return-edit.component';
import { PurchaseHistoryComponent } from './purchasing/pages/purchase-history/purchase-history.component';
import { PurchaseDetailComponent } from './purchasing/pages/purchase-detail/purchase-detail.component';
import { PurchaseEditComponent } from './purchasing/pages/purchase-edit/purchase-edit.component';
import { RegisterPurchaseComponent } from './purchasing/pages/register-purchase/register-purchase.component';
import { OrderNotificationsComponent } from './purchasing/pages/order-notifications/order-notifications.component';
import { OrderPreviewComponent } from './purchasing/pages/order-preview/order-preview.component';

// --- COMPONENTES DE DASHBOARD ---
import { AdminDashboardComponent } from './dashboard/pages/admin-dashboard/admin-dashboard.component';
import { AnalyticsComponent } from './dashboard/pages/analytics/analytics.component';
import { LoginComponent } from './auth/pages/login/login.component';
import { authGuard, loginGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  // Auth - primera pantalla
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  // Redirige raíz al dashboard (guard redirige a login si no hay sesión)
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  // --- DASHBOARD ADMINISTRATIVO (Home principal) ---
  { path: 'dashboard', component: AdminDashboardComponent, canActivate: [authGuard] },

  // --- MÓDULO DE ANALÍTICA AVANZADA ---
  { path: 'analitica', component: AnalyticsComponent, canActivate: [authGuard] },
  { path: 'dashboard/analitica', redirectTo: 'analitica', pathMatch: 'full' },
  { path: 'reportes/analitica', redirectTo: 'analitica', pathMatch: 'full' },

  // --- HEALTH (redirige al dashboard) ---
  { path: 'health', component: HealthComponent },

  // --- VENTAS ---
  { path: 'ventas', component: HistorialVentasComponent, canActivate: [authGuard] },
  { path: 'ventas/pos', component: PosVentaComponent, canActivate: [authGuard] },
  // Flujo antiguo (buscar -> registrar) reemplazado por la vista POS única
  { path: 'ventas/crear', redirectTo: 'ventas/pos', pathMatch: 'full' },
  { path: 'ventas/registrar', redirectTo: 'ventas/pos', pathMatch: 'full' },
  { path: 'ventas/historial/filtrar', component: FiltrarHistorialComponent, canActivate: [authGuard] },
  { path: 'reportes/ventas', component: ReporteVentasComponent, canActivate: [authGuard] },
  { path: 'ventas/:id', component: DetalleVentaComponent, canActivate: [authGuard] },
  { path: 'ventas/:id/anular', component: AnularVentaComponent, canActivate: [authGuard] },

  // --- ALERTAS ---
  { path: 'alertas', component: AlertasComponent, canActivate: [authGuard] },

  // --- COMPRAS Y DEVOLUCIONES ---
  { path: 'purchasing/return-history', component: ReturnHistoryComponent, canActivate: [authGuard] },
  { path: 'purchasing/register-return', component: RegisterReturnComponent, canActivate: [authGuard] },
  { path: 'purchasing/return-detail/:id', component: ReturnDetailComponent, canActivate: [authGuard] },
  { path: 'purchasing/return-edit/:id', component: ReturnEditComponent, canActivate: [authGuard] },
  { path: 'purchasing/purchase-history', component: PurchaseHistoryComponent, canActivate: [authGuard] },
  { path: 'purchasing/purchase-detail/:id', component: PurchaseDetailComponent, canActivate: [authGuard] },
  { path: 'purchasing/purchase-edit/:id', component: PurchaseEditComponent, canActivate: [authGuard] },
  { path: 'purchasing/register-purchase', component: RegisterPurchaseComponent, canActivate: [authGuard] },
  { path: 'purchasing/order-notifications', component: OrderNotificationsComponent, canActivate: [authGuard] },
  { path: 'purchasing/order-preview/:id', component: OrderPreviewComponent, canActivate: [authGuard] },

  // Redirecciones para compatibilidad con rutas antiguas
  { path: 'entregas', redirectTo: 'purchasing/return-history', pathMatch: 'full' },
  { path: 'entregas/nueva', redirectTo: 'purchasing/register-return', pathMatch: 'full' },
  { path: 'entregas/compras', redirectTo: 'purchasing/purchase-history', pathMatch: 'full' },
  { path: 'entregas/compras/nueva', redirectTo: 'purchasing/register-purchase', pathMatch: 'full' },
  { path: 'compras/notificaciones', redirectTo: 'purchasing/order-notifications', pathMatch: 'full' },
  { path: 'compras/previsualizar-orden', redirectTo: 'purchasing/order-notifications', pathMatch: 'full' },
  { path: 'previsualizar-orden/:id', redirectTo: 'purchasing/order-preview/:id', pathMatch: 'full' },

  // --- INVENTARIO ---
  // Categorias ahora es un panel deslizante dentro de /inventario
  { path: 'inventario/categorias', redirectTo: 'inventario', pathMatch: 'full' },
  { path: 'inventario', component: InventoryComponent, canActivate: [authGuard] },
  // Ingreso de stock ahora es un panel deslizante dentro de /inventario
  { path: 'inventario/ingreso', redirectTo: 'inventario', pathMatch: 'full' },
  // Crear/editar producto ahora son paneles deslizantes dentro de /inventario
  { path: 'inventario/crear', redirectTo: 'inventario', pathMatch: 'full' },
  { path: 'inventario/editar/:id', redirectTo: 'inventario', pathMatch: 'full' },
  { path: 'inventario/:id', component: ProductDetailComponent, canActivate: [authGuard] },

  // --- PROVEEDORES ---
  { path: 'proveedores', component: SupplierListComponent, canActivate: [authGuard] },
  { path: 'proveedores/nuevo', component: CreateSupplierComponent, canActivate: [authGuard] },
  { path: 'proveedores/:id', component: SupplierDetailComponent, canActivate: [authGuard] },
  { path: 'proveedores/:id/editar', component: EditSupplierComponent, canActivate: [authGuard] },

  // --- GESTIÓN DE COMPRAS ---
  { path: 'compras-gestion', component: PurchasingDashboardComponent, canActivate: [authGuard] },

  // --- CONFIGURACIÓN ---
  { path: 'health', component: HealthComponent },
  { path: 'configuracion', component: SettingsComponent, canActivate: [authGuard] },
];
