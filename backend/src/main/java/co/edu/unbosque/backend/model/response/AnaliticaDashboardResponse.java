package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO principal de respuesta para el módulo de analítica avanzada.
 *
 * @author Sebastian Cardenas Garcia
 */
public record AnaliticaDashboardResponse(
        AnaliticaResumenResponse resumen,
        List<VentaPorDiaResponse> tendenciaActual,
        List<VentaPorDiaResponse> tendenciaAnterior,
        List<InventarioCategoriaResponse> ventasPorCategoria,
        List<ProductoAnaliticaResponse> productos,
        AnaliticaInsightsResponse insights,
        LocalDateTime fechaConsulta
) {
}
