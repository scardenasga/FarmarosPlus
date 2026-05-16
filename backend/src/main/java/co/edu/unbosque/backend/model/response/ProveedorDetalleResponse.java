package co.edu.unbosque.backend.model.response;

import java.util.List;

/**
 * DTO de detalle de proveedor con productos asociados.
 */
public record ProveedorDetalleResponse(
        Long idProveedor,
        String nombre,
        String nit,
        String telefono,
        String email,
        String contacto,
        String estado,
        String condicionPago,
        List<ProductoProveedorResponse> productos
) {
}
