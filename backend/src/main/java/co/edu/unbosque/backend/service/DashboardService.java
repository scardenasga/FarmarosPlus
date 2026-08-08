package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.response.DashboardResponse;
import co.edu.unbosque.backend.model.response.DashboardResumenResponse;
import co.edu.unbosque.backend.model.response.InventarioCategoriaResponse;
import co.edu.unbosque.backend.model.response.ProductoDestacadoResponse;
import co.edu.unbosque.backend.model.response.ProductoStockBajoResponse;
import co.edu.unbosque.backend.model.response.VentaDashboardResponse;
import co.edu.unbosque.backend.model.response.VentaPorDiaResponse;
import co.edu.unbosque.backend.repository.DashboardRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicación para el dashboard.
 *
 * Obtiene y organiza la información necesaria para mostrar:
 * - Ventas del día
 * - Ventas del mes
 * - Productos con stock bajo
 * - Productos próximos a vencer
 * - Comportamiento de ventas
 * - Productos más vendidos
 *
 * @author Angie Tatiana Ortiz
 */
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    /**
     * Construye la información completa del dashboard.
     *
     * @param inicio inicio del período seleccionado
     * @param fin fin del período seleccionado
     * @return información completa del dashboard
     */
    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard(
            LocalDateTime inicio,
            LocalDateTime fin) {

        /*
         * ============================================================
         * 1. VENTAS DEL DÍA
         * ============================================================
         */

        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(23, 59, 59);

        List<Object[]> ventasDiaList =
                dashboardRepository.resumenVentas(
                        inicioDia,
                        finDia
                );

        Object[] ventasDiaRaw = ventasDiaList.isEmpty()
                ? new Object[]{0, 0}
                : ventasDiaList.get(0);

        Double ventasDelDia = toDouble(ventasDiaRaw[0]);
        Long cantidadVentasDelDia = toLong(ventasDiaRaw[1]);


        /*
         * ============================================================
         * 2. VENTAS DEL MES
         * ============================================================
         */

        LocalDate primerDiaMes = hoy.withDayOfMonth(1);

        LocalDateTime inicioMes =
                primerDiaMes.atStartOfDay();

        LocalDateTime finMes =
                hoy.atTime(23, 59, 59);

        List<Object[]> ventasMesList =
                dashboardRepository.resumenVentas(
                        inicioMes,
                        finMes
                );

        Object[] ventasMesRaw = ventasMesList.isEmpty()
                ? new Object[]{0, 0}
                : ventasMesList.get(0);

        Double ventasDelMes = toDouble(ventasMesRaw[0]);
        Long cantidadVentasDelMes = toLong(ventasMesRaw[1]);


        /*
         * ============================================================
         * 3. STOCK BAJO
         * ============================================================
         */

        Long productosStockBajo =
                dashboardRepository.contarProductosStockBajo();


        /*
         * ============================================================
         * 4. PRODUCTOS PRÓXIMOS A VENCER
         *
         * Se consideran próximos a vencer los productos que
         * tengan un lote disponible cuyo vencimiento esté dentro
         * de los próximos 30 días.
         * ============================================================
         */

        LocalDate fechaLimite =
                hoy.plusDays(30);

        Long productosPorVencer =
                dashboardRepository.contarProductosPorVencer(
                        hoy,
                        fechaLimite
                );


        /*
         * ============================================================
         * 5. RESUMEN PRINCIPAL DEL DASHBOARD
         * ============================================================
         */

        DashboardResumenResponse resumen =
                new DashboardResumenResponse(
                        ventasDelDia,
                        cantidadVentasDelDia,
                        ventasDelMes,
                        cantidadVentasDelMes,
                        productosStockBajo,
                        productosPorVencer
                );


        /*
         * ============================================================
         * 6. PRODUCTOS CON STOCK BAJO
         *
         * Esta lista permite que posteriormente el frontend pueda
         * mostrar cuáles son los productos que requieren reposición.
         * ============================================================
         */

        List<ProductoStockBajoResponse> productosStockBajoLista =
                dashboardRepository.productosConStockBajo()
                        .stream()
                        .map(row -> new ProductoStockBajoResponse(
                                toLong(row[0]),
                                row[1] != null
                                        ? row[1].toString()
                                        : "",
                                row[2] != null
                                        ? ((Number) row[2]).intValue()
                                        : 0,
                                row[3] != null
                                        ? ((Number) row[3]).intValue()
                                        : 0
                        ))
                        .toList();


        /*
         * ============================================================
         * 7. COMPORTAMIENTO DE VENTAS
         *
         * Utiliza el período seleccionado desde el frontend.
         *
         * Por ejemplo:
         * - 7 días
         * - 30 días
         * - 90 días
         * - período personalizado
         * ============================================================
         */

        String inicioStr =
                inicio.toString().replace("T", " ");

        String finStr =
                fin.toString().replace("T", " ");

        List<VentaPorDiaResponse> ventasPorDia =
                dashboardRepository
                        .ventasAgrupadasPorDia(
                                inicioStr,
                                finStr
                        )
                        .stream()
                        .map(row -> new VentaPorDiaResponse(
                                row[0] != null
                                        ? row[0].toString()
                                        : "",
                                toDouble(row[1]),
                                toLong(row[2])
                        ))
                        .toList();


        /*
         * ============================================================
         * 8. TRANSACCIONES
         *
         * Se mantienen disponibles por si posteriormente
         * queremos mostrarlas desde el dashboard.
         * ============================================================
         */

        List<VentaDashboardResponse> transacciones =
                dashboardRepository
                        .ventasCompletadasPorPeriodo(
                                inicio,
                                fin
                        )
                        .stream()
                        .map(row -> new VentaDashboardResponse(
                                toLong(row[0]),
                                row[1] instanceof LocalDateTime
                                        ? (LocalDateTime) row[1]
                                        : null,
                                toDouble(row[2]),
                                row[3] != null
                                        ? row[3].toString()
                                        : ""
                        ))
                        .toList();


        /*
         * ============================================================
         * 9. INVENTARIO POR CATEGORÍA
         *
         * Se mantiene disponible para análisis del inventario.
         * ============================================================
         */

        List<InventarioCategoriaResponse> inventarioPorCategoria =
                dashboardRepository
                        .inventarioPorCategoria()
                        .stream()
                        .map(row -> new InventarioCategoriaResponse(
                                row[0] != null
                                        ? row[0].toString()
                                        : "Sin categoría",
                                toLong(row[1]),
                                toLong(row[2])
                        ))
                        .toList();


        /*
         * ============================================================
         * 10. PRODUCTOS MÁS VENDIDOS
         *
         * Se obtienen los 10 productos con mayor cantidad vendida
         * durante el período seleccionado.
         * ============================================================
         */

        List<ProductoDestacadoResponse> productosDestacados =
                dashboardRepository
                        .productosMasVendidos(
                                inicio,
                                fin,
                                PageRequest.of(0, 10)
                        )
                        .stream()
                        .map(row -> new ProductoDestacadoResponse(
                                toLong(row[0]),
                                row[1] != null
                                        ? row[1].toString()
                                        : "",
                                toLong(row[2]),
                                toDouble(row[3])
                        ))
                        .toList();


        /*
         * ============================================================
         * 11. RESPUESTA FINAL
         * ============================================================
         */

        return new DashboardResponse(
                resumen,
                ventasPorDia,
                transacciones,
                inventarioPorCategoria,
                productosDestacados,
                productosStockBajoLista,
                LocalDateTime.now()
        );
    }


    /**
     * Convierte un objeto a Double.
     *
     * @param value valor recibido
     * @return valor convertido o 0.0
     */
    private Double toDouble(Object value) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


    /**
     * Convierte un objeto a Long.
     *
     * @param value valor recibido
     * @return valor convertido o 0
     */
    private Long toLong(Object value) {

        if (value == null) {
            return 0L;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}