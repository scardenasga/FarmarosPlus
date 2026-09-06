package co.edu.unbosque.backend.recomendacion.api;

/**
 * Item de recomendacion individual.
 */
public record RecomendacionItemResponse(
        Long productoId,
        String nombre,
        double soporte,
        double confianza,
        double lift,
        int frecuenciaConjunta
) {}
