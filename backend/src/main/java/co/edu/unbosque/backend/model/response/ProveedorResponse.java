package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para proveedores.
 */
public record ProveedorResponse(
        Long idProveedor,
        String nombre,
        String nit,
        String telefono,
        String email,
        String contacto,
        String estado,
        String condicionPago,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
