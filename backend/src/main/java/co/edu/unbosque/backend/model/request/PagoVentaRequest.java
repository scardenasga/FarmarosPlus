package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Comando de entrada para un pago asociado a una venta.
 *
 * @param tipo   tipo de pago: EFECTIVO | TARJETA | TRANSFERENCIA
 * @param monto  monto pagado con este medio
 * @author Sebastian Cardenas Garcia
 */
public record PagoVentaRequest(
        @NotBlank(message = "El tipo de pago es obligatorio")
        @Pattern(
                regexp = "EFECTIVO|TARJETA|TRANSFERENCIA",
                message = "El tipo de pago debe ser EFECTIVO, TARJETA o TRANSFERENCIA"
        )
        String tipo,
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero")
        Double monto
) {
}
