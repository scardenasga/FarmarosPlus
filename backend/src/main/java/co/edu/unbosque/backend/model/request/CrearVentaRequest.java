package co.edu.unbosque.backend.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Comando de entrada para registrar una venta completa.
 *
 * @param usuarioId usuario que registra la venta
 * @param descuento descuento total aplicado
 * @param detalles líneas vendidas
 * @param pagos pagos registrados para la venta
 * @author Sebastian Cardenas Garcia
 */
public record CrearVentaRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        Long usuarioId,
        @DecimalMin(value = "0.0", inclusive = true, message = "El descuento no puede ser negativo")
        Double descuento,
        @NotEmpty(message = "La venta debe tener al menos un detalle")
        List<@Valid VentaDetalleRequest> detalles,
        @NotEmpty(message = "La venta debe tener al menos un pago")
        List<@Valid PagoVentaRequest> pagos
) {
}
