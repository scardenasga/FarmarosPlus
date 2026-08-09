package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request para registrar la gestión de lotes (ingresos múltiples) en una sola operación.
 */
public record GestionLotesRequest(
        @NotEmpty(message = "La lista de ingresos no puede estar vacía")
        @Valid
        @Schema(description = "Lista de productos y lotes a gestionar")
        List<ItemGestionLote> items
) {
    public record ItemGestionLote(
            @NotNull(message = "El id del producto o código de barras es necesario")
            @Schema(description = "ID del producto o Código de Barras")
            String identificador,

            @NotNull(message = "La cantidad es obligatoria")
            Integer cantidad,

            @NotBlank(message = "El número de lote es obligatorio")
            String numeroLote,

            @NotNull(message = "La fecha de vencimiento es obligatoria")
            java.time.LocalDate fechaVencimiento,

            Double nuevoCosto,
            Double nuevoPrecioVenta
    ) {}
}
