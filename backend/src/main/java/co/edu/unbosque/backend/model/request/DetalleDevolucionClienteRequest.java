package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Línea de detalle para una devolución de cliente.
 *
 * @param idDetalleVenta identificador del detalle de la venta original
 * @param cantidad unidades a devolver
 */
public record DetalleDevolucionClienteRequest(
        @NotNull(message = "El detalle de venta es obligatorio")
        Long idDetalleVenta,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        int cantidad
) {
}
