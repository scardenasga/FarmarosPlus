package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Comando de entrada para registrar el ingreso de un nuevo lote al inventario.
 *
 * @param productoId producto al que pertenece el lote
 * @param numeroLote identificador funcional del lote
 * @param fechaVencimiento fecha de vencimiento del lote
 * @param cantidad cantidad inicial ingresada
 * @param motivo motivo o comentario del ingreso
 * @param referenciaDocumento referencia documental externa
 * @author Sebastian Cardenas Garcia
 */
public record IngresoLoteRequest(
        @NotNull(message = "El productoId es obligatorio")
        Long productoId,
        @NotBlank(message = "El numeroLote es obligatorio")
        String numeroLote,
        @FutureOrPresent(message = "La fecha de vencimiento no puede estar en el pasado")
        LocalDate fechaVencimiento,
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad,
        String motivo,
        String referenciaDocumento
) {
}
