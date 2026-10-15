package co.edu.unbosque.backend.model.response;

import java.time.LocalDate;

/**
 * DTO de salida para lotes de inventario.
 *
 * @param id id del lote
 * @param numeroLote código o número funcional
 * @param fechaVencimiento fecha de vencimiento
 * @param cantidad cantidad disponible
 * @param producto producto asociado
 * @author Sebastian Cardenas Garcia
 */
public record LoteResponse(
        Long id,
        String numeroLote,
        LocalDate fechaVencimiento,
        Integer cantidad,
        ProductoResponse producto
) {
}
