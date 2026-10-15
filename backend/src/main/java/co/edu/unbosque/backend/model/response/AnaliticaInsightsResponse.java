package co.edu.unbosque.backend.model.response;

/**
 * DTO de diagnósticos e insights para el módulo de analítica avanzada.
 *
 * @author Sebastian Cardenas Garcia
 */
public record AnaliticaInsightsResponse(
        String productoMasRentable,
        String diaMayorDemanda,
        String rotacionCritica
) {
}
