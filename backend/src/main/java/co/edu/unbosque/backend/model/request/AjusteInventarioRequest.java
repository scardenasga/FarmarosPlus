package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Comando de entrada para ajustar stock de un lote y su producto.
 *
 * @param loteId lote afectado
 * @param diferencia ajuste a aplicar; positivo para entrada, negativo para salida
 * @param tipoMovimiento tipo funcional del ajuste
 * @param motivo motivo del ajuste
 * @param referenciaDocumento referencia externa de soporte
 * @author Sebastian Cardenas Garcia
 */
public record AjusteInventarioRequest(
        @NotNull(message = "El loteId es obligatorio")
        Long loteId,
        @NotNull(message = "La diferencia es obligatoria")
        Integer diferencia,
        @NotBlank(message = "El tipoMovimiento es obligatorio")
        String tipoMovimiento,
        String motivo,
        String referenciaDocumento
) {
}
