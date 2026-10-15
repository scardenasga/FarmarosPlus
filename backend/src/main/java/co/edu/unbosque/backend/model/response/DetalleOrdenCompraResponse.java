package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para detalle de orden de compra.
 * @author Angie Tatiana Ortiz
 */
public record DetalleOrdenCompraResponse(
        Long idDetalle,
        Long productoId,
        String nombreProducto,
        String descripcionProducto,
        Integer cantidadPedida,
        Double precioUnitarioPactado,
        Double subtotal
) {
}
