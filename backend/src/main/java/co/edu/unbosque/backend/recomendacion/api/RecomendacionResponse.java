package co.edu.unbosque.backend.recomendacion.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para el endpoint POS.
 */
public record RecomendacionResponse(
        String origen,
        LocalDateTime actualizadoEn,
        int totalVentasAnalizadas,
        List<RecomendacionItemResponse> recomendaciones
) {}
