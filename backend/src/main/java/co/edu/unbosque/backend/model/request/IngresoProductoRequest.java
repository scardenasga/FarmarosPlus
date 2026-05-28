package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Comando de entrada para aumentar stock de un producto existente.
 *
 * @param cantidad cantidad a ingresar
 * @param numeroLote numero de lote opcional
 * @param nuevoCosto nuevo costo opcional
 * @param nuevoPrecioVenta nuevo precio de venta opcional
 * @param fechaVencimiento fecha de vencimiento obligatoria del lote asociado al ingreso
 * @author Sebastian Cardenas Garcia
 */
public record IngresoProductoRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Schema(description = "Cantidad a ingresar al inventario", example = "30")
        Integer cantidad,
        @NotBlank(message = "El numero de lote es obligatorio")
        @Schema(description = "Numero de lote obligatorio para el ingreso", example = "AMX-2026-02")
        String numeroLote,
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo costo no puede ser negativo")
        @Schema(description = "Nuevo costo opcional. Si cambia el precio, puede acompañar la actualizacion.", example = "16000.0")
        Double nuevoCosto,
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo precio de venta no puede ser negativo")
        @Schema(description = "Nuevo precio de venta opcional. Si cambia, se registra historial de precio.", example = "23500.0")
        Double nuevoPrecioVenta,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        @FutureOrPresent(message = "La fecha de vencimiento no puede estar en el pasado")
        @Schema(description = "Fecha de vencimiento obligatoria del lote asociado al ingreso.", example = "2027-12-31")
        LocalDate fechaVencimiento
) {
}
