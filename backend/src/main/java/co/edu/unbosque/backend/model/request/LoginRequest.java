package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Comando de entrada para autenticación por username y contraseña.
 *
 * @param username nombre de usuario
 * @param password contraseña en texto plano
 * @author Sebastian Cardenas Garcia
 */
public record LoginRequest(
        @NotBlank(message = "El username es obligatorio")
        String username,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
