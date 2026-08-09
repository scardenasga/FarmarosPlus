package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida principal del dashboard.
 *
 * Agrupa la información necesaria para mostrar:
 * - Resumen de ventas del día y del mes
 * - Ventas agrupadas por día
 * - Transacciones
 * - Inventario por categoría
 * - Productos más vendidos
 * - Productos con stock bajo
 *
 * @author Angie Tatiana Ortiz
 */
public record DashboardResponse(
        DashboardResumenResponse resumen,
        List<VentaPorDiaResponse> ventasPorDia,
        List<VentaDashboardResponse> transacciones,
        List<InventarioCategoriaResponse> inventarioPorCategoria,
        List<ProductoDestacadoResponse> productosDestacados,
        List<ProductoStockBajoResponse> productosStockBajo,
        LocalDateTime fechaConsulta
        
) {
}