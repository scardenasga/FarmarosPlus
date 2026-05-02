package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para productos.
 *
 * @param id id del producto
 * @param categoria categoría asociada
 * @param nombre nombre del producto
 * @param descripcion descripción del producto
 * @param codigoBarras código de barras
 * @param stockMinimo stock mínimo configurado
 * @param stockActual stock actual agregado
 * @param costo costo actual
 * @param precioVenta precio de venta actual
 * @param margenGanancia margen de ganancia calculado
 * @param estado estado funcional
 * @author Sebastian Cardenas Garcia
 */
public record ProductoResponse(
        Long id,
        CategoriaResponse categoria,
        String nombre,
        String descripcion,
        String codigoBarras,
        Integer stockMinimo,
        Integer stockActual,
        Double costo,
        Double precioVenta,
        Double margenGanancia,
        Double porcentajeIva,
        String estado
) {
}
