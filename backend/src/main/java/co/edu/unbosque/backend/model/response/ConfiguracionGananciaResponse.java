package co.edu.unbosque.backend.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response para configuración de ganancia mínima.
 */
public record ConfiguracionGananciaResponse(
        @Schema(description = "Porcentaje mínimo de ganancia sobre costo", example = "30.0")
        Double porcentajeMinimo
) {}
