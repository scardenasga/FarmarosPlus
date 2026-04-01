package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Comando de entrada para crear un producto.
 *
 * @param categoriaId id de la categoria opcional
 * @param nombre nombre del producto
 * @param descripcion descripcion opcional
 * @param codigoBarras codigo de barras del producto
 * @param stockMinimo stock minimo opcional
 * @param stockInicial stock inicial opcional
 * @param costo costo del producto
 * @param precioVenta precio de venta del producto
 * @param estado estado funcional opcional
 * @param numeroLote numero de lote opcional
 * @author Sebastian Cardenas Garcia
 */
public record CrearProductoRequest(
        @Schema(description = "Id de la categoria. Es opcional.")
        Long categoriaId,
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Schema(description = "Nombre comercial del producto", example = "Acetaminofen 500mg")
        String nombre,
        @Schema(description = "Descripcion opcional del producto", example = "Caja por 20 tabletas")
        String descripcion,
        @NotBlank(message = "El codigo de barras es obligatorio")
        @Schema(description = "Codigo de barras unico del producto", example = "7701234567890")
        String codigoBarras,
        @Schema(description = "Stock minimo recomendado", example = "10")
        @jakarta.validation.constraints.PositiveOrZero(message = "El stock minimo no puede ser negativo")
        Integer stockMinimo,
        @NotNull(message = "El stock inicial es obligatorio")
        @Positive(message = "El stock inicial debe ser mayor a cero")
        @Schema(description = "Stock inicial del producto. Debe ser mayor a cero para registrar el movimiento inicial.", example = "20")
        Integer stockInicial,
        @NotNull(message = "El costo es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
        @Schema(description = "Costo de compra del producto", example = "8500.0")
        Double costo,
        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio de venta no puede ser negativo")
        @Schema(description = "Precio de venta del producto", example = "12000.0")
        Double precioVenta,
        @Pattern(
                regexp = "^$|ACTIVO|INACTIVO|DESCONTINUADO",
                message = "El estado debe ser ACTIVO, INACTIVO o DESCONTINUADO"
        )
        @Schema(description = "Estado funcional del producto", example = "ACTIVO")
        String estado,
        @Schema(description = "Numero de lote opcional. Si se envia, tambien se crea un lote inicial.", example = "AMX-2026-01")
        String numeroLote
) {
}
