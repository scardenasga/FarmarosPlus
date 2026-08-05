export interface DetalleDevolucionResponse {
  idProducto: number;
  nombreProducto: string;
  numeroLote: string | null;
  cantidad: number;
}

export interface DevolucionResponse {
  id: number;
  idProveedor: number;
  nombreProveedor: string;
  usuarioResponsable: string;
  motivo: string | null;
  fecha: string;
  detalles: DetalleDevolucionResponse[];
}

export interface DetalleDevolucionClienteResponse {
  idDetalleVenta: number;
  idProducto: number;
  nombreProducto: string;
  numeroLote: string | null;
  cantidad: number;
}

export interface DevolucionClienteResponse {
  id: number;
  idVenta: number;
  nombreCliente: string | null;
  documentoCliente: string | null;
  usuarioResponsable: string;
  motivo: string | null;
  fecha: string;
  detalles: DetalleDevolucionClienteResponse[];
}

export interface DetalleCompraResponse {
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface CompraResponse {
  id: number;
  idProveedor: number;
  nombreProveedor: string;
  usuarioResponsable: string;
  numeroFactura: string | null;
  notas: string | null;
  fechaRecepcion: string;
  total: number;
  detalles: DetalleCompraResponse[];
}

export interface RegistrarCompraRequest {
  idProveedor: number;
  usuarioResponsable: string;
  numeroFactura?: string;
  notas?: string;
  detalles: {
    idProducto: number;
    cantidad: number;
    precioUnitario: number;
  }[];
}

export interface RegistrarDevolucionRequest {
  idProveedor: number;
  usuarioResponsable: string;
  motivo: string;
  detalles: {
    idProducto: number;
    idLote: number;
    cantidad: number;
  }[];
}

export interface RegistrarDevolucionClienteRequest {
  idVenta: number;
  usuarioResponsable: string;
  nombreCliente?: string;
  documentoCliente?: string;
  motivo?: string;
  detalles: {
    idDetalleVenta: number;
    cantidad: number;
  }[];
}

export interface VentaDetalleResponse {
  id: number;
  producto: {
    id: number;
    nombre: string;
  };
  lote: {
    idLote: number;
    numeroLote: string | null;
  } | null;
  cantidad: number;
  precioUnitarioAplicado: number;
  subtotalLinea: number;
  ivaLinea: number;
}

export interface VentaResponse {
  id: number;
  fecha: string;
  estado: string;
  motivoAnulacion: string | null;
  subtotal: number;
  iva: number;
  descuento: number;
  total: number;
  cambio: number;
  detalles: VentaDetalleResponse[];
}

export interface AlertaDetallada {
  idOrden: number;
  codigoGenerado: string;
  proveedorNombre: string;
  estadoActual: string;
  fechaCreacionFormateada: string;
  diasTranscurridos: number;
}

export interface ResumenSeguimiento {
  totalOrdenes: number;
  pendientesRecibirOPagar: number;
  vencidasAtrasadas: number;
  alertasDetalladas: AlertaDetallada[];
}

export interface PrevisualizacionOrden {
  proveedorNombre: string;
  items: {
    nombre: string;
    cantidadSugerida: number;
    precioUnitario: number;
    motivo: string;
  }[];
}
