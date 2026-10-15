package co.edu.unbosque.backend.recomendacion.infrastructure.smile;

import co.edu.unbosque.backend.recomendacion.domain.ReglaAsociacion;
import co.edu.unbosque.backend.recomendacion.domain.RuleMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import smile.association.ARM;
import smile.association.AssociationRule;
import smile.association.FPTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adaptador SMILE para FP-Growth + ARM.
 * Unica clase que importa smile.*.
 */
@Component
public class SmileFpGrowthRuleMiner implements RuleMiner {

    private static final Logger log = LoggerFactory.getLogger(SmileFpGrowthRuleMiner.class);

    @Override
    public List<ReglaAsociacion> minar(List<Set<Long>> transacciones, int minSupportCount, double minConfidence) {
        if (transacciones == null || transacciones.isEmpty()) {
            return List.of();
        }
        if (minSupportCount < 1) {
            throw new IllegalArgumentException("minSupportCount debe ser >=1");
        }
        // Filtrar transacciones vacias y deduplicar items por transaccion
        List<Set<Long>> limpias = transacciones.stream()
                .filter(c -> c != null && !c.isEmpty())
                .map(c -> (Set<Long>) new LinkedHashSet<>(c))
                .toList();
        if (limpias.isEmpty()) {
            return List.of();
        }

        // Mapeo determinista Long <-> int denso [0,n)
        Map<Long, Integer> productoToItem = new HashMap<>();
        Map<Integer, Long> itemToProducto = new HashMap<>();
        int nextId = 0;
        // Ordenar productos para mapeo determinista
        Set<Long> todos = new HashSet<>();
        for (Set<Long> c : limpias) {
            todos.addAll(c);
        }
        List<Long> ordenados = new ArrayList<>(todos);
        Collections.sort(ordenados);
        for (Long pid : ordenados) {
            productoToItem.put(pid, nextId);
            itemToProducto.put(nextId, pid);
            nextId++;
        }
        if (nextId == 0) {
            return List.of();
        }

        // Construir int[][] con ids densos
        int[][] itemsets = new int[limpias.size()][];
        for (int i = 0; i < limpias.size(); i++) {
            Set<Long> canasta = limpias.get(i);
            int[] arr = new int[canasta.size()];
            int idx = 0;
            for (Long pid : canasta) {
                arr[idx++] = productoToItem.get(pid);
            }
            itemsets[i] = arr;
        }

        int totalTransacciones = itemsets.length;
        try {
            FPTree tree = FPTree.of(minSupportCount, itemsets);
            List<AssociationRule> rules = ARM.apply(minConfidence, tree).toList();

            List<ReglaAsociacion> resultado = new ArrayList<>();
            for (AssociationRule r : rules) {
                // Solo consecuente de exactamente un producto (accionable por chip)
                if (r.consequent().length != 1) {
                    continue;
                }
                Set<Long> antecedente = new LinkedHashSet<>();
                for (int item : r.antecedent()) {
                    Long pid = itemToProducto.get(item);
                    if (pid != null) {
                        antecedente.add(pid);
                    }
                }
                Long consecuente = itemToProducto.get(r.consequent()[0]);
                if (consecuente == null) {
                    continue;
                }
                // Evitar reglas donde antecedente contiene consecuente (no deberia pasar)
                if (antecedente.contains(consecuente)) {
                    continue;
                }
                int frecuenciaConjunta = (int) Math.round(r.support() * totalTransacciones);
                ReglaAsociacion regla = new ReglaAsociacion(
                        antecedente,
                        consecuente,
                        r.support(),
                        r.confidence(),
                        r.lift(),
                        r.leverage(),
                        frecuenciaConjunta
                );
                resultado.add(regla);
            }
            log.debug("SMILE minado: {} transacciones, {} reglas crudas, {} reglas con consecuente unico", totalTransacciones, rules.size(), resultado.size());
            return resultado;
        } catch (IllegalArgumentException e) {
            // Ej: Empty stream of itemsets cuando no hay items frecuentes
            log.warn("SMILE minado sin reglas: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Error inesperado en SMILE RuleMiner", e);
            return List.of();
        }
    }
}
