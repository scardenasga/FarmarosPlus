package co.edu.unbosque.backend.recomendacion.domain;

import java.util.Set;

/**
 * Regla de asociacion de dominio, desacoplada de SMILE.
 *
 * @param antecedente conjunto de productos que activan la regla
 * @param consecuente producto sugerido (unico)
 * @param soporte proporcion de ventas que contienen antecedente U consecuente (0..1)
 * @param confianza supp(A U B)/supp(A) (0..1)
 * @param lift confianza / soporte(B)
 * @param leverage soporte(A U B) - soporte(A)*soporte(B)
 * @param frecuenciaConjunta soporte absoluto = ventas con A y B
 */
public record ReglaAsociacion(
        Set<Long> antecedente,
        Long consecuente,
        double soporte,
        double confianza,
        double lift,
        double leverage,
        int frecuenciaConjunta
) {}
