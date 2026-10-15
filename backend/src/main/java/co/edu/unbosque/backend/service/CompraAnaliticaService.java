package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.response.CumplimientoProveedorResponse;
import co.edu.unbosque.backend.model.response.TopProductoCompradoResponse;
import co.edu.unbosque.backend.repository.DetalleRecepcionCompraRepository;
import co.edu.unbosque.backend.repository.RecepcionCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Analitica del proceso de compras: top de productos comprados y
 * cumplimiento de tiempos de entrega por proveedor.
 */
@Service
public class CompraAnaliticaService {

    private static final int MAX_TOP_PRODUCTOS = 8;

    private final DetalleRecepcionCompraRepository detalleRecepcionRepository;
    private final RecepcionCompraRepository recepcionRepository;

    public CompraAnaliticaService(
            DetalleRecepcionCompraRepository detalleRecepcionRepository,
            RecepcionCompraRepository recepcionRepository
    ) {
        this.detalleRecepcionRepository = detalleRecepcionRepository;
        this.recepcionRepository = recepcionRepository;
    }

    /**
     * Top de productos con mayor gasto en compras dentro de un periodo.
     *
     * @param inicio inicio inclusivo
     * @param fin fin inclusivo
     * @return hasta 8 productos ordenados por monto descendente
     */
    @Transactional(readOnly = true)
    public List<TopProductoCompradoResponse> topProductosComprados(LocalDateTime inicio, LocalDateTime fin) {
        return detalleRecepcionRepository.topProductosComprados(inicio, fin).stream()
                .limit(MAX_TOP_PRODUCTOS)
                .map(fila -> new TopProductoCompradoResponse(
                        (String) fila[0],
                        fila[1] != null ? ((Number) fila[1]).longValue() : 0L,
                        fila[2] != null ? ((Number) fila[2]).doubleValue() : 0.0
                ))
                .toList();
    }

    /**
     * Porcentaje de recepciones entregadas a tiempo (en o antes de la fecha
     * esperada de su orden) agrupado por proveedor.
     */
    @Transactional(readOnly = true)
    public List<CumplimientoProveedorResponse> cumplimientoProveedores() {
        return recepcionRepository.cumplimientoPorProveedor().stream()
                .map(fila -> {
                    String proveedor = (String) fila[0];
                    long total = fila[1] != null ? ((Number) fila[1]).longValue() : 0L;
                    long aTiempo = fila[2] != null ? ((Number) fila[2]).longValue() : 0L;
                    return new CumplimientoProveedorResponse(
                            proveedor,
                            total,
                            aTiempo,
                            porcentaje(aTiempo, total)
                    );
                })
                .sorted(Comparator.comparingDouble(CumplimientoProveedorResponse::porcentajeCumplimiento).reversed())
                .toList();
    }

    /** Porcentaje de cumplimiento; sin recepciones devuelve 0%. */
    public static double porcentaje(long parte, long total) {
        if (total <= 0) return 0.0;
        return (parte / (double) total) * 100.0;
    }
}
