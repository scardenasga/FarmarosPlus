export interface Lote {
  id: number;
  numeroLote: string;
  fechaVencimiento: string;
  cantidad: number;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  unit: string;
  category: string;
  imageUrl?: string;
  trend?: number;
  lotes?: Lote[];
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
