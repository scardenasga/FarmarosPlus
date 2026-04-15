package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para respuestas de error de la API.
 *
 * @param timestamp fecha y hora del error
 * @param status código HTTP
 * @param error nombre corto del error
 * @param message mensaje detallado
 * @param path ruta solicitada
 * @author Sebastian Cardenas Garcia
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
