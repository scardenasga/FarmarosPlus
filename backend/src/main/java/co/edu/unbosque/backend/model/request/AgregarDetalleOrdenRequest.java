package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para agregar un detalle a una orden de compra.
 */
public record AgregarDetalleOrdenRequest(
        @NotNull(message = "El id del producto es obligatorio")
        Long productoId,

        String nombreProducto,

        String descripcionProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidadPedida,

        @NotNull(message = "El precio unitario es obligatorio")
        @Positive(message = "El precio unitario debe ser mayor a cero")
        Double precioUnitarioPactado
) {
}
