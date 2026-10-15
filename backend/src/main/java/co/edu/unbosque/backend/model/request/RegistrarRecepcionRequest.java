package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar la recepción de una orden de compra.
 */
public record RegistrarRecepcionRequest(
        @NotNull(message = "El id de la orden es obligatorio")
        Long ordenId,

        String observaciones,

        @NotNull(message = "El estado de recepción es obligatorio")
        String estado,

        @NotNull(message = "El total de recepción es obligatorio")
        Double totalRecepcion
) {
}
