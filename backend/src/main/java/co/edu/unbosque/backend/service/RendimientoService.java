package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.entity.CierreCaja;
import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.PagoVenta;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.repository.CierreCajaRepository;
import co.edu.unbosque.backend.repository.VentaRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Servicio para reporte de rendimiento / funcionamiento.
 * 100% in-app, offline, sin dependencias externas nuevas.
 * Usa datos ya persistidos (Venta, CierreCaja) por lo que no introduce
 * overhead en el flujo POS. El cálculo se hace on-demand al pulsar el botón
 * en Configuración.
 */
@Service
public class RendimientoService {

    private static final String NOMBRE_FARMACIA = "Droguería Farmarosita";
    private static final Color COLOR_HEADER = new Color(41, 128, 185);
    private static final Color COLOR_FILA_PAR = new Color(235, 245, 251);
    private static final Color COLOR_SECCION = new Color(236, 240, 241);
    private static final Color COLOR_OK = new Color(39, 174, 96);
    private static final Color COLOR_WARN = new Color(211, 84, 0);

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentaRepository ventaRepository;
    private final CierreCajaRepository cierreCajaRepository;

    public RendimientoService(VentaRepository ventaRepository, CierreCajaRepository cierreCajaRepository) {
        this.ventaRepository = ventaRepository;
        this.cierreCajaRepository = cierreCajaRepository;
    }

    public record ProductoVendido(String nombre, int unidades, double ingresos) {}

    public record ResumenRendimiento(
            LocalDate desde,
            LocalDate hasta,
            int totalVentas,
            int completadas,
            int anuladas,
            double porcentajeAnuladas,
            int unidadesVendidas,
            double totalIngresos,
            double ticketPromedio,
            double ivaRecaudado,
            double descuentosOtorgados,
            double gananciaEstimada,
            double promedioDiario,
            LocalDate mejorDia,
            double mejorDiaIngresos,
            LocalDate peorDia,
            double peorDiaIngresos,
            double tendenciaPorcentaje,
            Map<LocalDate, Double> ingresosPorDia,
            Map<LocalDate, Integer> transaccionesPorDia,
            Map<Integer, Integer> ventasPorHora,
            double promedioMinutosEntreVentas,
            double ventasPorDiaPromedio,
            // Medios de pago
            double totalEfectivo,
            double totalTarjeta,
            double totalTransferencia,
            // Productos
            List<ProductoVendido> topProductos,
            List<ProductoVendido> menosVendidos,
            Map<String, int[]> unidadesPorProductoRaw,
            Map<String, double[]> ingresosPorProductoRaw,
            // Cierre de caja
            int cantidadCierres,
            double diferenciaPromedio,
            double diferenciaMax,
            double diferenciaMin,
            long cierresCuadrados,
            long cierresConSobrante,
            long cierresConFaltante,
            double porcentajeCuadre,
            List<CierreCaja> cierresDetalle
    ) {}

