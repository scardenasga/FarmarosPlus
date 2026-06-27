package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.response.DashboardResumenResponse;
import co.edu.unbosque.backend.model.response.DashboardResponse;
import co.edu.unbosque.backend.model.response.InventarioCategoriaResponse;
import co.edu.unbosque.backend.model.response.ProductoDestacadoResponse;
import co.edu.unbosque.backend.model.response.VentaPorDiaResponse;
import co.edu.unbosque.backend.repository.DashboardRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicacion para el dashboard administrativo.
 * Agrega indicadores de ventas, inventario y productos destacados
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
     * Construye el dashboard completo para un rango de fechas dado.
     *
     * @param inicio inicio del periodo
     * @param fin    fin del periodo
     * @return respuesta agregada con KPIs, series temporales e inventario
     */
    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard(LocalDateTime inicio, LocalDateTime fin) {

        // Resumen de ventas
        List<Object[]> resumenList = dashboardRepository.resumenVentas(inicio, fin);
        Object[] resumenRaw = resumenList.isEmpty() ? new Object[]{0, 0} : resumenList.get(0);
        Double totalVentas = toDouble(resumenRaw[0]);
        Long cantidadVentas = toLong(resumenRaw[1]);
        Double ticketPromedio = cantidadVentas > 0 ? totalVentas / cantidadVentas : 0.0;

        DashboardResumenResponse resumen = new DashboardResumenResponse(
                totalVentas,
                cantidadVentas,
                ticketPromedio,
                dashboardRepository.contarProductosActivos(),
                dashboardRepository.contarProductosStockBajo(),
                toDouble(dashboardRepository.calcularValorInventario())
        );

        // Ventas agrupadas por dia 
        String inicioStr = inicio.toString().replace("T", " ");
        String finStr = fin.toString().replace("T", " ");

        List<VentaPorDiaResponse> ventasPorDia = dashboardRepository
                .ventasAgrupadasPorDia(inicioStr, finStr)
                .stream()
                .map(row -> new VentaPorDiaResponse(
                        row[0] != null ? row[0].toString() : "",
                        toDouble(row[1]),
                        toLong(row[2])
                ))
                .toList();

        // Inventario por categoria
        List<InventarioCategoriaResponse> inventarioPorCategoria = dashboardRepository
                .inventarioPorCategoria()
                .stream()
                .map(row -> new InventarioCategoriaResponse(
                        row[0] != null ? row[0].toString() : "Sin categoria",
                        toLong(row[1]),
                        toLong(row[2])
                ))
                .toList();

        // Top 10 productos mas vendidos
        List<ProductoDestacadoResponse> productosDestacados = dashboardRepository
                .productosMasVendidos(inicio, fin, PageRequest.of(0, 10))
                .stream()
                .map(row -> new ProductoDestacadoResponse(
                        toLong(row[0]),
                        row[1] != null ? row[1].toString() : "",
                        toLong(row[2]),
                        toDouble(row[3])
                ))
                .toList();

        return new DashboardResponse(
                resumen,
                ventasPorDia,
                inventarioPorCategoria,
                productosDestacados,
                LocalDateTime.now()
        );
    }

    /**
     * Convierte un objeto a Double de forma segura.
     *
     * @param value objeto a convertir
     * @return valor double o 0.0 si es nulo
     */
    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    /**
     * Convierte un objeto a Long de forma segura.
     *
     * @param value objeto a convertir
     * @return valor long o 0 si es nulo
     */
    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); }
        catch (NumberFormatException e) { return 0L; }
    }
}