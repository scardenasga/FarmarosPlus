package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para el resumen de KPIs del dashboard administrativo.
 * Contiene indicadores agregados de ventas e inventario.
 * 
 * @author Angie Tatiana Ortiz
 */

public record DashboardResumenResponse(
    Double ventasDelDia,
    Long cantidadVentasDelDia,
    Double ventasDelMes,
    Long cantidadVentasDelMes,
    Long productosStockBajo,
    Long productosPorVencer

) {
}