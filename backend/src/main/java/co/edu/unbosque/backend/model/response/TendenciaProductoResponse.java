package co.edu.unbosque.backend.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tendencia de ventas de un producto: compara las unidades vendidas en el
 * periodo reciente contra el periodo inmediatamente anterior de igual duracion.
 *
 * @param idProducto identificador del producto
 * @param unidadesRecientes unidades vendidas en los ultimos N dias
 * @param unidadesPrevias unidades vendidas en los N dias anteriores
 * @param porcentajeCambio variacion porcentual (positivo = crece, negativo = cae)
 * @param diasComparacion tamanio del periodo usado para la comparacion
 */
@Schema(description = "Tendencia de ventas de un producto")
public record TendenciaProductoResponse(
        Long idProducto,
        long unidadesRecientes,
        long unidadesPrevias,
        Double porcentajeCambio,
        int diasComparacion
) {
}
