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

export interface CreateSupplierRequest {
  nombre: string;
  nit?: string;
  telefono?: string;
  email?: string;
  contacto?: string;
  condicionPago?: string;
}

export interface UpdateSupplierStatusRequest {
  estado: 'ACTIVO' | 'INACTIVO';
}
