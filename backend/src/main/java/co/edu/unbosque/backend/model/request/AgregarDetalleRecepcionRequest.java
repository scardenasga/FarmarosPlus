package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para agregar un detalle a una recepción de compra.
 */
public record AgregarDetalleRecepcionRequest(
        @NotNull(message = "El id del detalle de orden es obligatorio")
        Long detalleOrdenId,

        Long productoId,

        @NotNull(message = "La cantidad recibida es obligatoria")
        @Positive(message = "La cantidad recibida debe ser mayor a cero")
        Integer cantidadRecibida,

        Double costoUnitarioReal,

        String observaciones
) {
}
