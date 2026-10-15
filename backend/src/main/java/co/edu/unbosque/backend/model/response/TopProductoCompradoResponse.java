package co.edu.unbosque.backend.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Producto con mayor gasto en compras dentro de un periodo.
 */
@Schema(description = "Producto mas comprado en un periodo")
public record TopProductoCompradoResponse(
        String nombre,
        long unidadesCompradas,
        Double montoTotal
) {
}
