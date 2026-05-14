package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Comando de entrada para actualizar un producto.
 *
 * @param categoriaId categoria opcional
 * @param nombre nombre opcional del producto
 * @param descripcion descripcion opcional
 * @param stockMinimo stock minimo opcional
 * @param stockActual stock actual opcional para correccion manual
 * @param costo costo opcional
 * @param precioVenta precio de venta opcional
 * @param porcentajeIva porcentaje de IVA opcional
 * @param requierePrescripcion indica si el producto requiere prescripcion medica
 * @param estado estado funcional opcional
 * @author Sebastian Cardenas Garcia
 */
public record ActualizarProductoRequest(
        @Schema(description = "Id de la categoria. Si no se envia, la categoria no cambia.")
        Long categoriaId,
        @Schema(description = "Nombre comercial del producto", example = "Acetaminofen 500mg")
        String nombre,
        @Schema(description = "Descripcion opcional del producto", example = "Caja por 20 tabletas")
        String descripcion,
        @PositiveOrZero(message = "El stock minimo no puede ser negativo")
        @Schema(description = "Stock minimo recomendado", example = "10")
        Integer stockMinimo,
        @PositiveOrZero(message = "El stock actual no puede ser negativo")
        @Schema(description = "Stock actual del producto. Se usa solo para correcciones manuales.", example = "120")
        Integer stockActual,
        @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
        @Schema(description = "Costo de compra del producto", example = "8500.0")
        Double costo,
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio de venta no puede ser negativo")
        @Schema(description = "Precio de venta del producto", example = "12000.0")
        Double precioVenta,
        @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje de IVA no puede ser negativo")
        @Schema(description = "Porcentaje de IVA", example = "0.0")
        Double porcentajeIva,
        @Schema(description = "Indica si el producto requiere prescripcion medica", example = "false")
        Boolean requierePrescripcion,
        @Pattern(
                regexp = "^$|ACTIVO|INACTIVO|DESCONTINUADO",
                message = "El estado debe ser ACTIVO, INACTIVO o DESCONTINUADO"
        )
        @Schema(description = "Estado funcional del producto", example = "ACTIVO")
        String estado
) {
}
