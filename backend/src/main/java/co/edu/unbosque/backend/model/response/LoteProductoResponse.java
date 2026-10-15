package co.edu.unbosque.backend.model.response;

import java.time.LocalDate;

/**
 * DTO resumido de lote asociado a un producto.
 *
 * @param id id del lote
 * @param numeroLote numero funcional del lote
 * @param fechaVencimiento fecha de vencimiento del lote
 * @param cantidad cantidad disponible
 * @author Sebastian Cardenas Garcia
 */
public record LoteProductoResponse(
        Long id,
        String numeroLote,
        LocalDate fechaVencimiento,
        Integer cantidad
) {
}
