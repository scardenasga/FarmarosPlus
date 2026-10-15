package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para productos asociados a un proveedor.
 */
public record ProductoProveedorResponse(
        Long id,
        String nombre,
        String descripcion,
        String codigoBarras,
        String estado,
        String codigoProductoProveedor,
        Double precioReferencia,
        String estadoRelacion
) {
}
