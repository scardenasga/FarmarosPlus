package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para los productos mas vendidos en el periodo seleccionado.
 * Se usa en la gráfica de barras horizontales del dashboard.
 * 
 * @author Angie Tatiana Ortiz
 */

public record ProductoDestacadoResponse(
    Long idProducto,
    String nombre, 
    Long cantidadVendida,
    Double totalVendido

) {
    
}
