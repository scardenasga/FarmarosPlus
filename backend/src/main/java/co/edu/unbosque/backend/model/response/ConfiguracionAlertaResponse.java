package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para la configuración de alertas.
 *
 * @param diasProximoVencimiento días de anticipación configurados
 * @author juanjo2748
 */
public record ConfiguracionAlertaResponse(
        int diasProximoVencimiento
) {
}
