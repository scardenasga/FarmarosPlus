package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para detalle de recepción de compra.
 */
public record DetalleRecepcionCompraResponse(
        Long idDetalleRecepcion,
        Long productoId,
        String nombreProducto,
        Integer cantidadRecibida,
        Double costoUnitarioReal,
        Double subtotal,
        String observaciones
) {
}
