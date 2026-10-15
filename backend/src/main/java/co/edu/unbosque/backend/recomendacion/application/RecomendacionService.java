package co.edu.unbosque.backend.recomendacion.application;

import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.recomendacion.api.RecomendacionItemResponse;
import co.edu.unbosque.backend.recomendacion.api.RecomendacionResponse;
import co.edu.unbosque.backend.recomendacion.config.RecomendacionProperties;
import co.edu.unbosque.backend.recomendacion.domain.CanastaSnapshot;
import co.edu.unbosque.backend.recomendacion.domain.CanastaReader;
import co.edu.unbosque.backend.recomendacion.domain.ReglaAsociacion;
import co.edu.unbosque.backend.recomendacion.domain.RuleMiner;
import co.edu.unbosque.backend.repository.DetalleVentaRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Servicio de recomendaciones de canasta.
 * Cache en memoria con TTL y recalculo perezoso.
 */
@Service
public class RecomendacionService {

    private static final Logger log = LoggerFactory.getLogger(RecomendacionService.class);

    private final CanastaReader canastaReader;
    private final RuleMiner ruleMiner;
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final RecomendacionProperties props;

    // Cache
    private volatile CachedRules cachedRules = null;
    private final ReentrantLock recalcLock = new ReentrantLock();

    public RecomendacionService(CanastaReader canastaReader,
                                RuleMiner ruleMiner,
                                ProductoRepository productoRepository,
                                DetalleVentaRepository detalleVentaRepository,
                                RecomendacionProperties props) {
        this.canastaReader = canastaReader;
        this.ruleMiner = ruleMiner;
        this.productoRepository = productoRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.props = props;
    }

    private static class CachedRules {
        final List<ReglaAsociacion> reglas;
        final int totalVentas;
        final Instant actualizadoEn;
        final CanastaSnapshot snapshot;

        CachedRules(List<ReglaAsociacion> reglas, int totalVentas, Instant actualizadoEn, CanastaSnapshot snapshot) {
            this.reglas = reglas;
            this.totalVentas = totalVentas;
            this.actualizadoEn = actualizadoEn;
            this.snapshot = snapshot;
        }
    }

    /**
     * Obtiene recomendaciones para el carrito dado.
     *
     * @param productoIds IDs en carrito
     * @param limit maximo de recomendaciones (1..5)
     * @return respuesta con origen y lista filtrada
     */
    public RecomendacionResponse recomendar(Set<Long> productoIds, int limit) {
        if (productoIds == null || productoIds.isEmpty()) {
            // Carrito vacio: sin datos, no se consulta cache
            return new RecomendacionResponse("SIN_DATOS", LocalDateTime.now(), getTotalVentasCachedOrRead(), List.of());
        }
        // Normalizar limit
        int lim = Math.max(1, Math.min(limit, props.getLimiteMaximo()));

        CachedRules cache = obtenerCacheValida();

        if (cache == null || cache.reglas == null) {
            // Sin datos suficientes para reglas
            return fallbackPopularidad(productoIds, lim, cache);
        }

        // Filtrar reglas por carrito
        List<ReglaAsociacion> candidatas = filtrarPorCarrito(cache.reglas, productoIds);

        // Filtro adicional lift > minLift y confianza ya fue minada pero reforzar
        double minLift = props.getMinLift();
        candidatas = candidatas.stream()
                .filter(r -> r.lift() > minLift)
                .toList();

        if (candidatas.isEmpty()) {
            return fallbackPopularidad(productoIds, lim, cache);
        }

        // Resolver productos elegibles y ordenar
        List<RecomendacionItemResponse> items = resolverItemsOrdenados(candidatas, productoIds, lim);
        if (items.isEmpty()) {
            return fallbackPopularidad(productoIds, lim, cache);
        }

        String origen = "FPGROWTH";
        LocalDateTime actualizadoEn = LocalDateTime.ofInstant(cache.actualizadoEn, ZoneId.systemDefault());
        return new RecomendacionResponse(origen, actualizadoEn, cache.totalVentas, items);
    }

    /**
     * Fuerza recarga de cache (uso administrativo futuro).
     */
    public void recalcular() {
        recalcLock.lock();
        try {
            cachedRules = recalcularCache();
        } finally {
            recalcLock.unlock();
        }
    }

