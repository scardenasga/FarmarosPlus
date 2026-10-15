package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para el stock de inventario agrupado por categoria.
 * Se usa en la gráfica del dashboard.
 * 
 * @author Angie Tatiana Ortiz
 */

public record InventarioCategoriaResponse(
    String categoria, 
    Long stockTotal,
    Long cantidadProductos
    
) {

}