package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para crear o reactivar la relación entre proveedor y producto.
 */
public record AsociarProductoProveedorRequest(
        @NotNull(message = "El id del producto es obligatorio")
        Long productoId,

        String codigoProductoProveedor,

        @DecimalMin(value = "0.0", inclusive = true, message = "El precio de referencia no puede ser negativo")
        Double precioReferencia
) {
}
