package co.edu.unbosque.backend.model.response;

import java.util.List;

/**
 * DTO de detalle extendido para un producto.
 *
 * @param id id del producto
 * @param categoria categoria asociada
 * @param nombre nombre del producto
 * @param descripcion descripcion del producto
 * @param codigoBarras codigo de barras
 * @param stockMinimo stock minimo configurado
 * @param stockActual stock actual agregado
 * @param costo costo actual
 * @param precioVenta precio de venta actual
 * @param margenGanancia margen de ganancia calculado
 * @param porcentajeIva porcentaje de IVA
 * @param requierePrescripcion indica si el producto requiere prescripcion medica
 * @param estado estado funcional
 * @param lotes lotes asociados al producto
 * @author Sebastian Cardenas Garcia
 */
public record ProductoDetalleResponse(
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
        Boolean requierePrescripcion,
        String estado,
        List<LoteProductoResponse> lotes
) {
}
