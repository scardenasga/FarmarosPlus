package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para una línea de detalle de compra a proveedor.
 *
 * @author juanjo2748
 */
public record DetalleCompraResponse(
        Long idProducto,
        String nombreProducto,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {
}
