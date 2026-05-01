package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * DTO para crear una orden de compra.
 */
public record CrearOrdenCompraRequest(
        @NotNull(message = "El id del proveedor es obligatorio")
        Long proveedorId,

        LocalDateTime fechaEsperada,

        @Positive(message = "El total esperado debe ser mayor a cero")
        Double totalEsperado,

        String observaciones
) {
}