    @Transactional(readOnly = true)
    public ResumenRendimiento calcularResumen(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fin = hasta != null ? hasta.atTime(LocalTime.of(23, 59, 59)) : null;

        List<Venta> ventas = ventaRepository.findHistorico(inicio, fin, null, null);
        List<Venta> completadasList = ventas.stream().filter(v -> "COMPLETADA".equalsIgnoreCase(v.getEstado())).toList();
        long anuladas = ventas.stream().filter(v -> "ANULADA".equalsIgnoreCase(v.getEstado())).count();

        double totalIngresos = 0;
        double ivaRecaudado = 0;
        double descuentos = 0;
        int unidades = 0;
        double ganancia = 0;
        double totalEfectivo = 0, totalTarjeta = 0, totalTransferencia = 0;
        Map<LocalDate, Double> porDia = new TreeMap<>();
        Map<LocalDate, Integer> transPorDia = new TreeMap<>();
        Map<Integer, Integer> porHora = new TreeMap<>();
        Map<String, int[]> unidadesPorProducto = new LinkedHashMap<>();
        Map<String, double[]> ingresosPorProducto = new LinkedHashMap<>();

        // Ordenar por fecha para calcular tiempo entre ventas
        List<Venta> ordenadas = new ArrayList<>(completadasList);
        ordenadas.sort(Comparator.comparing(Venta::getFecha, Comparator.nullsLast(Comparator.naturalOrder())));

        for (Venta v : completadasList) {
            totalIngresos += v.getTotal() != null ? v.getTotal() : 0.0;
            descuentos += v.getDescuento() != null ? v.getDescuento() : 0.0;
            if (v.getPagos() != null) {
                for (PagoVenta p : v.getPagos()) {
                    if ("EFECTIVO".equalsIgnoreCase(p.getTipo())) totalEfectivo += p.getMonto() != null ? p.getMonto() : 0;
                    else if ("TARJETA".equalsIgnoreCase(p.getTipo())) totalTarjeta += p.getMonto() != null ? p.getMonto() : 0;
                    else if ("TRANSFERENCIA".equalsIgnoreCase(p.getTipo())) totalTransferencia += p.getMonto() != null ? p.getMonto() : 0;
                }
            }
            if (v.getDetalles() != null) {
                for (DetalleVenta d : v.getDetalles()) {
                    int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                    unidades += cant;
                    ivaRecaudado += d.getIvaLinea() != null ? d.getIvaLinea() : 0.0;
                    double precio = d.getPrecioUnitarioAplicado() != null ? d.getPrecioUnitarioAplicado() : 0.0;
                    double costo = d.getProducto() != null && d.getProducto().getCosto() != null ? d.getProducto().getCosto() : 0.0;
                    ganancia += (precio - costo) * cant;
                    Producto prod = d.getProducto();
                    String nombre = prod != null && prod.getNombre() != null ? prod.getNombre() : "Producto";
                    unidadesPorProducto.computeIfAbsent(nombre, k -> new int[]{0})[0] += cant;
                    ingresosPorProducto.computeIfAbsent(nombre, k -> new double[]{0.0})[0] += d.getSubtotalLinea() != null ? d.getSubtotalLinea() : 0.0;
                }
            }
            LocalDate dia = v.getFecha() != null ? v.getFecha().toLocalDate() : null;
            if (dia != null) {
                porDia.merge(dia, v.getTotal() != null ? v.getTotal() : 0.0, Double::sum);
                transPorDia.merge(dia, 1, Integer::sum);
            }
            if (v.getFecha() != null) {
                int hora = v.getFecha().getHour();
                porHora.merge(hora, 1, Integer::sum);
            }
        }

        // Top / menos vendidos
        List<ProductoVendido> productos = new ArrayList<>();
        for (String nombre : unidadesPorProducto.keySet()) {
            productos.add(new ProductoVendido(nombre, unidadesPorProducto.get(nombre)[0], ingresosPorProducto.get(nombre)[0]));
        }
        productos.sort(Comparator.comparingInt(ProductoVendido::unidades).reversed().thenComparing(Comparator.comparingDouble(ProductoVendido::ingresos).reversed()));
        List<ProductoVendido> top = productos.stream().limit(5).toList();
        List<ProductoVendido> menos = new ArrayList<>(productos.stream()
                .sorted(Comparator.comparingInt(ProductoVendido::unidades).thenComparing(Comparator.comparingDouble(ProductoVendido::ingresos)))
                .limit(5).toList());
        menos.sort(Comparator.comparingInt(ProductoVendido::unidades));

        double ticket = completadasList.isEmpty() ? 0 : totalIngresos / completadasList.size();
        double porcAnuladas = ventas.isEmpty() ? 0 : (anuladas * 100.0 / ventas.size());
        double promedioDiario = porDia.isEmpty() ? 0 : totalIngresos / porDia.size();
        double ventasPorDia = porDia.isEmpty() ? 0 : (double) completadasList.size() / porDia.size();

        // Mejor / peor día
        LocalDate mejor = null; double mejorVal = -1;
        LocalDate peor = null; double peorVal = Double.MAX_VALUE;
        for (Map.Entry<LocalDate, Double> e : porDia.entrySet()) {
            if (e.getValue() > mejorVal) { mejorVal = e.getValue(); mejor = e.getKey(); }
            if (e.getValue() < peorVal) { peorVal = e.getValue(); peor = e.getKey(); }
        }
        if (porDia.isEmpty()) peorVal = 0;

        // Tendencia primera vs segunda mitad
        double primera = 0, segunda = 0;
        if (!porDia.isEmpty()) {
            LocalDate primer = porDia.keySet().iterator().next();
            LocalDate ultimo = null; for (LocalDate d : porDia.keySet()) ultimo = d;
            long dias = ChronoUnit.DAYS.between(primer, ultimo) + 1;
            LocalDate corte = primer.plusDays(dias / 2);
            for (Map.Entry<LocalDate, Double> e : porDia.entrySet()) {
                if (e.getKey().isBefore(corte)) primera += e.getValue(); else segunda += e.getValue();
            }
        }
        double tendencia = primera <= 0 ? (segunda > 0 ? 100.0 : 0) : ((segunda - primera) / primera) * 100.0;

        // Promedio minutos entre ventas
        double promedioMinEntre = 0;
        if (ordenadas.size() > 1) {
            long totalMin = 0; int pares = 0;
            for (int i = 1; i < ordenadas.size(); i++) {
                LocalDateTime a = ordenadas.get(i - 1).getFecha();
                LocalDateTime b = ordenadas.get(i).getFecha();
                if (a != null && b != null) {
                    totalMin += ChronoUnit.MINUTES.between(a, b);
                    pares++;
                }
            }
            promedioMinEntre = pares == 0 ? 0 : (double) totalMin / pares;
        }

        // Cierres de caja en el rango
        List<CierreCaja> cierres;
        if (inicio != null && fin != null) {
            cierres = cierreCajaRepository.findByFechaCierreBetweenOrderByFechaCierreDesc(inicio, fin);
        } else if (inicio != null || fin != null) {
            LocalDateTime d = inicio != null ? inicio : LocalDate.now().minusYears(10).atStartOfDay();
            LocalDateTime h = fin != null ? fin : LocalDateTime.now();
            cierres = cierreCajaRepository.findByFechaCierreBetweenOrderByFechaCierreDesc(d, h);
        } else {
            cierres = cierreCajaRepository.findAllByOrderByFechaCierreDesc();
        }

        int cantCierres = cierres.size();
        double difProm = 0, difMax = 0, difMin = 0;
        long cuadrados = 0, sobrante = 0, faltante = 0;
        if (!cierres.isEmpty()) {
            double sumaDif = 0; int conDif = 0;
            Double max = null, min = null;
            for (CierreCaja c : cierres) {
                Double d = c.getDiferencia();
                if (d == null) {
                    cuadrados++;
                    continue;
                }
                sumaDif += d; conDif++;
                if (max == null || d > max) max = d;
                if (min == null || d < min) min = d;
                if (Math.abs(d) < 0.01) cuadrados++;
                else if (d > 0) sobrante++;
                else faltante++;
            }
            difProm = conDif == 0 ? 0 : sumaDif / conDif;
            difMax = max != null ? max : 0;
            difMin = min != null ? min : 0;
        }
        double porcCuadre = cantCierres == 0 ? 0 : (cuadrados * 100.0 / cantCierres);

        return new ResumenRendimiento(
                desde, hasta, ventas.size(), completadasList.size(), (int) anuladas, porcAnuladas,
                unidades, totalIngresos, ticket, ivaRecaudado, descuentos, ganancia,
                promedioDiario, mejor, mejorVal < 0 ? 0 : mejorVal, peor, peorVal,
                tendencia, porDia, transPorDia, porHora, promedioMinEntre, ventasPorDia,
                totalEfectivo, totalTarjeta, totalTransferencia,
                top, menos, unidadesPorProducto, ingresosPorProducto,
                cantCierres, difProm, difMax, difMin, cuadrados, sobrante, faltante, porcCuadre,
                cierres
        );
    }

    public byte[] generarReportePdf(LocalDate desde, LocalDate hasta) {
        ResumenRendimiento r = calcularResumen(desde, hasta);
        List<Venta> ventas = ventaRepository.findHistorico(
                desde != null ? desde.atStartOfDay() : null,
                hasta != null ? hasta.atTime(LocalTime.of(23, 59, 59)) : null,
                null, null);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();
            agregarEncabezado(doc, desde, hasta);
            // Mantener versión actual + añadir detalles
            agregarKpisOperativos(doc, r);
            agregarKpisCierre(doc, r);
            agregarPagosPorMetodo(doc, r);
            agregarTablaPorDia(doc, r);
            agregarVentasPorHora(doc, r);
            agregarTablaProductos(doc, "Top 5 productos más vendidos", r.topProductos());
            agregarTablaProductos(doc, "5 productos menos vendidos", r.menosVendidos());
            if (!r.cierresDetalle().isEmpty()) agregarTablaCierres(doc, r.cierresDetalle());
            if (!ventas.isEmpty()) {
                agregarTablaVentas(doc, ventas);
                agregarTablaLineasVenta(doc, ventas);
                agregarTablaPagos(doc, ventas);
            }
            // Anexo dataset crudo para análisis externo
            agregarAnexoDatasetInfo(doc);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte de rendimiento PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generarReporteExcel(LocalDate desde, LocalDate hasta) {
        ResumenRendimiento r = calcularResumen(desde, hasta);
        List<Venta> ventas = ventaRepository.findHistorico(
                desde != null ? desde.atStartOfDay() : null,
                hasta != null ? hasta.atTime(LocalTime.of(23, 59, 59)) : null,
                null, null);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            CellStyle stTitulo = estiloTitulo(wb);
            CellStyle stHeader = estiloHeader(wb);
            CellStyle stMoneda = estiloMoneda(wb);
            CellStyle stFecha = estiloFecha(wb);
            CellStyle stTotal = estiloTotal(wb);
            // Mantener 2 hojas actuales
            escribirHojaResumen(wb, r, desde, hasta, stTitulo, stHeader, stMoneda);
            escribirHojaVentas(wb, ventas, desde, hasta, stTitulo, stHeader, stMoneda, stFecha, stTotal);
            // Nuevas hojas detalladas para análisis profundo
            escribirHojaLineasDetalle(wb, ventas, stHeader, stMoneda);
            escribirHojaPagos(wb, ventas, stHeader, stMoneda);
            escribirHojaCierres(wb, r.cierresDetalle(), stHeader, stMoneda, stFecha);
            escribirHojaProductosAgregado(wb, r, stHeader, stMoneda);
            escribirHojaPorDia(wb, r, stHeader, stMoneda);
            escribirHojaPorHora(wb, r, stHeader);
            escribirHojaDatasetCrudo(wb, ventas, r.cierresDetalle(), stHeader, stMoneda, stFecha);
            wb.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar reporte de rendimiento Excel: " + e.getMessage(), e);
        }
    }