    private CachedRules obtenerCacheValida() {
        CachedRules local = cachedRules;
        long ttlMinutes = props.getCacheTtlMinutes();
        if (local != null) {
            Instant expiry = local.actualizadoEn.plusSeconds(ttlMinutes * 60);
            if (Instant.now().isBefore(expiry)) {
                return local;
            }
        }
        // TTL vencido o sin cache: intentar recalcular con bloqueo de un solo recalculo
        if (recalcLock.tryLock()) {
            try {
                // Doble chequeo
                CachedRules doubleCheck = cachedRules;
                if (doubleCheck != null) {
                    Instant expiry = doubleCheck.actualizadoEn.plusSeconds(ttlMinutes * 60);
                    if (Instant.now().isBefore(expiry)) {
                        return doubleCheck;
                    }
                }
                CachedRules nuevo = recalcularCache();
                cachedRules = nuevo;
                return nuevo;
            } finally {
                recalcLock.unlock();
            }
        } else {
            // Otra hebra recalcula: devolver ultimo valido aunque vencido, o null
            return local;
        }
    }

    private CachedRules recalcularCache() {
        try {
            CanastaSnapshot snapshot = canastaReader.leerCanastas();
            int total = snapshot.totalVentasCompletadas();
            if (total == 0 || snapshot.canastasMinables().isEmpty()) {
                log.info("Recomendaciones: sin canastas minables (totalVentas={})", total);
                return new CachedRules(List.of(), total, Instant.now(), snapshot);
            }
            int minSupportCount = Math.max(2, (int) Math.ceil(props.getMinSupportRelativo() * total));
            double minConf = props.getMinConfidence();
            List<ReglaAsociacion> reglas = ruleMiner.minar(snapshot.canastasMinables(), minSupportCount, minConf);
            // Filtro lift estricto
            reglas = reglas.stream().filter(r -> r.lift() > props.getMinLift()).toList();
            log.info("Recomendaciones cache recalculada: totalVentas={}, minSupport={}, reglas={}", total, minSupportCount, reglas.size());
            return new CachedRules(reglas, total, Instant.now(), snapshot);
        } catch (Exception e) {
            log.error("Error recalculando cache de recomendaciones", e);
            // No propagar, devolver vacio pero con timestamp
            CachedRules prev = cachedRules;
            int total = prev != null ? prev.totalVentas : 0;
            CanastaSnapshot emptySnapshot = new CanastaSnapshot(List.of(), List.of(), total);
            return new CachedRules(List.of(), total, Instant.now(), emptySnapshot);
        }
    }

    private List<ReglaAsociacion> filtrarPorCarrito(List<ReglaAsociacion> reglas, Set<Long> carrito) {
        Set<Long> carritoSet = new HashSet<>(carrito);
        // Mantener solo reglas donde antecedente subset de carrito y consecuente no en carrito
        Map<Long, ReglaAsociacion> mejorPorConsecuente = new HashMap<>();
        for (ReglaAsociacion r : reglas) {
            if (carritoSet.contains(r.consecuente())) {
                continue;
            }
            if (!carritoSet.containsAll(r.antecedente())) {
                continue;
            }
            // Si hay multiples reglas con mismo consecuente, quedarnos con la mejor (lift, confianza, soporte)
            ReglaAsociacion existente = mejorPorConsecuente.get(r.consecuente());
            if (existente == null || compararReglas(r, existente) < 0) {
                mejorPorConsecuente.put(r.consecuente(), r);
            }
        }
        return new ArrayList<>(mejorPorConsecuente.values());
    }

