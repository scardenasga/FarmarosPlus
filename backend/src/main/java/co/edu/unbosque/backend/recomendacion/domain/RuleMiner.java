package co.edu.unbosque.backend.recomendacion.domain;

import java.util.List;
import java.util.Set;

/**
 * Puerto para mineria de reglas de asociacion.
 */
public interface RuleMiner {

    /**
     * Mina reglas a partir de transacciones.
     *
     * @param transacciones lista de conjuntos de idProducto (cada venta)
     * @param minSupportCount soporte absoluto minimo (>=1)
     * @param minConfidence confianza minima (0..1)
     * @return reglas de dominio filtradas y crudas
     */
    List<ReglaAsociacion> minar(List<Set<Long>> transacciones, int minSupportCount, double minConfidence);
}
