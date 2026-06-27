package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida principal del dashboard administrativo./**
  * Agrupa todos los indicadores: resumen KPIs, ventas por dia, 
  * inventario por categoria y productos destacados.
  * 
  * @author Angie Tatiana Ortiz
  */
 public record DashboardResponse (
    DashboardResumenResponse resumen,
    List<VentaPorDiaResponse> ventasPorDia,
    List<InventarioCategoriaResponse> inventarioPorCategoria,
    List<ProductoDestacadoResponse> productosDestacados,
    LocalDateTime fechaConsulta    
 
) { 
 }