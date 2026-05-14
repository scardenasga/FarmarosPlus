import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria } from '../inventory/models/product.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private http = inject(HttpClient);
  private readonly base = '/api/categorias';

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.base);
  }

  crear(nombre: string, descripcion: string): Observable<Categoria> {
    return this.http.post<Categoria>(this.base, { nombre, descripcion });
  }

  actualizar(id: number, nombre: string, descripcion: string): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.base}/${id}`, { nombre, descripcion });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
