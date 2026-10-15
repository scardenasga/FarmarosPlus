package co.edu.unbosque.backend.model.response;

import java.time.LocalDate;

/**
 * DTO de salida resumido para lotes embebidos en otras respuestas.
 *
 * @param id id del lote
 * @param numeroLote número del lote
 * @param fechaVencimiento fecha de vencimiento
 * @author Sebastian Cardenas Garcia
 */
public record LoteResumenResponse(
        Long id,
        String numeroLote,
        LocalDate fechaVencimiento
) {
}
