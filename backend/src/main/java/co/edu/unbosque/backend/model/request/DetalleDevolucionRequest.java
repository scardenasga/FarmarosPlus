package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Línea de detalle para una devolución a proveedor.
 *
 * @param idProducto identificador del producto
 * @param idLote     identificador del lote a devolver
 * @param cantidad   unidades a devolver
 * @author juanjo2748
 */
public record DetalleDevolucionRequest(
        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @NotNull(message = "El lote es obligatorio")
        Long idLote,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        int cantidad
) {
}
