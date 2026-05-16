import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Supplier, CreateSupplierRequest, UpdateSupplierStatusRequest } from '../models/supplier.model';

@Injectable({
  providedIn: 'root'
})
export class SupplierService {
  private http = inject(HttpClient);
  private apiUrl = '/api/proveedores';

  listAll(): Observable<Supplier[]> {
    return this.http.get<Supplier[]>(this.apiUrl);
  }

  listActive(): Observable<Supplier[]> {
    return this.http.get<Supplier[]>(`${this.apiUrl}/activos`);
  }

  getById(id: number): Observable<Supplier> {
    return this.http.get<Supplier>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateSupplierRequest): Observable<Supplier> {
    return this.http.post<Supplier>(this.apiUrl, request);
  }

  updateStatus(id: number, status: UpdateSupplierStatusRequest): Observable<Supplier> {
    return this.http.patch<Supplier>(`${this.apiUrl}/${id}/estado`, status);
  }
}
