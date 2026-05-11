package co.edu.unbosque.backend.model.response;

import java.util.List;

/**
 * @author Angie Tatiana Ortiz
 */
public record OrdenCompraPreviewResponse(
    Long proveedorId,
    String proveedorNombre,
    List<ItemPreview> items,
    Double totalEstimado
) {
    public record ItemPreview(
        Long productoId,
        String nombre,
        Integer cantidadSugerida,
        Double precioUnitario,
        Double subtotal,
        String motivo 
    ) {}
}