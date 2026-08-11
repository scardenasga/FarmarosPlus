package co.edu.unbosque.backend.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Solicitud para registrar una devolución de productos a proveedor.
 *
 * @param idProveedor         identificador del proveedor
 * @param usuarioResponsable  username del usuario que registra la devolución
 * @param motivo              razón de la devolución (opcional)
 * @param detalles            productos y cantidades a devolver
 * @author juanjo2748
 */
public record RegistrarDevolucionRequest(
        @NotNull(message = "El proveedor es obligatorio")
        Long idProveedor,

        @NotBlank(message = "El usuario responsable es obligatorio")
        String usuarioResponsable,

        String motivo,

        @NotEmpty(message = "La devolución debe tener al menos un producto")
        @Valid
        List<DetalleDevolucionRequest> detalles
) {
}
