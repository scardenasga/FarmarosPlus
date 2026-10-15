package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para mostrar una transacción en el dashboard administrativo.
 *
 * @param idVenta identificador de la venta
 * @param fecha fecha y hora en que se realizó la venta
 * @param total valor total de la venta
 * @param estado estado actual de la venta
 * @author Angie Tatiana Ortiz
 */
public record VentaDashboardResponse(
        Long idVenta,
        LocalDateTime fecha,
        Double total,
        String estado
) {
}