package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.Min;

/**
 * Solicitud para actualizar la configuración de alertas de inventario.
 *
 * @param diasProximoVencimiento días de anticipación para alertar sobre lotes próximos a vencer
 * @author juanjo2748
 */
public record ActualizarConfiguracionAlertaRequest(
        @Min(value = 1, message = "El umbral de vencimiento debe ser al menos 1 día")
        int diasProximoVencimiento
) {
}
