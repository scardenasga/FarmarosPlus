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
import { PurchaseHistoryComponent } from './purchasing/pages/purchase-history/purchase-history.component';
import { RegisterPurchaseComponent } from './purchasing/pages/register-purchase/register-purchase.component';
import { OrderNotificationsComponent } from './purchasing/pages/order-notifications/order-notifications.component';
import { OrderPreviewComponent } from './purchasing/pages/order-preview/order-preview.component';

// --- COMPONENTES DE DASHBOARD ---
import { AdminDashboardComponent } from './dashboard/pages/admin-dashboard/admin-dashboard.component';
import { AnalyticsComponent } from './dashboard/pages/analytics/analytics.component';

export const routes: Routes = [
  // Redirige raíz al dashboard
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  // --- DASHBOARD ADMINISTRATIVO (Home principal) ---
  { path: 'dashboard', component: AdminDashboardComponent },

  // --- MÓDULO DE ANALÍTICA AVANZADA ---
  { path: 'analitica', component: AnalyticsComponent },
  { path: 'dashboard/analitica', redirectTo: 'analitica', pathMatch: 'full' },
  { path: 'reportes/analitica', redirectTo: 'analitica', pathMatch: 'full' },

  // --- HEALTH (redirige al dashboard) ---
  { path: 'health', component: HealthComponent },

  // --- VENTAS ---
  { path: 'ventas', component: HistorialVentasComponent },
  { path: 'ventas/pos', component: PosVentaComponent },
  // Flujo antiguo (buscar -> registrar) reemplazado por la vista POS única
  { path: 'ventas/crear', redirectTo: 'ventas/pos', pathMatch: 'full' },
  { path: 'ventas/registrar', redirectTo: 'ventas/pos', pathMatch: 'full' },
  { path: 'ventas/historial/filtrar', component: FiltrarHistorialComponent },
  { path: 'reportes/ventas', component: ReporteVentasComponent },
  { path: 'ventas/:id', component: DetalleVentaComponent },
  { path: 'ventas/:id/anular', component: AnularVentaComponent },

  // --- ALERTAS ---
  { path: 'alertas', component: AlertasComponent },

  // --- COMPRAS Y DEVOLUCIONES ---
  { path: 'purchasing/return-history', component: ReturnHistoryComponent },
  { path: 'purchasing/register-return', component: RegisterReturnComponent },
  { path: 'purchasing/purchase-history', component: PurchaseHistoryComponent },
  { path: 'purchasing/register-purchase', component: RegisterPurchaseComponent },
  { path: 'purchasing/order-notifications', component: OrderNotificationsComponent },
  { path: 'purchasing/order-preview/:id', component: OrderPreviewComponent },

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
  { path: 'inventario', component: InventoryComponent },
  // Ingreso de stock ahora es un panel deslizante dentro de /inventario
  { path: 'inventario/ingreso', redirectTo: 'inventario', pathMatch: 'full' },
  // Crear/editar producto ahora son paneles deslizantes dentro de /inventario
  { path: 'inventario/crear', redirectTo: 'inventario', pathMatch: 'full' },
  { path: 'inventario/editar/:id', redirectTo: 'inventario', pathMatch: 'full' },
  { path: 'inventario/:id', component: ProductDetailComponent },

  // --- PROVEEDORES ---
  { path: 'proveedores', component: SupplierListComponent },
  { path: 'proveedores/nuevo', component: CreateSupplierComponent },
  { path: 'proveedores/:id', component: SupplierDetailComponent },
  { path: 'proveedores/:id/editar', component: EditSupplierComponent },

  // --- GESTIÓN DE COMPRAS ---
  { path: 'compras-gestion', component: PurchasingDashboardComponent },

  // --- CONFIGURACIÓN ---
  { path: 'health', component: HealthComponent },
  { path: 'configuracion', component: SettingsComponent },
];
