import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrearProductoRequest, Product, Categoria, ProductoDetalleResponse, ActualizarProductoRequest, IngresoStockRequest, LoteResponse, TendenciaProducto } from '../models/product.model';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private http = inject(HttpClient);
  private productsUrl = '/api/productos';
  private categoriasUrl = '/api/categorias';

  // Products
  getActiveProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.productsUrl}/activos`);
  }

  /** Todos los productos sin importar el estado (para filtrar en el inventario). */
  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.productsUrl}`);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.productsUrl}/${id}`);
  }

  getProductDetail(id: number): Observable<ProductoDetalleResponse> {
    return this.http.get<ProductoDetalleResponse>(`${this.productsUrl}/${id}/detalle`);
  }

  updateProduct(id: number, product: ActualizarProductoRequest): Observable<Product> {
    return this.http.patch<Product>(`${this.productsUrl}/${id}`, product);
  }

  createProduct(product: CrearProductoRequest): Observable<Product> {
    return this.http.post<Product>(this.productsUrl, product);
  }

  registrarIngreso(codigoBarras: string, request: IngresoStockRequest): Observable<any> {
    return this.http.post(`${this.productsUrl}/codigo-barras/${codigoBarras}/ingresos`, request);
  }

  getLotsExpiringBy(date: string): Observable<LoteResponse[]> {
    return this.http.get<LoteResponse[]>(`/api/inventario/lotes/proximos-a-vencer?fechaCorte=${date}`);
  }

  searchProducts(nombre?: string, codigo?: string): Observable<Product[]> {
    let params = '';
    if (nombre) params += `nombre=${nombre}`;
    if (codigo) params += (params ? '&' : '') + `codigo=${codigo}`;
    return this.http.get<Product[]>(`${this.productsUrl}/buscar?${params}`);
  }

  /**
   * Tendencia de ventas por producto: % de cambio entre los últimos `dias`
   * y el periodo anterior de igual duración (solo ventas completadas).
   */
  getTendencias(dias: number): Observable<TendenciaProducto[]> {
    const params = new HttpParams().set('dias', String(dias));
    return this.http.get<TendenciaProducto[]>(`${this.productsUrl}/tendencia-ventas`, { params });
  }

  /** Eliminación física: solo permitida por el backend para productos sin ventas. */
  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.productsUrl}/${id}`);
  }

  // Categories
  // Removed: now handled by CategoriaService
}
