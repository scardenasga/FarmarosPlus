package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para líneas de una venta.
 *
 * @param id id del detalle
 * @param producto producto vendido
 * @param lote lote desde el cual se descontó inventario
 * @param cantidad cantidad vendida
 * @param precioUnitarioAplicado precio unitario aplicado
 * @param subtotalLinea subtotal de la línea
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
public record DetalleVentaResponse(
        Long id,
        ProductoResponse producto,
        LoteResumenResponse lote,
        Integer cantidad,
        Double precioUnitarioAplicado,
        Double subtotalLinea,
        Double ivaLinea
) {
}
