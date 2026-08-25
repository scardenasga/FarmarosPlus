export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string;
}

export interface Lote {
  id: number;
  numeroLote?: string;
  fechaVencimiento: string;
  cantidad: number;
}

export interface LoteResponse extends Lote {
  producto?: Product;
}

export interface Product {
  id: number;
  nombre: string;
  descripcion?: string;
  codigoBarras: string;
  stockMinimo: number;
  stockActual: number;
  costo: number;
  precioVenta: number;
  margenGanancia?: number;
  porcentajeIva?: number;
  requierePrescripcion?: boolean;
  estado: string;
  categoria?: Categoria;
  numeroLote?: string;
  // UI related fields (optional/computed in FE if needed)
  trend?: number;
}


export interface CrearProductoRequest {
  categoriaId?: number; // Opcional -
  nombre: string; //-
  descripcion?: string; // Opcional-
  codigoBarras: string;//-
  stockMinimo?: number; // Opcional-
  stockInicial: number;//-
  costo: number;//-
  precioVenta: number;// -
  porcentajeIva?: number; // Opcional -
  requierePrescripcion: boolean;
  fechaVencimiento: string | Date; // Opcional (ISO string 2027-12-31)
  numeroLote?: string; // Opcional
}

export interface ActualizarProductoRequest {
  categoriaId?: number;
  nombre?: string;
  descripcion?: string;
  stockMinimo?: number;
  stockActual?: number;
  costo?: number;
  precioVenta?: number;
  porcentajeIva?: number;
  requierePrescripcion?: boolean;
  estado?: string;
}

export interface CreateCategoriaRequest {
  nombre: string;
  descripcion: string;
}

export interface ProductoDetalleResponse {
  id: number;
  categoria: Categoria;
  nombre: string;
  descripcion: string;
  codigoBarras: string;
  stockMinimo: number;
  stockActual: number;
  costo: number;
  precioVenta: number;
  margenGanancia: number;
  porcentajeIva: number;
  requierePrescripcion: boolean;
  estado: string;
  lotes?: Lote[];
}

// Interfaces adicionales necesarias:
export interface CategoriaResponse {
  id: number;
  nombre: string;
}

/** Tendencia de ventas de un producto (backend: TendenciaProductoResponse). */
export interface TendenciaProducto {
  idProducto: number;
  unidadesRecientes: number;
  unidadesPrevias: number;
  porcentajeCambio: number;
  diasComparacion: number;
}

export interface IngresoStockRequest {
  cantidad: number;
  fechaVencimiento: string;
  numeroLote?: string;
  nuevoCosto?: number;
  nuevoPrecioVenta?: number;
}
