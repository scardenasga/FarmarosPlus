package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Comando de entrada para aumentar stock de un producto existente.
 *
 * @param cantidad cantidad a ingresar
 * @param numeroLote numero de lote opcional
 * @param nuevoCosto nuevo costo opcional
 * @param nuevoPrecioVenta nuevo precio de venta opcional
 * @author Sebastian Cardenas Garcia
 */
public record IngresoProductoRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Schema(description = "Cantidad a ingresar al inventario", example = "30")
        Integer cantidad,
        @Schema(description = "Numero de lote opcional para el ingreso", example = "AMX-2026-02")
        String numeroLote,
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo costo no puede ser negativo")
        @Schema(description = "Nuevo costo opcional. Si cambia el precio, puede acompañar la actualizacion.", example = "16000.0")
        Double nuevoCosto,
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo precio de venta no puede ser negativo")
        @Schema(description = "Nuevo precio de venta opcional. Si cambia, se registra historial de precio.", example = "23500.0")
        Double nuevoPrecioVenta
) {
}
