package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para la comparativa mensual de ventas y costos.
 *
 * @author Sebastian Cardenas Garcia
 */
public record ComparativaMensualResponse(
        String mes,
        Double ventas,
        Double costos
) {
}
