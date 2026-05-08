export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string;
}

export interface Lote {
  id: number;
  numeroLote: string;
  fechaVencimiento: string;
  cantidad: number;
}

export interface Product {
  id: number;
  nombre: string;
  codigoBarras: string;
  stockMinimo: number;
  stockActual: number;
  costo: number;
  precioVenta: number;
  estado: string;
  categoria?: Categoria;
  numeroLote?: string;
  // UI related fields (optional/computed in FE if needed)
  trend?: number;
}

export interface CreateProductRequest {
  nombre: string;
  codigoBarras: string;
  stockMinimo: number;
  stockInicial: number;
  costo: number;
  precioVenta: number;
  estado: string;
  categoriaId?: number;
  numeroLote?: string;
}

export interface CreateCategoriaRequest {
  nombre: string;
  descripcion: string;
}
