package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida resumido para usuario.
 *
 * @param id id del usuario
 * @param username nombre de usuario
 * @param nombreCompleto nombre completo
 * @param rol rol operativo
 * @param estado estado funcional
 * @author Sebastian Cardenas Garcia
 */
public record UsuarioResumenResponse(
        Long id,
        String username,
        String nombreCompleto,
        String rol,
        String estado
) {
}
