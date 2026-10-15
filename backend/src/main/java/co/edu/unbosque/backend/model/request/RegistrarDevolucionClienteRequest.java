package co.edu.unbosque.backend.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Solicitud para registrar una devolución de cliente.
 *
 * @param idVenta identificador de la venta original
 * @param usuarioResponsable username opcional del usuario que registra la devolución
 * @param nombreCliente nombre del cliente, si se conoce
 * @param documentoCliente documento o identificador del cliente, si se conoce
 * @param motivo razón de la devolución
 * @param detalles productos y cantidades devueltas
 */
public record RegistrarDevolucionClienteRequest(
        @NotNull(message = "La venta es obligatoria")
        Long idVenta,

        String usuarioResponsable,

        String nombreCliente,

        String documentoCliente,

        String motivo,

        @NotEmpty(message = "La devolución debe tener al menos un producto")
        @Valid
        List<DetalleDevolucionClienteRequest> detalles
) {
}
