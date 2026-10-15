export interface RecomendacionItem {
  productoId: number;
  nombre: string;
  soporte: number;
  confianza: number;
  lift: number;
  frecuenciaConjunta: number;
}

export type OrigenRecomendacion = 'FPGROWTH' | 'POPULARIDAD' | 'SIN_DATOS';

export interface RecomendacionResponse {
  origen: OrigenRecomendacion;
  actualizadoEn: string;
  totalVentasAnalizadas: number;
  recomendaciones: RecomendacionItem[];
}
