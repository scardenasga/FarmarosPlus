export interface DashboardResumen {
  totalVentas: number;
  cantidadVentas: number;
  ticketPromedio: number;
  productosActivos: number;
  productosStockBajo: number;
  valorInventario: number;
}

export interface VentaPorDia {
  fecha: string;
  total: number;
  cantidad: number;
}

export interface InventarioCategoria {
  categoria: string;
  stockTotal: number;
  cantidadProductos: number;
}

export interface ProductoDestacado {
  idProducto: number;
  nombre: string;
  cantidadVendida: number;
  totalVendido: number;
}

export interface DashboardResponse {
  resumen: DashboardResumen;
  ventasPorDia: VentaPorDia[];
  inventarioPorCategoria: InventarioCategoria[];
  productosDestacados: ProductoDestacado[];
  fechaConsulta: string;
}

export type PeriodPreset = '7d' | '30d' | '90d' | 'custom';

export interface DashboardPeriod {
  preset: PeriodPreset;
  fechaInicio: string;
  fechaFin: string;
}