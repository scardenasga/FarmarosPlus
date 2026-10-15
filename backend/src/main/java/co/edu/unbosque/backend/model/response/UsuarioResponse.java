package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida detallado para usuarios.
 *
 * @param id id del usuario
 * @param username nombre de usuario
 * @param nombreCompleto nombre completo
 * @param rol rol operativo
 * @param estado estado funcional
 * @param ultimoAcceso fecha del último acceso registrado
 * @author Sebastian Cardenas Garcia
 */
public record UsuarioResponse(
        Long id,
        String username,
        String nombreCompleto,
        String rol,
        String estado,
        LocalDateTime ultimoAcceso
) {
}
