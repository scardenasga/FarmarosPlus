package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para consultas GET de proveedores sin campos de auditoría.
 */
public record ProveedorConsultaResponse(
        Long idProveedor,
        String nombre,
        String nit,
        String telefono,
        String email,
        String contacto,
        String estado,
        String condicionPago
) {
}
