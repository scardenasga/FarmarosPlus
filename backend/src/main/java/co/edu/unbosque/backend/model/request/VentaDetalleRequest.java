package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Comando de entrada para una línea de venta.
 *
 * @param productoId producto vendido
 * @param loteId lote desde el cual se descuenta el inventario
 * @param cantidad cantidad vendida
 * @param precioUnitario precio aplicado; si es nulo se toma el precio actual del producto
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
public record VentaDetalleRequest(
        @NotNull(message = "El productoId es obligatorio")
        Long productoId,
        Long loteId,
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad,
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario no puede ser negativo")
        Double precioUnitario
) {
}
