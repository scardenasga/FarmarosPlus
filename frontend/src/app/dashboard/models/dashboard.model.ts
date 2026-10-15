export interface DashboardResumen {
  ventasDelDia: number;
  cantidadVentasDelDia: number;
  ventasDelMes: number;
  cantidadVentasDelMes: number;
  productosStockBajo: number;
  productosPorVencer: number;
  comprasDelMes: number;
  gananciaDelMes: number;
  margenGanancia: number;
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

export interface ComparativaMensual {
  mes: string;
  ventas: number;
  costos: number;
}

export interface DashboardResponse {
  resumen: DashboardResumen;
  ventasPorDia: VentaPorDia[];
  transacciones: VentaDashboard[];
  inventarioPorCategoria: InventarioCategoria[];
  productosDestacados: ProductoDestacado[];
  productosStockBajo: ProductoStockBajo[];
  comparativaMensual: ComparativaMensual[];
  fechaConsulta: string;
}

export interface ProductoAnalitica {
  idProducto: number;
  nombre: string;
  categoria: string;
  unidadesVendidas: number;
  ventasTotales: number;
  margenGanancia: number;
  stockActual: number;
  stockMinimo: number;
  estadoStock: 'ok' | 'warning' | 'critical';
}

export interface AnaliticaResumen {
  ventasFiltradas: number;
  crecimientoVentas: number;
  unidadesVendidas: number;
  promedioUnidadesPorDia: number;
  margenBrutoPromedio: number;
  categoriaLider: string;
  porcentajeCategoriaLider: number;
  topProducto: string;
  unidadesTopProducto: number;
  perdidasRiesgo: number;
  productosEnRiesgo: number;
}

export interface AnaliticaInsights {
  productoMasRentable: string;
  diaMayorDemanda: string;
  rotacionCritica: string;
}

export interface AnaliticaDashboardResponse {
  resumen: AnaliticaResumen;
  tendenciaActual: VentaPorDia[];
  tendenciaAnterior: VentaPorDia[];
  ventasPorCategoria: InventarioCategoria[];
  productos: ProductoAnalitica[];
  insights: AnaliticaInsights;
  fechaConsulta: string;
}

export type PeriodPreset = '7d' | '30d' | '90d' | 'custom';

export interface DashboardPeriod {
  preset: PeriodPreset;
  fechaInicio: string;
  fechaFin: string;
}