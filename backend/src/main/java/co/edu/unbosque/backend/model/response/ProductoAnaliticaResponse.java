package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para el desglose detallado de producto en analítica avanzada.
 *
 * @author Sebastian Cardenas Garcia
 */
public record ProductoAnaliticaResponse(
        Long idProducto,
        String nombre,
        String categoria,
        Long unidadesVendidas,
        Double ventasTotales,
        Double margenGanancia,
        Integer stockActual,
        Integer stockMinimo,
        String estadoStock
) {
}
