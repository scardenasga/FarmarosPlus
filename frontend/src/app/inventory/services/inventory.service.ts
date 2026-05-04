import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CreateProductRequest } from '../models/product.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private http = inject(HttpClient);
  private apiUrl = '/api/productos';

  createProduct(product: CreateProductRequest): Observable<any> {
    return this.http.post(this.apiUrl, product);
  }
}
