package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Comando de entrada para un pago asociado a una venta.
 *
 * @param tipo tipo de pago
 * @param monto monto pagado
 * @author Sebastian Cardenas Garcia
 */
public record PagoVentaRequest(
        @NotBlank(message = "El tipo de pago es obligatorio")
        String tipo,
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero")
        Double monto
) {
}
