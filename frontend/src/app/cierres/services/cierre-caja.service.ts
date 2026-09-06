import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CierreResumen {
  fechaApertura: string;
  fechaCierre: string;
  montoInicial: number;
  totalEfectivo: number;
  totalTarjeta: number;
  totalTransferencia: number;
  totalVentas: number;
  totalDescuentos: number;
  totalIva: number;
  cantidadVentas: number;
  cantidadAnuladas: number;
  gananciaEstimada: number;
  efectivoEsperado: number;
}

export interface CierreCaja {
  id: number;
  fechaApertura: string;
  fechaCierre: string;
  idUsuario: number | null;
  username: string | null;
  montoInicial: number;
  montoDeclarado: number | null;
  totalEfectivo: number;
  totalTarjeta: number;
  totalTransferencia: number;
  totalVentas: number;
  totalDescuentos: number;
  totalIva: number;
  cantidadVentas: number;
  cantidadAnuladas: number;
  gananciaEstimada: number;
  diferencia: number | null;
  observaciones: string | null;
  estado: string;
  fechaCreacion: string;
}

export interface CrearCierreRequest {
  fechaApertura?: string;
  fechaCierre?: string;
  montoInicial?: number;
  montoDeclarado?: number | null;
  idUsuario?: number | null;
  observaciones?: string | null;
}

@Injectable({ providedIn: 'root' })
export class CierreCajaService {
  private api = '/api/cierres-caja';

  constructor(private http: HttpClient) {}

  resumen(params: { desde?: string; hasta?: string; usuarioId?: number | null; montoInicial?: number | null }): Observable<CierreResumen> {
    let httpParams = new HttpParams();
    if (params.desde) httpParams = httpParams.set('desde', params.desde);
    if (params.hasta) httpParams = httpParams.set('hasta', params.hasta);
    if (params.usuarioId) httpParams = httpParams.set('usuarioId', String(params.usuarioId));
    if (params.montoInicial != null) httpParams = httpParams.set('montoInicial', String(params.montoInicial));
    return this.http.get<CierreResumen>(`${this.api}/resumen`, { params: httpParams });
  }

  crear(req: CrearCierreRequest): Observable<CierreCaja> {
    return this.http.post<CierreCaja>(this.api, req);
  }

  listar(): Observable<CierreCaja[]> {
    return this.http.get<CierreCaja[]>(this.api);
  }

  obtener(id: number): Observable<CierreCaja> {
    return this.http.get<CierreCaja>(`${this.api}/${id}`);
  }
}
