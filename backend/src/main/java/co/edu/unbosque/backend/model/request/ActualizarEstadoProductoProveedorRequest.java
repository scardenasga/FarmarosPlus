package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para actualizar el estado de una relación proveedor-producto.
 */
public record ActualizarEstadoProductoProveedorRequest(
        @NotBlank(message = "El estado de la relación es obligatorio")
        @Pattern(
                regexp = "ACTIVO|INACTIVO",
                message = "El estado de la relación debe ser ACTIVO o INACTIVO"
        )
        String estado
) {
}
