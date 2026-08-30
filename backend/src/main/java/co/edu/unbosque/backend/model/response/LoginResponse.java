package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para login exitoso.
 *
 * @param id id del usuario
 * @param username nombre de usuario
 * @param nombreCompleto nombre completo
 * @param rol rol operativo (ADMIN | VENDEDOR)
 * @param estado estado funcional
 * @param ultimoAcceso ultimo acceso actualizado
 */
public record LoginResponse(
        Long id,
        String username,
        String nombreCompleto,
        String rol,
        String estado,
        LocalDateTime ultimoAcceso
) {
}
