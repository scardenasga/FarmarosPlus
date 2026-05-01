package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO para actualizar el estado de pago de una recepción de compra.
 */
public record ActualizarEstadoPagoRequest(
        @NotBlank(message = "El estado de pago es obligatorio")
        String estadoPago,

        @PositiveOrZero(message = "El monto pagado no puede ser negativo")
        Double montoPagado
) {
}
