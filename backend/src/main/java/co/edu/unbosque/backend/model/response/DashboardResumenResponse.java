package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para el resumen de KPIs del dashboard administrativo.
 * Contiene indicadores agregados de ventas, compras e inventario.
 *
 * @author Angie Tatiana Ortiz
 * @author Sebastian Cardenas Garcia
 */
public record DashboardResumenResponse(
        Double ventasDelDia,
        Long cantidadVentasDelDia,
        Double ventasDelMes,
        Long cantidadVentasDelMes,
        Long productosStockBajo,
        Long productosPorVencer,
        Double comprasDelMes,
        Double gananciaDelMes,
        Double margenGanancia
) {
}