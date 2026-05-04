package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para alertas del sistema.
 *
 * @param id              identificador de la alerta
 * @param tipo            STOCK_MINIMO o PROXIMO_VENCIMIENTO
 * @param titulo          título corto de la alerta
 * @param mensaje         detalle de la alerta
 * @param leida           si el usuario ya la revisó
 * @param fechaGeneracion cuándo se generó
 * @author juanjo2748
 */
public record AlertaResponse(
        Long id,
        String tipo,
        String titulo,
        String mensaje,
        boolean leida,
        LocalDateTime fechaGeneracion
) {
}
