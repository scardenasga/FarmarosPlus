package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Comando de entrada para cambiar el estado de un usuario.
 *
 * @param estado nuevo estado del usuario
 * @author Sebastian Cardenas Garcia
 */
public record ActualizarEstadoUsuarioRequest(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(
                regexp = "ACTIVO|INACTIVO",
                message = "El estado debe ser ACTIVO o INACTIVO"
        )
        String estado
) {
}
