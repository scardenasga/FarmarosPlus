package co.edu.unbosque.backend.model.request;

import java.time.LocalDateTime;

/**
 * Parámetros para filtrar el histórico de ventas
 * 
 * @author Angie Tatiana Ortiz
 */

public record HistoricoFiltroRequest(
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    Long idVendedor,
    String estado
    
)
{}