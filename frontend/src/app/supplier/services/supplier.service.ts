import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  Supplier, 
  CreateSupplierRequest, 
  UpdateSupplierStatusRequest, 
  SupplierDetalleResponse, 
  UpdateSupplierRequest, 
  AssociateProductRequest, 
  UpdateRelationStatusRequest,
  SupplierProductRel
} from '../models/supplier.model';

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

  getDetail(id: number): Observable<SupplierDetalleResponse> {
    return this.http.get<SupplierDetalleResponse>(`${this.apiUrl}/${id}/detalle`);
  }

  create(request: CreateSupplierRequest): Observable<Supplier> {
    return this.http.post<Supplier>(this.apiUrl, request);
  }

  update(id: number, request: UpdateSupplierRequest): Observable<Supplier> {
    return this.http.patch<Supplier>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(id: number, status: UpdateSupplierStatusRequest): Observable<Supplier> {
    return this.http.patch<Supplier>(`${this.apiUrl}/${id}/estado`, status);
  }

  associateProduct(id: number, request: AssociateProductRequest): Observable<SupplierProductRel> {
    return this.http.post<SupplierProductRel>(`${this.apiUrl}/${id}/productos`, request);
  }

  updateRelationStatus(supplierId: number, productId: number, status: UpdateRelationStatusRequest): Observable<SupplierProductRel> {
    return this.http.patch<SupplierProductRel>(`${this.apiUrl}/${supplierId}/productos/${productId}/estado`, status);
  }

  deleteRelation(supplierId: number, productId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${supplierId}/productos/${productId}`);
  }
}
