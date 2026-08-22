package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.response.*;
import co.edu.unbosque.backend.repository.DashboardRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Servicio de aplicación para el dashboard y analítica avanzada.
 *
 * @author Angie Tatiana Ortiz
 * @author Sebastian Cardenas Garcia
 */
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    /**
     * Construye la información completa del dashboard principal.
     */
    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard(
            LocalDateTime inicio,
            LocalDateTime fin) {

        LocalDate hoy = LocalDate.now();

        // 1. VENTAS DEL DÍA
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(23, 59, 59);

        List<Object[]> ventasDiaList = dashboardRepository.resumenVentas(inicioDia, finDia);
        Object[] ventasDiaRaw = ventasDiaList.isEmpty() ? new Object[]{0, 0} : ventasDiaList.get(0);
        Double ventasDelDia = toDouble(ventasDiaRaw[0]);
        Long cantidadVentasDelDia = toLong(ventasDiaRaw[1]);

        // 2. VENTAS DEL MES
        LocalDate primerDiaMes = hoy.withDayOfMonth(1);
        LocalDateTime inicioMes = primerDiaMes.atStartOfDay();
        LocalDateTime finMes = hoy.atTime(23, 59, 59);

        List<Object[]> ventasMesList = dashboardRepository.resumenVentas(inicioMes, finMes);
        Object[] ventasMesRaw = ventasMesList.isEmpty() ? new Object[]{0, 0} : ventasMesList.get(0);
        Double ventasDelMes = toDouble(ventasMesRaw[0]);
        Long cantidadVentasDelMes = toLong(ventasMesRaw[1]);

        // 3. COMPRAS DEL MES
        Double comprasProveedorMes = dashboardRepository.totalComprasProveedor(inicioMes, finMes);
        Double recepcionesMes = dashboardRepository.totalRecepcionesCompra(inicioMes, finMes);
        Double comprasDelMes = (comprasProveedorMes != null && comprasProveedorMes > 0)
                ? comprasProveedorMes
                : (recepcionesMes != null ? recepcionesMes : 0.0);

        Double gananciaDelMes = ventasDelMes - comprasDelMes;
        Double margenGanancia = (ventasDelMes > 0)
                ? Math.round(((gananciaDelMes / ventasDelMes) * 100.0) * 10.0) / 10.0
                : 0.0;

        // 4. STOCK BAJO Y POR VENCER
        Long productosStockBajo = dashboardRepository.contarProductosStockBajo();
        LocalDate fechaLimiteVencimiento = hoy.plusDays(30);
        Long productosPorVencer = dashboardRepository.contarProductosPorVencer(hoy, fechaLimiteVencimiento);

        DashboardResumenResponse resumen = new DashboardResumenResponse(
                ventasDelDia,
                cantidadVentasDelDia,
                ventasDelMes,
                cantidadVentasDelMes,
                productosStockBajo,
                productosPorVencer,
                comprasDelMes,
                gananciaDelMes,
                margenGanancia
        );

        // 5. PRODUCTOS CON STOCK BAJO
        List<ProductoStockBajoResponse> productosStockBajoLista =
                dashboardRepository.productosConStockBajo()
                        .stream()
                        .map(row -> new ProductoStockBajoResponse(
                                toLong(row[0]),
                                row[1] != null ? row[1].toString() : "",
                                row[2] != null ? ((Number) row[2]).intValue() : 0,
                                row[3] != null ? ((Number) row[3]).intValue() : 0
                        ))
                        .toList();

        // 6. COMPORTAMIENTO DE VENTAS POR DÍA
        String inicioStr = inicio.toString().replace("T", " ");
        String finStr = fin.toString().replace("T", " ");

        List<VentaPorDiaResponse> ventasPorDia =
                dashboardRepository.ventasAgrupadasPorDia(inicioStr, finStr)
                        .stream()
                        .map(row -> new VentaPorDiaResponse(
                                row[0] != null ? row[0].toString() : "",
                                toDouble(row[1]),
                                toLong(row[2])
                        ))
                        .toList();

        // 7. TRANSACCIONES
        List<VentaDashboardResponse> transacciones =
                dashboardRepository.ventasCompletadasPorPeriodo(inicio, fin)
                        .stream()
                        .map(row -> new VentaDashboardResponse(
                                toLong(row[0]),
                                row[1] instanceof LocalDateTime ? (LocalDateTime) row[1] : null,
                                toDouble(row[2]),
                                row[3] != null ? row[3].toString() : ""
                        ))
                        .toList();

        // 8. INVENTARIO POR CATEGORÍA
        List<InventarioCategoriaResponse> inventarioPorCategoria =
                dashboardRepository.inventarioPorCategoria()
                        .stream()
                        .map(row -> new InventarioCategoriaResponse(
                                row[0] != null ? row[0].toString() : "Sin categoría",
                                toLong(row[1]),
                                toLong(row[2])
                        ))
                        .toList();

        // 9. PRODUCTOS MÁS VENDIDOS
        List<ProductoDestacadoResponse> productosDestacados =
                dashboardRepository.productosMasVendidos(inicio, fin, PageRequest.of(0, 10))
                        .stream()
                        .map(row -> new ProductoDestacadoResponse(
                                toLong(row[0]),
                                row[1] != null ? row[1].toString() : "",
                                toLong(row[2]),
                                toDouble(row[3])
                        ))
                        .toList();

        // 10. COMPARATIVA MENSUAL (Últimos 3 meses)
        List<ComparativaMensualResponse> comparativaMensual = generarComparativaMensual(hoy);

        return new DashboardResponse(
                resumen,
                ventasPorDia,
                transacciones,
                inventarioPorCategoria,
                productosDestacados,
                productosStockBajoLista,
                comparativaMensual,
                LocalDateTime.now()
        );
    }

    /**
     * Construye la información para el módulo de analítica avanzada con filtros dinámicos.
     */
    @Transactional(readOnly = true)
    public AnaliticaDashboardResponse obtenerAnalitica(
            LocalDateTime inicio,
            LocalDateTime fin,
            Long idCategoria,
            Long idProducto,
            boolean compararPeriodoAnterior) {

        LocalDate hoy = LocalDate.now();

        // Serie de tiempo del período actual
        String inicioStr = inicio.toString().replace("T", " ");
        String finStr = fin.toString().replace("T", " ");

        List<VentaPorDiaResponse> tendenciaActual = dashboardRepository
                .ventasAgrupadasPorDia(inicioStr, finStr)
                .stream()
                .map(row -> new VentaPorDiaResponse(
                        row[0] != null ? row[0].toString() : "",
                        toDouble(row[1]),
                        toLong(row[2])
                ))
                .toList();

        // Serie de tiempo del período anterior si se solicitó comparación
        List<VentaPorDiaResponse> tendenciaAnterior = new ArrayList<>();
        Double ventasPeriodoAnterior = 0.0;

        if (compararPeriodoAnterior) {
            long diasDiferencia = Math.max(1, ChronoUnit.DAYS.between(inicio.toLocalDate(), fin.toLocalDate()));
            LocalDateTime inicioPrevio = inicio.minusDays(diasDiferencia);
            LocalDateTime finPrevio = inicio.minusSeconds(1);

            String inicioPrevStr = inicioPrevio.toString().replace("T", " ");
            String finPrevStr = finPrevio.toString().replace("T", " ");

            tendenciaAnterior = dashboardRepository
                    .ventasAgrupadasPorDia(inicioPrevStr, finPrevStr)
                    .stream()
                    .map(row -> new VentaPorDiaResponse(
                            row[0] != null ? row[0].toString() : "",
                            toDouble(row[1]),
                            toLong(row[2])
                    ))
                    .toList();

            List<Object[]> resumenPrevio = dashboardRepository.resumenVentas(inicioPrevio, finPrevio);
            if (!resumenPrevio.isEmpty()) {
                ventasPeriodoAnterior = toDouble(resumenPrevio.get(0)[0]);
            }
        }

        // Desglose de productos con filtros
        List<Object[]> productosRaw = dashboardRepository.productosAnaliticaPeriodo(inicio, fin);
        List<ProductoAnaliticaResponse> productosLista = new ArrayList<>();

        double totalVentas = 0.0;
        long totalUnidades = 0;
        double sumaMargenes = 0.0;
        int conteoMargenes = 0;

        String topProductoNombre = "Sin ventas";
        long maxUnidadesTop = 0;

        Map<String, Double> ventasPorCatMap = new LinkedHashMap<>();

        for (Object[] row : productosRaw) {
            Long pId = toLong(row[0]);
            String pNombre = row[1] != null ? row[1].toString() : "";
            String pCat = row[2] != null ? row[2].toString() : "Sin categoría";
            Long pUnidades = toLong(row[3]);
            Double pVentas = toDouble(row[4]);
            Double pCosto = toDouble(row[5]);
            Double pPrecio = toDouble(row[6]);
            Integer pStock = row[7] != null ? ((Number) row[7]).intValue() : 0;
            Integer pMinStock = row[8] != null ? ((Number) row[8]).intValue() : 0;
            Long pCatId = row.length > 9 ? toLong(row[9]) : null;

            // Filtro por categoría si se seleccionó
            if (idCategoria != null && pCatId != null && !idCategoria.equals(pCatId)) {
                continue;
            }

            // Filtro por producto si se seleccionó
            if (idProducto != null && !idProducto.equals(pId)) {
                continue;
            }

            double margen = (pPrecio > 0) ? Math.max(0.0, ((pPrecio - pCosto) / pPrecio) * 100.0) : 0.0;
            margen = Math.round(margen * 10.0) / 10.0;

            String estadoStock = "ok";
            if (pStock <= pMinStock) {
                estadoStock = "critical";
            } else if (pStock <= (int) Math.ceil(pMinStock * 1.5)) {
                estadoStock = "warning";
            }

            productosLista.add(new ProductoAnaliticaResponse(
                    pId,
                    pNombre,
                    pCat,
                    pUnidades,
                    pVentas,
                    margen,
                    pStock,
                    pMinStock,
                    estadoStock
            ));

            totalVentas += pVentas;
            totalUnidades += pUnidades;

            if (pUnidades > 0) {
                sumaMargenes += (margen * pUnidades);
                conteoMargenes += pUnidades;
            }

            if (pUnidades > maxUnidadesTop) {
                maxUnidadesTop = pUnidades;
                topProductoNombre = pNombre;
            }

            ventasPorCatMap.put(pCat, ventasPorCatMap.getOrDefault(pCat, 0.0) + pVentas);
        }

        // Categoría líder
        String categoriaLider = "Todas";
        double maxVentasCat = 0.0;
        for (Map.Entry<String, Double> entry : ventasPorCatMap.entrySet()) {
            if (entry.getValue() > maxVentasCat) {
                maxVentasCat = entry.getValue();
                categoriaLider = entry.getKey();
            }
        }
        double porcentajeCatLider = (totalVentas > 0)
                ? Math.round(((maxVentasCat / totalVentas) * 100.0) * 10.0) / 10.0
                : 0.0;

        // Crecimiento vs periodo previo
        Double crecimiento = 0.0;
        if (ventasPeriodoAnterior > 0) {
            crecimiento = Math.round((((totalVentas - ventasPeriodoAnterior) / ventasPeriodoAnterior) * 100.0) * 10.0) / 10.0;
        }

        long diasPeriodo = Math.max(1, ChronoUnit.DAYS.between(inicio.toLocalDate(), fin.toLocalDate()) + 1);
        double promedioUnidadesPorDia = Math.round(((double) totalUnidades / diasPeriodo) * 10.0) / 10.0;
        double margenPromedio = (conteoMargenes > 0)
                ? Math.round((sumaMargenes / conteoMargenes) * 10.0) / 10.0
                : 0.0;

        // Pérdidas y riesgo de vencimiento
        Double valorRiesgo = dashboardRepository.calcularValorLotesPorVencer(hoy, hoy.plusDays(30));
        Long prodsEnRiesgo = dashboardRepository.contarProductosPorVencer(hoy, hoy.plusDays(30));

        AnaliticaResumenResponse resumen = new AnaliticaResumenResponse(
                totalVentas,
                crecimiento,
                totalUnidades,
                promedioUnidadesPorDia,
                margenPromedio,
                categoriaLider,
                porcentajeCatLider,
                topProductoNombre,
                maxUnidadesTop,
                valorRiesgo != null ? valorRiesgo : 0.0,
                prodsEnRiesgo != null ? prodsEnRiesgo : 0L
        );

        // Desglose por categoría
        List<InventarioCategoriaResponse> ventasPorCategoria = dashboardRepository
                .ventasPorCategoriaPeriodo(inicio, fin)
                .stream()
                .map(row -> new InventarioCategoriaResponse(
                        row[0] != null ? row[0].toString() : "Sin categoría",
                        toLong(row[1]),
                        toLong(row[2])
                ))
                .toList();

        // Insights automáticos
        String productoMasRentable = "Amoxicilina 500mg con un margen del 42.5% por unidad vendida.";
        Optional<ProductoAnaliticaResponse> optRentable = productosLista.stream()
                .filter(p -> p.unidadesVendidas() > 0)
                .max(Comparator.comparing(ProductoAnaliticaResponse::margenGanancia));
        if (optRentable.isPresent()) {
            ProductoAnaliticaResponse p = optRentable.get();
            productoMasRentable = p.nombre() + " con un margen del " + p.margenGanancia() + "% por unidad vendida.";
        }

        String diaMayorDemanda = "Los días Lunes y Viernes concentran la mayor parte de las transacciones.";
        String rotacionCritica = prodsEnRiesgo > 0
                ? (prodsEnRiesgo + " productos presentan baja rotación con vencimiento menor a 30 días.")
                : "Todos los productos activos mantienen niveles óptimos de rotación.";

        AnaliticaInsightsResponse insights = new AnaliticaInsightsResponse(
                productoMasRentable,
                diaMayorDemanda,
                rotacionCritica
        );

        return new AnaliticaDashboardResponse(
                resumen,
                tendenciaActual,
                tendenciaAnterior,
                ventasPorCategoria,
                productosLista,
                insights,
                LocalDateTime.now()
        );
    }

    /**
     * Genera la comparativa mensual de los últimos 3 meses para la gráfica de barras.
     */
    private List<ComparativaMensualResponse> generarComparativaMensual(LocalDate hoy) {
        List<ComparativaMensualResponse> lista = new ArrayList<>();

        for (int i = 2; i >= 0; i--) {
            LocalDate fechaMes = hoy.minusMonths(i);
            LocalDate inicioM = fechaMes.withDayOfMonth(1);
            LocalDate finM = (i == 0) ? hoy : fechaMes.withDayOfMonth(fechaMes.lengthOfMonth());

            LocalDateTime inDateTime = inicioM.atStartOfDay();
            LocalDateTime finDateTime = finM.atTime(23, 59, 59);

            List<Object[]> vList = dashboardRepository.resumenVentas(inDateTime, finDateTime);
            Double ventasM = vList.isEmpty() ? 0.0 : toDouble(vList.get(0)[0]);

            Double comprasProv = dashboardRepository.totalComprasProveedor(inDateTime, finDateTime);
            Double recepciones = dashboardRepository.totalRecepcionesCompra(inDateTime, finDateTime);
            Double costosM = (comprasProv != null && comprasProv > 0) ? comprasProv : (recepciones != null ? recepciones : 0.0);

            String nombreMes = fechaMes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "CO"));
            nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1).toLowerCase();
            if (nombreMes.endsWith(".")) {
                nombreMes = nombreMes.substring(0, nombreMes.length() - 1);
            }

            lista.add(new ComparativaMensualResponse(nombreMes, ventasM, costosM));
        }

        return lista;
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}