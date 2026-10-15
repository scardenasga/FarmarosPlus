package co.edu.unbosque.backend.recomendacion.domain;

import java.util.List;
import java.util.Set;

/**
 * Fotografia de canastas para mineria.
 *
 * @param canastas lista de conjuntos de idProducto distintos por venta (size >=1)
 * @param canastasMinables solo canastas con 2+ items, listas para FP-Growth
 * @param totalVentasCompletadas total de ventas con estado COMPLETADA
 */
public record CanastaSnapshot(
        List<Set<Long>> canastas,
        List<Set<Long>> canastasMinables,
        int totalVentasCompletadas
) {}
