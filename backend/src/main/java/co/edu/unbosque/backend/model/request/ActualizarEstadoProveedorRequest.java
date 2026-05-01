package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar estado de un proveedor.
 */
public record ActualizarEstadoProveedorRequest(
        @NotNull(message = "El estado es obligatorio")
        String estado
) {
}