    // ── PDF helpers ──
    private void agregarEncabezado(Document doc, LocalDate desde, LocalDate hasta) throws DocumentException {
        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph t = new Paragraph(NOMBRE_FARMACIA + " - Reporte de Rendimiento y Funcionamiento", fTitulo);
        t.setAlignment(Element.ALIGN_CENTER); doc.add(t);
        Paragraph p = new Paragraph("Período: " + formatearPeriodo(desde, hasta) + "  ·  Generado: " + LocalDateTime.now().format(FMT_FECHA_HORA), fSub);
        p.setAlignment(Element.ALIGN_CENTER); doc.add(p);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarKpisOperativos(Document doc, ResumenRendimiento r) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fVal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph t = new Paragraph("Eficiencia operacional (ventas)", fSeccion);
        t.setSpacingAfter(4); doc.add(t);
        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100); tbl.setWidths(new float[]{2f, 1.5f, 2f, 1.5f});
        filaKpi(tbl, "Total ventas", String.valueOf(r.totalVentas()), fLabel, fVal);
        filaKpi(tbl, "Completadas / Anuladas", r.completadas() + " / " + r.anuladas() + " (" + String.format("%.1f%%", r.porcentajeAnuladas()) + " anuladas)", fLabel, fVal);
        filaKpi(tbl, "Unidades vendidas", String.valueOf(r.unidadesVendidas()), fLabel, fVal);
        filaKpi(tbl, "Ticket promedio", peso(r.ticketPromedio()), fLabel, fVal);
        filaKpi(tbl, "Total ingresos", peso(r.totalIngresos()), fLabel, fVal);
        filaKpi(tbl, "Ganancia estimada", peso(r.gananciaEstimada()), fLabel, fVal);
        filaKpi(tbl, "IVA recaudado", peso(r.ivaRecaudado()), fLabel, fVal);
        filaKpi(tbl, "Descuentos", peso(r.descuentosOtorgados()), fLabel, fVal);
        filaKpi(tbl, "Promedio diario", peso(r.promedioDiario()), fLabel, fVal);
        filaKpi(tbl, "Ventas/día promedio", String.format("%.1f", r.ventasPorDiaPromedio()), fLabel, fVal);
        filaKpi(tbl, "Minutos entre ventas (prom)", r.promedioMinutosEntreVentas() == 0 ? "—" : String.format("%.1f min", r.promedioMinutosEntreVentas()), fLabel, fVal);
        String mejor = r.mejorDia() != null ? r.mejorDia().format(FMT_FECHA) + " (" + peso(r.mejorDiaIngresos()) + ")" : "—";
        String peor = r.peorDia() != null ? r.peorDia().format(FMT_FECHA) + " (" + peso(r.peorDiaIngresos()) + ")" : "—";
        filaKpi(tbl, "Mejor día", mejor, fLabel, fVal);
        filaKpi(tbl, "Día más bajo", peor, fLabel, fVal);
        filaKpi(tbl, "Tendencia 2ª vs 1ª mitad", (r.tendenciaPorcentaje() >= 0 ? "▲ " : "▼ ") + String.format("%.1f%%", Math.abs(r.tendenciaPorcentaje())), fLabel, fVal);
        celdaVacia(tbl, fVal);
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarKpisCierre(Document doc, ResumenRendimiento r) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fVal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph t = new Paragraph("Cuadre de caja (descuadres)", fSeccion);
        t.setSpacingAfter(4); doc.add(t);
        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100); tbl.setWidths(new float[]{2f, 1.5f, 2f, 1.5f});
        filaKpi(tbl, "Cierres en período", String.valueOf(r.cantidadCierres()), fLabel, fVal);
        filaKpi(tbl, "Cierres cuadrados", r.cierresCuadrados() + " (" + String.format("%.1f%%", r.porcentajeCuadre()) + ")", fLabel, fVal);
        filaKpi(tbl, "Con sobrante", String.valueOf(r.cierresConSobrante()), fLabel, fVal);
        filaKpi(tbl, "Con faltante", String.valueOf(r.cierresConFaltante()), fLabel, fVal);
        filaKpi(tbl, "Diferencia promedio", peso(r.diferenciaPromedio()), fLabel, fVal);
        filaKpi(tbl, "Máx sobrante", peso(r.diferenciaMax()), fLabel, fVal);
        filaKpi(tbl, "Máx faltante", peso(r.diferenciaMin()), fLabel, fVal);
        filaKpi(tbl, "% cuadre (objetivo tesis)", String.format("%.1f%%", r.porcentajeCuadre()), fLabel, fVal);
        doc.add(tbl); doc.add(Chunk.NEWLINE);
        Font fNota = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7);
        Paragraph nota = new Paragraph("Un cierre se considera cuadrado cuando diferencia = 0 (monto declarado = efectivo esperado). Diferencia promedio cercana a 0 indica menor descuadre.", fNota);
        nota.setSpacingBefore(2); doc.add(nota); doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaPorDia(Document doc, ResumenRendimiento r) throws DocumentException {
        if (r.ingresosPorDia().isEmpty()) return;
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph p = new Paragraph("Rendimiento por día", fSeccion);
        p.setSpacingAfter(4); doc.add(p);
        PdfPTable tbl = new PdfPTable(3);
        tbl.setWidthPercentage(55); tbl.setWidths(new float[]{2f, 1.5f, 2f});
        celdaHeader(tbl, "Fecha", fBold); celdaHeader(tbl, "Ventas", fBold); celdaHeader(tbl, "Ingresos", fBold);
        boolean par = true;
        for (Map.Entry<LocalDate, Double> e : r.ingresosPorDia().entrySet()) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            celdaDato(tbl, e.getKey().format(FMT_FECHA), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, String.valueOf(r.transaccionesPorDia().getOrDefault(e.getKey(), 0)), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, peso(e.getValue()), fNormal, bg, Element.ALIGN_RIGHT);
            par = !par;
        }
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarVentasPorHora(Document doc, ResumenRendimiento r) throws DocumentException {
        if (r.ventasPorHora().isEmpty()) return;
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph p = new Paragraph("Distribución por hora (velocidad operacional)", fSeccion);
        p.setSpacingAfter(4); doc.add(p);
        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(40); tbl.setWidths(new float[]{2f, 2f});
        celdaHeader(tbl, "Hora", fBold); celdaHeader(tbl, "Ventas", fBold);
        boolean par = true;
        for (Map.Entry<Integer, Integer> e : r.ventasPorHora().entrySet()) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            celdaDato(tbl, String.format("%02d:00", e.getKey()), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, String.valueOf(e.getValue()), fNormal, bg, Element.ALIGN_CENTER);
            par = !par;
        }
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarPagosPorMetodo(Document doc, ResumenRendimiento r) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fVal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph t = new Paragraph("Medios de pago (solo completadas)", fSeccion);
        t.setSpacingAfter(4); doc.add(t);
        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100); tbl.setWidths(new float[]{2f, 1.5f, 2f, 1.5f});
        filaKpi(tbl, "Efectivo", peso(r.totalEfectivo()), fLabel, fVal);
        filaKpi(tbl, "Tarjeta", peso(r.totalTarjeta()), fLabel, fVal);
        filaKpi(tbl, "Transferencia", peso(r.totalTransferencia()), fLabel, fVal);
        double totalPagos = r.totalEfectivo() + r.totalTarjeta() + r.totalTransferencia();
        filaKpi(tbl, "Total pagos", peso(totalPagos), fLabel, fVal);
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaProductos(Document doc, String titulo, List<ProductoVendido> productos) throws DocumentException {
        if (productos == null || productos.isEmpty()) return;
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph p = new Paragraph(titulo, fSeccion);
        p.setSpacingAfter(4); doc.add(p);
        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(70); tbl.setWidths(new float[]{1f, 4f, 1.5f, 2f});
        celdaHeader(tbl, "#", fBold); celdaHeader(tbl, "Producto", fBold); celdaHeader(tbl, "Unidades", fBold); celdaHeader(tbl, "Ingresos", fBold);
        boolean par = true; int pos = 1;
        for (ProductoVendido pv : productos) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            celdaDato(tbl, String.valueOf(pos++), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, pv.nombre(), fNormal, bg, Element.ALIGN_LEFT);
            celdaDato(tbl, String.valueOf(pv.unidades()), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, peso(pv.ingresos()), fNormal, bg, Element.ALIGN_RIGHT);
            par = !par;
        }
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaCierres(Document doc, List<CierreCaja> cierres) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Paragraph t = new Paragraph("Detalle de cierres de caja en el período", fSeccion);
        t.setSpacingAfter(4); doc.add(t);
        PdfPTable tbl = new PdfPTable(9);
        tbl.setWidthPercentage(100); tbl.setWidths(new float[]{1.2f, 2f, 2f, 1.5f, 1.5f, 1.5f, 1.2f, 1.5f, 1.2f});
        celdaHeader(tbl, "ID", fBold); celdaHeader(tbl, "Apertura", fBold); celdaHeader(tbl, "Cierre", fBold);
        celdaHeader(tbl, "Ventas", fBold); celdaHeader(tbl, "Efectivo", fBold); celdaHeader(tbl, "Tarjeta", fBold);
        celdaHeader(tbl, "Cant.", fBold); celdaHeader(tbl, "Diferencia", fBold); celdaHeader(tbl, "Estado", fBold);
        boolean par = true;
        for (CierreCaja c : cierres) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            celdaDato(tbl, String.valueOf(c.getId()), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, c.getFechaApertura() != null ? c.getFechaApertura().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, c.getFechaCierre() != null ? c.getFechaCierre().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, peso(c.getTotalVentas()), fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tbl, peso(c.getTotalEfectivo()), fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tbl, peso(c.getTotalTarjeta()), fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tbl, String.valueOf(c.getCantidadVentas()), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, c.getDiferencia() != null ? String.format("$ %,.0f", c.getDiferencia()) : "—", fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tbl, c.getEstado() != null ? c.getEstado() : "—", fNormal, bg, Element.ALIGN_CENTER);
            par = !par;
        }
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaVentas(Document doc, List<Venta> ventas) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Paragraph t = new Paragraph("Detalle de transacciones (cabecera)", fSeccion);
        t.setSpacingAfter(4); doc.add(t);
        PdfPTable tbl = new PdfPTable(7);
        tbl.setWidthPercentage(100); tbl.setWidths(new float[]{1.2f, 2f, 2f, 1f, 1.5f, 1.5f, 1.5f});
        celdaHeader(tbl, "N° Venta", fBold); celdaHeader(tbl, "Fecha", fBold); celdaHeader(tbl, "Vendedor", fBold); celdaHeader(tbl, "Estado", fBold); celdaHeader(tbl, "Subtotal", fBold); celdaHeader(tbl, "IVA", fBold); celdaHeader(tbl, "Total", fBold);
        boolean par = true;
        for (Venta v : ventas) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            celdaDato(tbl, String.format("%06d", v.getIdVenta()), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : "—", fNormal, bg, Element.ALIGN_LEFT);
            celdaDato(tbl, v.getEstado(), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tbl, peso(v.getSubtotal()), fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tbl, peso(v.getIva()), fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tbl, peso(v.getTotal()), fNormal, bg, Element.ALIGN_RIGHT);
            par = !par;
        }
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaLineasVenta(Document doc, List<Venta> ventas) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Paragraph t = new Paragraph("Detalle de líneas de venta (dataset para análisis)", fSeccion);
        t.setSpacingAfter(4); doc.add(t);
        PdfPTable tbl = new PdfPTable(8);
        tbl.setWidthPercentage(100); tbl.setWidths(new float[]{1f, 2f, 3f, 1f, 1.2f, 1.5f, 1.2f, 1.5f});
        celdaHeader(tbl, "Venta", fBold); celdaHeader(tbl, "Fecha", fBold); celdaHeader(tbl, "Producto", fBold); celdaHeader(tbl, "Cant", fBold); celdaHeader(tbl, "Precio", fBold); celdaHeader(tbl, "Subtotal", fBold); celdaHeader(tbl, "IVA", fBold); celdaHeader(tbl, "Estado venta", fBold);
        boolean par = true;
        for (Venta v : ventas) {
            if (v.getDetalles() == null || v.getDetalles().isEmpty()) {
                Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
                celdaDato(tbl, String.format("%06d", v.getIdVenta()), fNormal, bg, Element.ALIGN_CENTER);
                celdaDato(tbl, v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
                celdaDato(tbl, "— (sin líneas)", fNormal, bg, Element.ALIGN_LEFT);
                celdaDato(tbl, "—", fNormal, bg, Element.ALIGN_CENTER);
                celdaDato(tbl, "—", fNormal, bg, Element.ALIGN_RIGHT);
                celdaDato(tbl, "—", fNormal, bg, Element.ALIGN_RIGHT);
                celdaDato(tbl, "—", fNormal, bg, Element.ALIGN_RIGHT);
                celdaDato(tbl, v.getEstado(), fNormal, bg, Element.ALIGN_CENTER);
                par = !par;
            } else {
                for (DetalleVenta d : v.getDetalles()) {
                    Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
                    celdaDato(tbl, String.format("%06d", v.getIdVenta()), fNormal, bg, Element.ALIGN_CENTER);
                    celdaDato(tbl, v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
                    celdaDato(tbl, d.getProducto() != null ? d.getProducto().getNombre() : "—", fNormal, bg, Element.ALIGN_LEFT);
                    celdaDato(tbl, String.valueOf(d.getCantidad()), fNormal, bg, Element.ALIGN_CENTER);
                    celdaDato(tbl, peso(d.getPrecioUnitarioAplicado()), fNormal, bg, Element.ALIGN_RIGHT);
                    celdaDato(tbl, peso(d.getSubtotalLinea()), fNormal, bg, Element.ALIGN_RIGHT);
                    celdaDato(tbl, peso(d.getIvaLinea()), fNormal, bg, Element.ALIGN_RIGHT);
                    celdaDato(tbl, v.getEstado(), fNormal, bg, Element.ALIGN_CENTER);
                    par = !par;
                }
            }
        }
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaPagos(Document doc, List<Venta> ventas) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Paragraph t = new Paragraph("Pagos por venta", fSeccion);
        t.setSpacingAfter(4); doc.add(t);
        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(70); tbl.setWidths(new float[]{1.5f, 2f, 1.5f, 2f});
        celdaHeader(tbl, "Venta", fBold); celdaHeader(tbl, "Fecha", fBold); celdaHeader(tbl, "Tipo", fBold); celdaHeader(tbl, "Monto", fBold);
        boolean par = true;
        for (Venta v : ventas) {
            if (v.getPagos() == null || v.getPagos().isEmpty()) {
                Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
                celdaDato(tbl, String.format("%06d", v.getIdVenta()), fNormal, bg, Element.ALIGN_CENTER);
                celdaDato(tbl, v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
                celdaDato(tbl, "—", fNormal, bg, Element.ALIGN_CENTER);
                celdaDato(tbl, "—", fNormal, bg, Element.ALIGN_RIGHT);
                par = !par;
            } else {
                for (PagoVenta p : v.getPagos()) {
                    Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
                    celdaDato(tbl, String.format("%06d", v.getIdVenta()), fNormal, bg, Element.ALIGN_CENTER);
                    celdaDato(tbl, v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
                    celdaDato(tbl, p.getTipo(), fNormal, bg, Element.ALIGN_CENTER);
                    celdaDato(tbl, peso(p.getMonto()), fNormal, bg, Element.ALIGN_RIGHT);
                    par = !par;
                }
            }
        }
        doc.add(tbl); doc.add(Chunk.NEWLINE);
    }

    private void agregarAnexoDatasetInfo(Document doc) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Paragraph t = new Paragraph("Anexo – Dataset para análisis profundo (ver Excel con 8 hojas)", fSeccion);
        t.setSpacingBefore(8); doc.add(t);
        Paragraph p = new Paragraph("El archivo Excel contiene todas las tablas en formato tabular crudo: Resumen, Detalle ventas (cabecera), Lineas_detalle, Pagos, Cierres, Productos_agregado, Por_dia, Por_hora, Dataset_crudo. Use filtros/tablas dinámicas para replicar métricas, calcular velocidades por rango, correlacionar descuadres vs volumen, y alimentar análisis estadístico externo (R/Python/Excel). Todos los valores son datos directos sin muestreo.", fNormal);
        doc.add(p);
    }

    // ── Excel ──
    private void escribirHojaResumen(Workbook wb, ResumenRendimiento r, LocalDate desde, LocalDate hasta, CellStyle stTitulo, CellStyle stHeader, CellStyle stMoneda) {
        Sheet sh = wb.createSheet("Rendimiento");
        int f = 0;
        Row t = sh.createRow(f++); Cell ct = t.createCell(0); ct.setCellValue(NOMBRE_FARMACIA + " - Reporte de Rendimiento"); ct.setCellStyle(stTitulo);
        Row p = sh.createRow(f++); p.createCell(0).setCellValue("Período: " + formatearPeriodo(desde, hasta));
        Row g = sh.createRow(f++); g.createCell(0).setCellValue("Generado: " + LocalDateTime.now().format(FMT_FECHA_HORA));
        f++;
        f = escribirPar(sh, f, "Total ventas:", String.valueOf(r.totalVentas()));
        f = escribirPar(sh, f, "Completadas / Anuladas:", r.completadas() + " / " + r.anuladas() + " (" + String.format("%.1f%%", r.porcentajeAnuladas()) + ")");
        f = escribirPar(sh, f, "Unidades vendidas:", String.valueOf(r.unidadesVendidas()));
        f = escribirParMoneda(sh, f, "Total ingresos:", r.totalIngresos(), stMoneda);
        f = escribirParMoneda(sh, f, "Ticket promedio:", r.ticketPromedio(), stMoneda);
        f = escribirParMoneda(sh, f, "Ganancia estimada:", r.gananciaEstimada(), stMoneda);
        f = escribirParMoneda(sh, f, "IVA recaudado:", r.ivaRecaudado(), stMoneda);
        f = escribirParMoneda(sh, f, "Descuentos:", r.descuentosOtorgados(), stMoneda);
        f = escribirParMoneda(sh, f, "Promedio diario:", r.promedioDiario(), stMoneda);
        f = escribirPar(sh, f, "Ventas/día promedio:", String.format("%.1f", r.ventasPorDiaPromedio()));
        f = escribirPar(sh, f, "Min entre ventas (prom):", r.promedioMinutosEntreVentas() == 0 ? "—" : String.format("%.1f min", r.promedioMinutosEntreVentas()));
        f = escribirPar(sh, f, "Tendencia 2ª vs 1ª mitad:", String.format("%.1f%%", r.tendenciaPorcentaje()));
        f++;
        f = escribirPar(sh, f, "Cierres en período:", String.valueOf(r.cantidadCierres()));
        f = escribirPar(sh, f, "Cierres cuadrados:", r.cierresCuadrados() + " (" + String.format("%.1f%%", r.porcentajeCuadre()) + ")");
        f = escribirPar(sh, f, "Con sobrante:", String.valueOf(r.cierresConSobrante()));
        f = escribirPar(sh, f, "Con faltante:", String.valueOf(r.cierresConFaltante()));
        f = escribirParMoneda(sh, f, "Diferencia promedio:", r.diferenciaPromedio(), stMoneda);
        f = escribirParMoneda(sh, f, "Máx sobrante:", r.diferenciaMax(), stMoneda);
        f = escribirParMoneda(sh, f, "Máx faltante:", r.diferenciaMin(), stMoneda);
        f++;
        Row h = sh.createRow(f++);
        String[] hdr = {"Fecha", "Ventas", "Ingresos"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        for (Map.Entry<LocalDate, Double> e : r.ingresosPorDia().entrySet()) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(e.getKey().format(FMT_FECHA));
            row.createCell(1).setCellValue(r.transaccionesPorDia().getOrDefault(e.getKey(), 0));
            Cell ing = row.createCell(2); ing.setCellValue(e.getValue()); ing.setCellStyle(stMoneda);
        }
        f++;
        Row hh = sh.createRow(f++);
        String[] hdr2 = {"Hora", "Ventas"};
        for (int i = 0; i < hdr2.length; i++) { Cell c = hh.createCell(i); c.setCellValue(hdr2[i]); c.setCellStyle(stHeader); }
        for (Map.Entry<Integer, Integer> e : r.ventasPorHora().entrySet()) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(String.format("%02d:00", e.getKey()));
            row.createCell(1).setCellValue(e.getValue());
        }
        sh.autoSizeColumn(0); sh.autoSizeColumn(1); sh.autoSizeColumn(2);
    }

    private void escribirHojaVentas(Workbook wb, List<Venta> ventas, LocalDate desde, LocalDate hasta, CellStyle stTitulo, CellStyle stHeader, CellStyle stMoneda, CellStyle stFecha, CellStyle stTotal) {
        Sheet sh = wb.createSheet("Detalle ventas");
        int f = 0;
        Row t = sh.createRow(f++); Cell ct = t.createCell(0); ct.setCellValue(NOMBRE_FARMACIA + " - Detalle ventas"); ct.setCellStyle(stTitulo);
        Row p = sh.createRow(f++); p.createCell(0).setCellValue("Período: " + formatearPeriodo(desde, hasta));
        f++;
        Row h = sh.createRow(f++);
        String[] hdr = {"N° Venta", "Fecha", "Vendedor", "Estado", "Total"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        for (Venta v : ventas) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(v.getIdVenta());
            Cell cf = row.createCell(1); cf.setCellValue(v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : ""); cf.setCellStyle(stFecha);
            row.createCell(2).setCellValue(v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : "—");
            row.createCell(3).setCellValue(v.getEstado());
            Cell ct2 = row.createCell(4); ct2.setCellValue(v.getTotal() != null ? v.getTotal() : 0); ct2.setCellStyle(stMoneda);
        }
        sh.autoSizeColumn(0); sh.autoSizeColumn(1); sh.autoSizeColumn(2); sh.autoSizeColumn(3); sh.autoSizeColumn(4);
    }

    private void escribirHojaLineasDetalle(Workbook wb, List<Venta> ventas, CellStyle stHeader, CellStyle stMoneda) {
        Sheet sh = wb.createSheet("Lineas_detalle");
        int f = 0;
        Row h = sh.createRow(f++);
        String[] hdr = {"ventaId", "fechaVenta", "estadoVenta", "producto", "cantidad", "precioUnitario", "subtotalLinea", "ivaLinea", "costo", "gananciaLinea"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        for (Venta v : ventas) {
            if (v.getDetalles() == null || v.getDetalles().isEmpty()) {
                Row row = sh.createRow(f++);
                row.createCell(0).setCellValue(v.getIdVenta());
                row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "");
                row.createCell(2).setCellValue(v.getEstado());
                row.createCell(3).setCellValue("—");
                row.createCell(4).setCellValue(0);
            } else {
                for (DetalleVenta d : v.getDetalles()) {
                    Row row = sh.createRow(f++);
                    row.createCell(0).setCellValue(v.getIdVenta());
                    row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "");
                    row.createCell(2).setCellValue(v.getEstado());
                    row.createCell(3).setCellValue(d.getProducto() != null ? d.getProducto().getNombre() : "—");
                    row.createCell(4).setCellValue(d.getCantidad() != null ? d.getCantidad() : 0);
                    Cell cPrecio = row.createCell(5); cPrecio.setCellValue(d.getPrecioUnitarioAplicado() != null ? d.getPrecioUnitarioAplicado() : 0); cPrecio.setCellStyle(stMoneda);
                    Cell cSub = row.createCell(6); cSub.setCellValue(d.getSubtotalLinea() != null ? d.getSubtotalLinea() : 0); cSub.setCellStyle(stMoneda);
                    Cell cIva = row.createCell(7); cIva.setCellValue(d.getIvaLinea() != null ? d.getIvaLinea() : 0); cIva.setCellStyle(stMoneda);
                    double costo = d.getProducto() != null && d.getProducto().getCosto() != null ? d.getProducto().getCosto() : 0;
                    Cell cCosto = row.createCell(8); cCosto.setCellValue(costo); cCosto.setCellStyle(stMoneda);
                    double ganancia = ((d.getPrecioUnitarioAplicado() != null ? d.getPrecioUnitarioAplicado() : 0) - costo) * (d.getCantidad() != null ? d.getCantidad() : 0);
                    Cell cGan = row.createCell(9); cGan.setCellValue(ganancia); cGan.setCellStyle(stMoneda);
                }
            }
        }
        for (int i = 0; i < hdr.length; i++) sh.autoSizeColumn(i);
    }

    private void escribirHojaPagos(Workbook wb, List<Venta> ventas, CellStyle stHeader, CellStyle stMoneda) {
        Sheet sh = wb.createSheet("Pagos");
        int f = 0;
        Row h = sh.createRow(f++);
        String[] hdr = {"ventaId", "fechaVenta", "estadoVenta", "tipoPago", "monto"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        for (Venta v : ventas) {
            if (v.getPagos() == null || v.getPagos().isEmpty()) {
                Row row = sh.createRow(f++);
                row.createCell(0).setCellValue(v.getIdVenta());
                row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "");
                row.createCell(2).setCellValue(v.getEstado());
                row.createCell(3).setCellValue("—");
                row.createCell(4).setCellValue(0);
            } else {
                for (PagoVenta p : v.getPagos()) {
                    Row row = sh.createRow(f++);
                    row.createCell(0).setCellValue(v.getIdVenta());
                    row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : "");
                    row.createCell(2).setCellValue(v.getEstado());
                    row.createCell(3).setCellValue(p.getTipo());
                    Cell cM = row.createCell(4); cM.setCellValue(p.getMonto() != null ? p.getMonto() : 0); cM.setCellStyle(stMoneda);
                }
            }
        }
        for (int i = 0; i < hdr.length; i++) sh.autoSizeColumn(i);
    }

    private void escribirHojaCierres(Workbook wb, List<CierreCaja> cierres, CellStyle stHeader, CellStyle stMoneda, CellStyle stFecha) {
        Sheet sh = wb.createSheet("Cierres");
        int f = 0;
        Row h = sh.createRow(f++);
        String[] hdr = {"id", "fechaApertura", "fechaCierre", "idUsuario", "username", "montoInicial", "montoDeclarado", "totalEfectivo", "totalTarjeta", "totalTransferencia", "totalVentas", "totalDescuentos", "totalIva", "cantidadVentas", "cantidadAnuladas", "gananciaEstimada", "diferencia", "estado", "efectivoEsperado"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        for (CierreCaja c : cierres) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(c.getId() != null ? c.getId() : 0);
            Cell ca = row.createCell(1); ca.setCellValue(c.getFechaApertura() != null ? c.getFechaApertura().format(FMT_FECHA_HORA) : ""); ca.setCellStyle(stFecha);
            Cell cc = row.createCell(2); cc.setCellValue(c.getFechaCierre() != null ? c.getFechaCierre().format(FMT_FECHA_HORA) : ""); cc.setCellStyle(stFecha);
            row.createCell(3).setCellValue(c.getIdUsuario() != null ? c.getIdUsuario() : 0);
            row.createCell(4).setCellValue(c.getUsername() != null ? c.getUsername() : "—");
            Cell ci = row.createCell(5); ci.setCellValue(c.getMontoInicial() != null ? c.getMontoInicial() : 0); ci.setCellStyle(stMoneda);
            Cell cd = row.createCell(6); cd.setCellValue(c.getMontoDeclarado() != null ? c.getMontoDeclarado() : 0); cd.setCellStyle(stMoneda);
            Cell ce = row.createCell(7); ce.setCellValue(c.getTotalEfectivo() != null ? c.getTotalEfectivo() : 0); ce.setCellStyle(stMoneda);
            Cell ct = row.createCell(8); ct.setCellValue(c.getTotalTarjeta() != null ? c.getTotalTarjeta() : 0); ct.setCellStyle(stMoneda);
            Cell ctr = row.createCell(9); ctr.setCellValue(c.getTotalTransferencia() != null ? c.getTotalTransferencia() : 0); ctr.setCellStyle(stMoneda);
            Cell tv = row.createCell(10); tv.setCellValue(c.getTotalVentas() != null ? c.getTotalVentas() : 0); tv.setCellStyle(stMoneda);
            Cell td = row.createCell(11); td.setCellValue(c.getTotalDescuentos() != null ? c.getTotalDescuentos() : 0); td.setCellStyle(stMoneda);
            Cell ti = row.createCell(12); ti.setCellValue(c.getTotalIva() != null ? c.getTotalIva() : 0); ti.setCellStyle(stMoneda);
            row.createCell(13).setCellValue(c.getCantidadVentas() != null ? c.getCantidadVentas() : 0);
            row.createCell(14).setCellValue(c.getCantidadAnuladas() != null ? c.getCantidadAnuladas() : 0);
            Cell cg = row.createCell(15); cg.setCellValue(c.getGananciaEstimada() != null ? c.getGananciaEstimada() : 0); cg.setCellStyle(stMoneda);
            Cell dif = row.createCell(16); dif.setCellValue(c.getDiferencia() != null ? c.getDiferencia() : 0); dif.setCellStyle(stMoneda);
            row.createCell(17).setCellValue(c.getEstado() != null ? c.getEstado() : "—");
            double esperado = (c.getTotalEfectivo() != null ? c.getTotalEfectivo() : 0) + (c.getMontoInicial() != null ? c.getMontoInicial() : 0);
            Cell ce2 = row.createCell(18); ce2.setCellValue(esperado); ce2.setCellStyle(stMoneda);
        }
        for (int i = 0; i < hdr.length; i++) sh.autoSizeColumn(i);
    }

    private void escribirHojaProductosAgregado(Workbook wb, ResumenRendimiento r, CellStyle stHeader, CellStyle stMoneda) {
        Sheet sh = wb.createSheet("Productos_agregado");
        int f = 0;
        Row h = sh.createRow(f++);
        String[] hdr = {"producto", "unidadesVendidas", "ingresos"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        // Todos los productos ordenados por unidades desc
        List<ProductoVendido> todos = new ArrayList<>();
        for (String nombre : r.unidadesPorProductoRaw().keySet()) {
            todos.add(new ProductoVendido(nombre, r.unidadesPorProductoRaw().get(nombre)[0], r.ingresosPorProductoRaw().get(nombre)[0]));
        }
        todos.sort(Comparator.comparingInt(ProductoVendido::unidades).reversed().thenComparing(Comparator.comparingDouble(ProductoVendido::ingresos).reversed()));
        for (ProductoVendido pv : todos) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(pv.nombre());
            row.createCell(1).setCellValue(pv.unidades());
            Cell ci = row.createCell(2); ci.setCellValue(pv.ingresos()); ci.setCellStyle(stMoneda);
        }
        for (int i = 0; i < hdr.length; i++) sh.autoSizeColumn(i);
    }

    private void escribirHojaPorDia(Workbook wb, ResumenRendimiento r, CellStyle stHeader, CellStyle stMoneda) {
        Sheet sh = wb.createSheet("Por_dia");
        int f = 0;
        Row h = sh.createRow(f++);
        String[] hdr = {"fecha", "ventasCompletadas", "ingresos", "ticketPromedioDia"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        for (Map.Entry<LocalDate, Double> e : r.ingresosPorDia().entrySet()) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(e.getKey().format(FMT_FECHA));
            int cant = r.transaccionesPorDia().getOrDefault(e.getKey(), 0);
            row.createCell(1).setCellValue(cant);
            Cell ci = row.createCell(2); ci.setCellValue(e.getValue()); ci.setCellStyle(stMoneda);
            double ticketDia = cant == 0 ? 0 : e.getValue() / cant;
            Cell ct = row.createCell(3); ct.setCellValue(ticketDia); ct.setCellStyle(stMoneda);
        }
        for (int i = 0; i < hdr.length; i++) sh.autoSizeColumn(i);
    }

    private void escribirHojaPorHora(Workbook wb, ResumenRendimiento r, CellStyle stHeader) {
        Sheet sh = wb.createSheet("Por_hora");
        int f = 0;
        Row h = sh.createRow(f++);
        String[] hdr = {"hora", "ventas"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        for (Map.Entry<Integer, Integer> e : r.ventasPorHora().entrySet()) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(String.format("%02d:00", e.getKey()));
            row.createCell(1).setCellValue(e.getValue());
        }
        for (int i = 0; i < hdr.length; i++) sh.autoSizeColumn(i);
    }

    private void escribirHojaDatasetCrudo(Workbook wb, List<Venta> ventas, List<CierreCaja> cierres, CellStyle stHeader, CellStyle stMoneda, CellStyle stFecha) {
        Sheet sh = wb.createSheet("Dataset_crudo");
        int f = 0;
        Row info = sh.createRow(f++); info.createCell(0).setCellValue("Dataset crudo: cada fila = una venta con agregados + cierre asociado por fecha (si existe). Para análisis externo (pivot, regresión, correlación).");
        f++;
        Row h = sh.createRow(f++);
        String[] hdr = {"ventaId", "fechaVenta", "estado", "subtotal", "iva", "descuento", "total", "cambio", "vendedor", "idVendedor", "cantLineas", "unidadesTotales", "cantPagos", "totalEfectivoVenta", "totalTarjetaVenta", "cierreIdEnFecha", "cierreDiferencia"};
        for (int i = 0; i < hdr.length; i++) { Cell c = h.createCell(i); c.setCellValue(hdr[i]); c.setCellStyle(stHeader); }
        // Mapa cierre por fecha para lookup
        Map<LocalDate, CierreCaja> cierrePorDia = new TreeMap<>();
        for (CierreCaja c : cierres) if (c.getFechaCierre() != null) cierrePorDia.put(c.getFechaCierre().toLocalDate(), c);
        for (Venta v : ventas) {
            Row row = sh.createRow(f++);
            row.createCell(0).setCellValue(v.getIdVenta());
            Cell cf = row.createCell(1); cf.setCellValue(v.getFecha() != null ? v.getFecha().format(FMT_FECHA_HORA) : ""); cf.setCellStyle(stFecha);
            row.createCell(2).setCellValue(v.getEstado());
            Cell cs = row.createCell(3); cs.setCellValue(v.getSubtotal() != null ? v.getSubtotal() : 0); cs.setCellStyle(stMoneda);
            Cell ci = row.createCell(4); ci.setCellValue(v.getIva() != null ? v.getIva() : 0); ci.setCellStyle(stMoneda);
            Cell cd = row.createCell(5); cd.setCellValue(v.getDescuento() != null ? v.getDescuento() : 0); cd.setCellStyle(stMoneda);
            Cell ct = row.createCell(6); ct.setCellValue(v.getTotal() != null ? v.getTotal() : 0); ct.setCellStyle(stMoneda);
            Cell cc = row.createCell(7); cc.setCellValue(v.getCambio() != null ? v.getCambio() : 0); cc.setCellStyle(stMoneda);
            row.createCell(8).setCellValue(v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : "—");
            row.createCell(9).setCellValue(v.getUsuario() != null && v.getUsuario().getIdUsuario() != null ? v.getUsuario().getIdUsuario() : 0);
            int cantLineas = v.getDetalles() != null ? v.getDetalles().size() : 0;
            row.createCell(10).setCellValue(cantLineas);
            int unidades = 0; if (v.getDetalles() != null) for (DetalleVenta d : v.getDetalles()) unidades += d.getCantidad() != null ? d.getCantidad() : 0;
            row.createCell(11).setCellValue(unidades);
            int cantPagos = v.getPagos() != null ? v.getPagos().size() : 0;
            row.createCell(12).setCellValue(cantPagos);
            double ef = 0, tar = 0; if (v.getPagos() != null) for (PagoVenta p : v.getPagos()) { if ("EFECTIVO".equalsIgnoreCase(p.getTipo())) ef += p.getMonto() != null ? p.getMonto() : 0; else if ("TARJETA".equalsIgnoreCase(p.getTipo())) tar += p.getMonto() != null ? p.getMonto() : 0; }
            Cell ce = row.createCell(13); ce.setCellValue(ef); ce.setCellStyle(stMoneda);
            Cell ctar = row.createCell(14); ctar.setCellValue(tar); ctar.setCellStyle(stMoneda);
            CierreCaja cierre = v.getFecha() != null ? cierrePorDia.get(v.getFecha().toLocalDate()) : null;
            row.createCell(15).setCellValue(cierre != null && cierre.getId() != null ? cierre.getId() : 0);
            Cell cdif = row.createCell(16); cdif.setCellValue(cierre != null && cierre.getDiferencia() != null ? cierre.getDiferencia() : 0); cdif.setCellStyle(stMoneda);
        }
        for (int i = 0; i < hdr.length; i++) sh.autoSizeColumn(i);
    }

    // ── helpers compartidos ──
    private void filaKpi(PdfPTable tbl, String etiqueta, String valor, Font fLabel, Font fVal) {
        PdfPCell l = new PdfPCell(new Phrase(etiqueta, fLabel));
        l.setBackgroundColor(COLOR_SECCION); l.setPadding(4); tbl.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(valor, fVal));
        v.setPadding(4); tbl.addCell(v);
    }
    private void celdaVacia(PdfPTable tbl, Font f) {
        PdfPCell c = new PdfPCell(new Phrase("", f)); c.setBorder(Rectangle.NO_BORDER); tbl.addCell(c);
        PdfPCell c2 = new PdfPCell(new Phrase("", f)); c2.setBorder(Rectangle.NO_BORDER); tbl.addCell(c2);
    }
    private void celdaHeader(PdfPTable tbl, String txt, Font f) {
        Font wf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, f.getSize(), Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(txt, wf));
        c.setBackgroundColor(COLOR_HEADER); c.setPadding(5); c.setHorizontalAlignment(Element.ALIGN_CENTER); tbl.addCell(c);
    }
    private void celdaDato(PdfPTable tbl, String txt, Font f, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt != null ? txt : "", f));
        c.setBackgroundColor(bg); c.setPadding(4); c.setHorizontalAlignment(align); tbl.addCell(c);
    }
    private String peso(Double v) { if (v == null) return "$ 0"; return String.format("$ %,.0f", v); }
    private String formatearPeriodo(LocalDate d, LocalDate h) {
        if (d == null && h == null) return "Todas las fechas";
        String di = d != null ? d.format(FMT_FECHA) : "...";
        String hf = h != null ? h.format(FMT_FECHA) : "...";
        return di + " - " + hf;
    }
    private int escribirPar(Sheet sh, int f, String etiqueta, String valor) {
        Row row = sh.createRow(f); row.createCell(0).setCellValue(etiqueta);
        Cell c = row.createCell(1);
        try { c.setCellValue(Long.parseLong(valor)); } catch (NumberFormatException ex) { c.setCellValue(valor); }
        return f + 1;
    }
    private int escribirParMoneda(Sheet sh, int f, String etiqueta, double valor, CellStyle st) {
        Row row = sh.createRow(f); row.createCell(0).setCellValue(etiqueta);
        Cell c = row.createCell(1); c.setCellValue(valor); c.setCellStyle(st); return f + 1;
    }
    private CellStyle estiloTitulo(Workbook wb) {
        org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true); font.setFontHeightInPoints((short) 14);
        CellStyle s = wb.createCellStyle(); s.setFont(font); return s;
    }
    private CellStyle estiloHeader(Workbook wb) {
        org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true); font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle s = wb.createCellStyle(); s.setFont(font); s.setFillForegroundColor(IndexedColors.BLUE.getIndex()); s.setFillPattern(FillPatternType.SOLID_FOREGROUND); return s;
    }
    private CellStyle estiloMoneda(Workbook wb) {
        CellStyle s = wb.createCellStyle(); s.setDataFormat(wb.createDataFormat().getFormat("$ #,##0")); return s;
    }
    private CellStyle estiloFecha(Workbook wb) { return wb.createCellStyle(); }
    private CellStyle estiloTotal(Workbook wb) {
        org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true);
        CellStyle s = wb.createCellStyle(); s.setFont(font); return s;
    }
}
