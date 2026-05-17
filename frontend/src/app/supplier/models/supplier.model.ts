export interface Supplier {
  idProveedor: number;
  nombre: string;
  nit: string | null;
  telefono: string | null;
  email: string | null;
  contacto: string | null;
  estado: 'ACTIVO' | 'INACTIVO';
  condicionPago: string | null;
  fechaCreacion: string;
  fechaModificacion: string;
}

export interface SupplierProductRel {
  id: number;
  nombre: string;
  descripcion: string | null;
  codigoBarras: string;
  estado: string;
  codigoProductoProveedor: string | null;
  precioReferencia: number | null;
  estadoRelacion: 'ACTIVO' | 'INACTIVO';
}

export interface SupplierDetalleResponse extends Supplier {
  productos: SupplierProductRel[];
}

export interface CreateSupplierRequest {
  nombre: string;
  nit?: string;
  telefono?: string;
  email?: string;
  contacto?: string;
  condicionPago?: string;
}

export interface UpdateSupplierRequest {
  nombre?: string;
  nit?: string;
  telefono?: string;
  email?: string;
  contacto?: string;
  condicionPago?: string;
}

export interface UpdateSupplierStatusRequest {
  estado: 'ACTIVO' | 'INACTIVO';
}

export interface AssociateProductRequest {
  productoId: number;
  codigoProductoProveedor?: string;
  precioReferencia?: number;
}

export interface UpdateRelationStatusRequest {
  estado: 'ACTIVO' | 'INACTIVO';
}
