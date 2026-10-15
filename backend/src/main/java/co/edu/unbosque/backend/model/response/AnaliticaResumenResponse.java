package co.edu.unbosque.backend.model.response;

/**
 * DTO de KPIs resumidos para el módulo de analítica avanzada.
 *
 * @author Sebastian Cardenas Garcia
 */
public record AnaliticaResumenResponse(
        Double ventasFiltradas,
        Double crecimientoVentas,
        Long unidadesVendidas,
        Double promedioUnidadesPorDia,
        Double margenBrutoPromedio,
        String categoriaLider,
        Double porcentajeCategoriaLider,
        String topProducto,
        Long unidadesTopProducto,
        Double perdidasRiesgo,
        Long productosEnRiesgo
) {
}
