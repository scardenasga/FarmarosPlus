package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Comando de entrada para crear un usuario interno del sistema.
 *
 * @param username nombre de usuario único
 * @param passwordHash contraseña en texto plano o valor a transformar por el backend
 * @param nombreCompleto nombre completo del usuario
 * @param rol rol operativo
 * @param estado estado funcional opcional
 * @author Sebastian Cardenas Garcia
 */
public record CrearUsuarioRequest(
        @NotBlank(message = "El username es obligatorio")
        String username,
        @NotBlank(message = "El passwordHash es obligatorio")
        String passwordHash,
        @NotBlank(message = "El nombreCompleto es obligatorio")
        String nombreCompleto,
        @NotBlank(message = "El rol es obligatorio")
        @Pattern(
                regexp = "ADMIN|REGENTE|VENDEDOR|ALMACENISTA",
                message = "El rol debe ser ADMIN, REGENTE, VENDEDOR o ALMACENISTA"
        )
        String rol,
        @Pattern(
                regexp = "^$|ACTIVO|INACTIVO",
                message = "El estado debe ser ACTIVO o INACTIVO"
        )
        String estado
) {
}
