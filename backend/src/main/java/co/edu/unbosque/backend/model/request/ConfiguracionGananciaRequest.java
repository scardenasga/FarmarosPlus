package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request para actualizar la ganancia mínima por defecto.
 */
public record ConfiguracionGananciaRequest(
        @NotNull(message = "El porcentaje mínimo es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje no puede ser negativo")
        @Schema(description = "Porcentaje mínimo de ganancia sobre costo (ej. 30.0 = 30%)", example = "30.0")
        Double porcentajeMinimo
) {}
