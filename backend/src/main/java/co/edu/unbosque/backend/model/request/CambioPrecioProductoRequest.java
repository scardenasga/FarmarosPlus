package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Comando de entrada para actualizar costo y precio de un producto.
 *
 * @param nuevoCosto nuevo costo
 * @param nuevoPrecioVenta nuevo precio de venta
 * @param motivo motivo opcional del cambio
 * @author Sebastian Cardenas Garcia
 */
public record CambioPrecioProductoRequest(
        @NotNull(message = "El nuevoCosto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo costo no puede ser negativo")
        @Schema(description = "Nuevo costo del producto", example = "9000.0")
        Double nuevoCosto,
        @NotNull(message = "El nuevoPrecioVenta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo precio de venta no puede ser negativo")
        @Schema(description = "Nuevo precio de venta del producto", example = "13000.0")
        Double nuevoPrecioVenta,
        @Schema(description = "Motivo opcional del cambio de precio", example = "Ajuste por proveedor")
        String motivo
) {
}
