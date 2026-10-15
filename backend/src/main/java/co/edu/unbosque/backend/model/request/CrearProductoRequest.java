package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

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
 * @param porcentajeIva porcentaje de IVA aplicable al producto
 * @param requierePrescripcion indica si el producto requiere prescripción médica
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
        @PositiveOrZero(message = "El stock minimo no puede ser negativo")
        Integer stockMinimo,
        @NotNull(message = "El stock inicial es obligatorio")
        @Positive(message = "El stock inicial debe ser mayor a cero")
        @Schema(description = "Stock inicial del producto. Debe ser mayor a cero para registrar el movimiento inicial.", example = "20")
        Integer stockInicial,
        @NotNull(message = "El costo es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
        @Schema(description = "Costo de compra del producto", example = "8500.0")
        Double costo,
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio de venta no puede ser negativo")
        @Schema(description = "Precio de venta del producto. Si no se envía, se calcula automáticamente usando la ganancia mínima configurada en /api/configuracion/ganancia (sobre costo).", example = "12000.0")
        Double precioVenta,
        @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje de IVA no puede ser negativo")
        @Schema(description = "Porcentaje de IVA. Medicamentos: 0.0, Cosméticos/otros: 19.0", example = "0.0")
        Double porcentajeIva,
        @NotNull(message = "El campo requierePrescripcion es obligatorio")
        @Schema(description = "Indica si el producto requiere prescripcion medica", example = "false")
        Boolean requierePrescripcion,
        @FutureOrPresent(message = "La fecha de vencimiento no puede estar en el pasado")
        @Schema(description = "Fecha de vencimiento opcional del lote inicial. Solo requerida si el producto maneja lote; si se envia, se crea el lote aunque numeroLote sea null.", example = "2027-12-31")
        LocalDate fechaVencimiento,
        @Schema(description = "Numero de lote opcional. Si se envia, tambien se crea un lote inicial.", example = "AMX-2026-01")
        String numeroLote,
        @Pattern(regexp = "^$|UNIDAD|PRESENTACION|AMBAS", message = "unidadVenta debe ser UNIDAD, PRESENTACION o AMBAS")
        @Schema(description = "Modo de venta: UNIDAD (solo suelta), PRESENTACION (solo blister/caja), AMBAS (permite ambas). Ej. AMBAS con 10 unidades por blister.", example = "AMBAS")
        String unidadVenta,
        @Positive(message = "unidadesPorPresentacion debe ser mayor a cero")
        @Schema(description = "Cuántas unidades base contiene una presentación (ej. 10 por blister, 14 variable por producto). Requerido si unidadVenta es PRESENTACION o AMBAS.", example = "10")
        Integer unidadesPorPresentacion,
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio de presentación debe ser mayor a cero")
        @Schema(description = "Precio de la presentación completa (ej. precio del blister).", example = "22000.0")
        Double precioPresentacion
) {
}
