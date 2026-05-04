package co.edu.unbosque.backend.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Solicitud para registrar una compra recibida de proveedor.
 *
 * @param idProveedor        identificador del proveedor
 * @param usuarioResponsable username del usuario que registra
 * @param numeroFactura      número de factura del proveedor (opcional)
 * @param notas              observaciones adicionales (opcional)
 * @param detalles           productos y cantidades recibidos
 * @author juanjo2748
 */
public record RegistrarCompraRequest(
        @NotNull(message = "El proveedor es obligatorio")
        Long idProveedor,

        @NotBlank(message = "El usuario responsable es obligatorio")
        String usuarioResponsable,

        String numeroFactura,

        String notas,

        @NotEmpty(message = "La compra debe tener al menos un producto")
        @Valid
        List<DetalleCompraRequest> detalles
) {
}
