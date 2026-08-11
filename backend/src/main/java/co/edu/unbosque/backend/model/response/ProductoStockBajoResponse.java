package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para productos con stock bajo.
 *
 * @param idProducto identificador del producto
 * @param nombre nombre del producto
 * @param stockActual cantidad disponible actualmente
 * @param stockMinimo cantidad mínima configurada
 *
 * @author Angie Tatiana Ortiz
 */
public record ProductoStockBajoResponse(
        Long idProducto,
        String nombre,
        Integer stockActual,
        Integer stockMinimo
) {
}