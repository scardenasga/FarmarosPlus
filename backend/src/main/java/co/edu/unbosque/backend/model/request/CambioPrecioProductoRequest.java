package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Comando de entrada para actualizar costo y precio de un producto.
 *
 * @param productoId producto a actualizar
 * @param nuevoCosto nuevo costo
 * @param nuevoPrecioVenta nuevo precio de venta
 * @param motivo motivo del cambio
 * @param usuarioResponsable usuario o actor responsable
 * @author Sebastian Cardenas Garcia
 */
public record CambioPrecioProductoRequest(
        @NotNull(message = "El productoId es obligatorio")
        Long productoId,
        @NotNull(message = "El nuevoCosto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo costo no puede ser negativo")
        Double nuevoCosto,
        @NotNull(message = "El nuevoPrecioVenta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo precio de venta no puede ser negativo")
        Double nuevoPrecioVenta,
        String motivo,
        @NotBlank(message = "El usuarioResponsable es obligatorio")
        String usuarioResponsable
) {
}
