import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrearProductoRequest, Product, Categoria, ProductoDetalleResponse, ActualizarProductoRequest } from '../models/product.model';
import { Observable } from 'rxjs';

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

  searchProducts(nombre?: string, codigo?: string): Observable<Product[]> {
    let params = '';
    if (nombre) params += `nombre=${nombre}`;
    if (codigo) params += (params ? '&' : '') + `codigo=${codigo}`;
    return this.http.get<Product[]>(`${this.productsUrl}/buscar?${params}`);
  }

  // Categories
  // Removed: now handled by CategoriaService
}
