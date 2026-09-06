package co.edu.unbosque.backend.recomendacion.infrastructure.jpa;

import co.edu.unbosque.backend.recomendacion.domain.CanastaReader;
import co.edu.unbosque.backend.recomendacion.domain.CanastaSnapshot;
import co.edu.unbosque.backend.repository.DetalleVentaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lee canastas desde detalle_venta agrupando por venta.
 * Solo lectura, ventas COMPLETADA.
 */
@Component
public class JpaCanastaReader implements CanastaReader {

    private final DetalleVentaRepository detalleVentaRepository;

    public JpaCanastaReader(DetalleVentaRepository detalleVentaRepository) {
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CanastaSnapshot leerCanastas() {
        List<Object[]> filas = detalleVentaRepository.findCanastaProjection();
        Map<Long, Set<Long>> agrupadas = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            Long idVenta = (Long) fila[0];
            Long idProducto = (Long) fila[1];
            agrupadas.computeIfAbsent(idVenta, k -> new LinkedHashSet<>()).add(idProducto);
        }
        List<Set<Long>> canastas = new ArrayList<>(agrupadas.values());
        List<Set<Long>> minables = canastas.stream()
                .filter(c -> c.size() >= 2)
                .toList();
        // total ventas completadas: distintas ventas
        int total = agrupadas.size();
        // Si hay ventas sin detalle? No deberia, pero por consistencia contar ventas distintas
        // Si no hay filas, total =0
        return new CanastaSnapshot(canastas, minables, total);
    }
}
