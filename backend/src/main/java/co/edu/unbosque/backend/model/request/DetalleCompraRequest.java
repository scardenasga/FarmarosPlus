package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Línea de un producto dentro de una compra a proveedor.
 *
 * @param idProducto     id del producto comprado
 * @param cantidad       unidades recibidas
 * @param precioUnitario precio de costo unitario pagado
 * @author juanjo2748
 */
public record DetalleCompraRequest(
        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @NotNull @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad,

        @NotNull @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        Double precioUnitario
) {
}
