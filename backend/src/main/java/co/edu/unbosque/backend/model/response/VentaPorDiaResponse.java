package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para una entrada de la serie temporal de ventas.
 * Representa el total y cantidad de ventas agrupadasp por dia.
 * 
 * @author Angie Tatiana Ortiz
 */

public record VentaPorDiaResponse(
    String fecha,
    Double total,
    Long cantidad

) {
    
}