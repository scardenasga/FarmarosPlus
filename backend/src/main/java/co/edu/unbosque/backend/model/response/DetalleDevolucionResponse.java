package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para el detalle de una devolución a proveedor.
 *
 * @author juanjo2748
 */
public record DetalleDevolucionResponse(
        Long idProducto,
        String nombreProducto,
        String numeroLote,
        int cantidad
) {
}
