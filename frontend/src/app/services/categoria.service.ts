import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string | null;
}

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly base = 'http://localhost:8080/api/categorias';

  constructor(private http: HttpClient) {}

  listar(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(this.base);
  }

  crear(nombre: string, descripcion: string): Observable<CategoriaResponse> {
    return this.http.post<CategoriaResponse>(this.base, { nombre, descripcion });
  }

  actualizar(id: number, nombre: string, descripcion: string): Observable<CategoriaResponse> {
    return this.http.put<CategoriaResponse>(`${this.base}/${id}`, { nombre, descripcion });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
