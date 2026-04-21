package co.edu.unbosque.backend.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Comando de entrada para registrar una devolución a proveedor.
 *
 * @param idProveedor identificador del proveedor al que se devuelve
 * @param motivo      razón general de la devolución
 * @param detalles    lista de productos y cantidades a devolver
 * @author juanjo2748
 */
public record DevolucionProveedorRequest(
        @NotNull(message = "El idProveedor es obligatorio")
        Long idProveedor,

        @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
        String motivo,

        @NotEmpty(message = "Debe incluir al menos un producto en la devolución")
        @Valid
        List<DetalleDevolucionRequest> detalles
) {
    /**
     * Línea de detalle dentro de una devolución.
     *
     * @param idProducto identificador del producto a devolver
     * @param idLote     identificador del lote (opcional)
     * @param cantidad   unidades a devolver
     * @param motivo     razón específica de esta línea
     */
    public record DetalleDevolucionRequest(
            @NotNull(message = "El idProducto es obligatorio")
            Long idProducto,

            Long idLote,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            Integer cantidad,

            @Size(max = 200, message = "El motivo del detalle no puede superar 200 caracteres")
            String motivo
    ) {
    }
}