    private List<RecomendacionItemResponse> resolverItemsOrdenados(List<ReglaAsociacion> candidatas, Set<Long> carrito, int limit) {
        if (candidatas.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = new HashSet<>();
        for (ReglaAsociacion r : candidatas) {
            ids.add(r.consecuente());
        }
        // Cargar productos y filtrar elegibilidad
        Map<Long, Producto> productos = new HashMap<>();
        for (Producto p : productoRepository.findAllById(ids)) {
            productos.put(p.getUniqueID(), p);
        }
        List<ReglaEnriquecida> enriquecidas = new ArrayList<>();
        for (ReglaAsociacion r : candidatas) {
            Producto p = productos.get(r.consecuente());
            if (p == null) continue;
            if (!"ACTIVO".equalsIgnoreCase(p.getEstado())) continue;
            if (p.getStockActual() == null || p.getStockActual() <= 0) continue;
            if (props.isExcluirPrescripcion() && Boolean.TRUE.equals(p.getRequierePrescripcion())) continue;
            enriquecidas.add(new ReglaEnriquecida(r, p.getNombre()));
        }
        // Orden: lift desc, confianza desc, soporte desc, nombre asc
        enriquecidas.sort((a, b) -> compararReglas(a.regla, b.regla) != 0 ? compararReglas(a.regla, b.regla) : a.nombre.compareToIgnoreCase(b.nombre));

        List<RecomendacionItemResponse> res = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, enriquecidas.size()); i++) {
            ReglaEnriquecida e = enriquecidas.get(i);
            res.add(new RecomendacionItemResponse(
                    e.regla.consecuente(),
                    e.nombre,
                    e.regla.soporte(),
                    e.regla.confianza(),
                    e.regla.lift(),
                    e.regla.frecuenciaConjunta()
            ));
        }
        return res;
    }

    private int compararReglas(ReglaAsociacion a, ReglaAsociacion b) {
        int cmp = Double.compare(b.lift(), a.lift());
        if (cmp != 0) return cmp;
        cmp = Double.compare(b.confianza(), a.confianza());
        if (cmp != 0) return cmp;
        cmp = Double.compare(b.soporte(), a.soporte());
        if (cmp != 0) return cmp;
        return 0;
    }

    private record ReglaEnriquecida(ReglaAsociacion regla, String nombre) {}

    private RecomendacionResponse fallbackPopularidad(Set<Long> carrito, int limit, CachedRules cache) {
        int totalVentas = cache != null ? cache.totalVentas : getTotalVentasCachedOrRead();
        Instant ts = cache != null ? cache.actualizadoEn : Instant.now();
        LocalDateTime actualizadoEn = LocalDateTime.ofInstant(ts, ZoneId.systemDefault());

        // Si totalVentas ==0 => SIN_DATOS
        if (totalVentas == 0) {
            return new RecomendacionResponse("SIN_DATOS", actualizadoEn, totalVentas, List.of());
        }

        // Popularidad: productos mas vendidos excluyendo carrito y filtrando elegibilidad
        try {
            List<Object[]> filas = detalleVentaRepository.findPopularidadProducto();
            List<RecomendacionItemResponse> items = new ArrayList<>();
            Set<Long> carritoSet = new HashSet<>(carrito);
            for (Object[] fila : filas) {
                Long pid = (Long) fila[0];
                if (carritoSet.contains(pid)) continue;
                // resolver producto
                Producto p = productoRepository.findById(pid).orElse(null);
                if (p == null) continue;
                if (!"ACTIVO".equalsIgnoreCase(p.getEstado())) continue;
                if (p.getStockActual() == null || p.getStockActual() <= 0) continue;
                if (props.isExcluirPrescripcion() && Boolean.TRUE.equals(p.getRequierePrescripcion())) continue;

                // Calcular frecuencia conjunta dummy: unidades totales no es soporte, pero servira
                // Usamos 0 para soporte/confianza/lift en fallback
                items.add(new RecomendacionItemResponse(pid, p.getNombre(), 0.0, 0.0, 0.0, ((Number) fila[1]).intValue()));
                if (items.size() >= limit) break;
            }
            if (items.isEmpty()) {
                return new RecomendacionResponse("SIN_DATOS", actualizadoEn, totalVentas, List.of());
            }
            return new RecomendacionResponse("POPULARIDAD", actualizadoEn, totalVentas, items);
        } catch (Exception e) {
            log.error("Error en fallback popularidad", e);
            return new RecomendacionResponse("SIN_DATOS", actualizadoEn, totalVentas, List.of());
        }
    }

    private int getTotalVentasCachedOrRead() {
        if (cachedRules != null) {
            return cachedRules.totalVentas;
        }
        try {
            CanastaSnapshot snap = canastaReader.leerCanastas();
            return snap.totalVentasCompletadas();
        } catch (Exception e) {
            return 0;
        }
    }
}
