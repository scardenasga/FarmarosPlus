import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CreateProductRequest, Product, Categoria } from '../models/product.model';
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

  createProduct(product: CreateProductRequest): Observable<Product> {
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
