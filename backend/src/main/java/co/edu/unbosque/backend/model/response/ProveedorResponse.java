package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para proveedores.
 *
 * @author juanjo2748
 */
public record ProveedorResponse(
        Long id,
        String nombre,
        String nit,
        String contacto,
        String telefono,
        String email,
        String estado
) {
}
