export interface DashboardResumen {
  ventasDelDia: number;
  cantidadVentasDelDia: number;
  ventasDelMes: number;
  cantidadVentasDelMes: number;
  productosStockBajo: number;
  productosPorVencer: number;
}

export interface VentaPorDia {
  fecha: string;
  total: number;
  cantidad: number;
}

export interface VentaDashboard {
  idVenta: number;
  fecha: string;
  total: number;
  estado: string;
}

export interface InventarioCategoria {
  categoria: string;
  stockTotal: number;
  cantidadProductos: number;
}

export interface ProductoStockBajo {
  idProducto: number;
  nombre: string;
  stockActual: number;
  stockMinimo: number;
}

export interface ProductoDestacado {
  idProducto: number;
  nombre: string;
  cantidadVendida: number;
  totalVendido: number;
}

export interface ProductoPorVencer {
  idProducto: number;
  nombre: string;
  fechaVencimiento: string;
  stockActual: number;
}

export interface DashboardResponse {
  resumen: DashboardResumen;
  ventasPorDia: VentaPorDia[];
  transacciones: VentaDashboard[];
  inventarioPorCategoria: InventarioCategoria[];
  productosDestacados: ProductoDestacado[];
  productosStockBajo: ProductoStockBajo[];
  productosPorVencer: ProductoPorVencer[];
  fechaConsulta: string;
}

export type PeriodPreset = '7d' | '30d' | '90d' | 'custom';

export interface DashboardPeriod {
  preset: PeriodPreset;
  fechaInicio: string;
  fechaFin: string;
}