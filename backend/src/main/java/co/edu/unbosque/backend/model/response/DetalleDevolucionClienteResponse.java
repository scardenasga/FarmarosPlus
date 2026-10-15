package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para el detalle de una devolución a cliente.
 */
public record DetalleDevolucionClienteResponse(
        Long idDetalleVenta,
        Long idProducto,
        String nombreProducto,
        String numeroLote,
        int cantidad
) {
}
