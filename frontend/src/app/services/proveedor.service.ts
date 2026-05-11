import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProveedorResponse {
  id: number;
  nombre: string;
  nit: string | null;
  contacto: string | null;
  telefono: string | null;
  email: string | null;
  estado: string;
}

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

@Injectable({ providedIn: 'root' })
export class ProveedorService {
  private readonly api = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  listarProveedores(): Observable<ProveedorResponse[]> {
    return this.http.get<ProveedorResponse[]>(`${this.api}/proveedores`);
  }

  crearProveedor(nombre: string, nit?: string, contacto?: string, telefono?: string, email?: string): Observable<ProveedorResponse> {
    return this.http.post<ProveedorResponse>(`${this.api}/proveedores`, { nombre, nit, contacto, telefono, email });
  }

  registrarDevolucion(body: {
    idProveedor: number;
    usuarioResponsable: string;
    motivo: string;
    detalles: { idProducto: number; idLote: number; cantidad: number }[];
  }): Observable<DevolucionResponse> {
    return this.http.post<DevolucionResponse>(`${this.api}/devoluciones`, body);
  }

  listarDevoluciones(): Observable<DevolucionResponse[]> {
    return this.http.get<DevolucionResponse[]>(`${this.api}/devoluciones`);
  }

  buscarProductos(termino: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/productos/buscar?nombre=${encodeURIComponent(termino)}`);
  }

  obtenerLotes(productoId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/inventario/productos/${productoId}/lotes-disponibles`);
  }

  registrarCompra(body: {
    idProveedor: number;
    usuarioResponsable: string;
    numeroFactura?: string;
    notas?: string;
    detalles: { idProducto: number; cantidad: number; precioUnitario: number }[];
  }): Observable<CompraResponse> {
    return this.http.post<CompraResponse>(`${this.api}/compras`, body);
  }

  listarCompras(idProveedor?: number): Observable<CompraResponse[]> {
    const params = idProveedor ? `?idProveedor=${idProveedor}` : '';
    return this.http.get<CompraResponse[]>(`${this.api}/compras${params}`);
  }
}
