package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Venta;
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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Servicio para generar reportes de ventas exportables en PDF y Excel.
 * Además de la tabla de transacciones, incluye un análisis del período:
 * KPIs, rendimiento por día, tendencia y productos más/menos vendidos.
 *
 * @author Angie Tatiana Ortiz
 */
@Service
public class ReporteVentasService {

    private static final String NOMBRE_FARMACIA = "Droguería Farmarosita";
    private static final Color COLOR_HEADER = new Color(41, 128, 185);
    private static final Color COLOR_FILA_PAR = new Color(235, 245, 251);
    private static final Color COLOR_SECCION = new Color(236, 240, 241);

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int MAX_PRODUCTOS_TOP = 5;

    /**
     * Genera el reporte de ventas en formato PDF.
     */
    public byte[] generarReportePdf(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            ResumenVentas resumen = calcularResumen(ventas);

            agregarEncabezado(doc, fechaInicio, fechaFin);
            agregarBloqueKpis(doc, resumen);
            agregarTablaProductos(doc, "Top " + MAX_PRODUCTOS_TOP + " productos más vendidos", resumen.topProductos());
            agregarTablaProductos(doc, MAX_PRODUCTOS_TOP + " productos menos vendidos del período", resumen.menosVendidos());
            agregarRendimientoDiario(doc, resumen);
            agregarTablaVentas(doc, ventas);
            agregarResumen(doc, ventas);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte de ventas en PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Genera el reporte de ventas en formato Excel (.xlsx).
     * Hoja 1: detalle de transacciones. Hoja 2: análisis del período.
     */
    public byte[] generarReporteExcel(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            ResumenVentas resumen = calcularResumen(ventas);

            CellStyle estiloTitulo = estiloTitulo(workbook);
            CellStyle estiloHeader = estiloHeader(workbook);
            CellStyle estiloMoneda = estiloMoneda(workbook);
            CellStyle estiloFecha = estiloFecha(workbook);
            CellStyle estiloTotal = estiloTotal(workbook);

            escribirHojaDetalle(workbook, ventas, fechaInicio, fechaFin,
                    estiloTitulo, estiloHeader, estiloMoneda, estiloFecha, estiloTotal);
            escribirHojaAnalisis(workbook, resumen, estiloTitulo, estiloHeader, estiloMoneda);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el reporte de ventas en Excel: " + e.getMessage(), e);
        }
    }

    // ── Agregación / lógica de negocio ──────────────────────────────────────

    /** Producto agregado dentro del período analizado. */
    public record ProductoVendido(String nombre, int unidades, double ingresos) {}

    /**
     * Resultado del análisis del período. Las métricas monetarias se calculan
     * sobre las ventas COMPLETADAS (las anuladas no representan ingreso),
     * aunque siguen apareciendo en el detalle y en los conteos.
     */
    public record ResumenVentas(
            int totalVentas,
            int completadas,
            int anuladas,
            double totalIngresos,
            double ticketPromedio,
            int unidadesVendidas,
            double ivaRecaudado,
            double descuentosOtorgados,
            List<ProductoVendido> topProductos,
            List<ProductoVendido> menosVendidos,
            Map<LocalDate, Double> ingresosPorDia,
            Map<LocalDate, Integer> transaccionesPorDia,
            LocalDate mejorDia,
            double mejorDiaIngresos,
            LocalDate peorDia,
            double peorDiaIngresos,
            double promedioDiario,
            double ingresoPrimeraMitad,
            double ingresoSegundaMitad
    ) {
        /** Variación porcentual entre la segunda y la primera mitad del período. */
        public double tendenciaPorcentaje() {
            if (ingresoPrimeraMitad <= 0) {
                return ingresoSegundaMitad > 0 ? 100.0 : 0.0;
            }
            return ((ingresoSegundaMitad - ingresoPrimeraMitad) / ingresoPrimeraMitad) * 100.0;
        }
    }

    /**
     * Calcula todas las métricas del período a partir de la lista ya filtrada.
     * No realiza consultas adicionales: todo sale de las ventas y sus detalles.
     */
    public ResumenVentas calcularResumen(List<Venta> ventas) {
        List<Venta> completadasList = new ArrayList<>();
        int anuladas = 0;
        for (Venta v : ventas) {
            if ("COMPLETADA".equalsIgnoreCase(v.getEstado())) {
                completadasList.add(v);
            } else if ("ANULADA".equalsIgnoreCase(v.getEstado())) {
                anuladas++;
            }
        }

        double totalIngresos = 0.0;
        double ivaRecaudado = 0.0;
        double descuentos = 0.0;
        int unidades = 0;

        Map<String, int[]> unidadesPorProducto = new LinkedHashMap<>();
        Map<String, double[]> ingresosPorProducto = new LinkedHashMap<>();
        Map<LocalDate, Double> porDia = new TreeMap<>();
        Map<LocalDate, Integer> transaccionesPorDia = new TreeMap<>();

        for (Venta venta : completadasList) {
            totalIngresos += valor(venta.getTotal());
            descuentos += valor(venta.getDescuento());

            if (venta.getDetalles() != null) {
                for (DetalleVenta d : venta.getDetalles()) {
                    int cantidad = d.getCantidad() != null ? d.getCantidad() : 0;
                    unidades += cantidad;
                    ivaRecaudado += valor(d.getIvaLinea());

                    Producto p = d.getProducto();
                    String nombre = p != null && p.getNombre() != null ? p.getNombre() : "Producto";
                    unidadesPorProducto.computeIfAbsent(nombre, k -> new int[]{0})[0] += cantidad;
                    ingresosPorProducto.computeIfAbsent(nombre, k -> new double[]{0.0})[0] += valor(d.getSubtotalLinea());
                }
            }

            LocalDate dia = venta.getFecha() != null ? venta.getFecha().toLocalDate() : null;
            if (dia != null) {
                porDia.merge(dia, valor(venta.getTotal()), Double::sum);
                transaccionesPorDia.merge(dia, 1, Integer::sum);
            }
        }

        List<ProductoVendido> productos = new ArrayList<>();
        for (String nombre : unidadesPorProducto.keySet()) {
            productos.add(new ProductoVendido(nombre,
                    unidadesPorProducto.get(nombre)[0],
                    ingresosPorProducto.get(nombre)[0]));
        }
        productos.sort(Comparator.comparingInt(ProductoVendido::unidades).reversed()
                .thenComparing(Comparator.comparingDouble(ProductoVendido::ingresos).reversed()));

        List<ProductoVendido> top = productos.stream().limit(MAX_PRODUCTOS_TOP).toList();
        List<ProductoVendido> menos = new ArrayList<>(productos.stream()
                .sorted(Comparator.comparingInt(ProductoVendido::unidades)
                        .thenComparing(Comparator.comparingDouble(ProductoVendido::ingresos)))
                .limit(MAX_PRODUCTOS_TOP)
                .toList());
        menos.sort(Comparator.comparingInt(ProductoVendido::unidades));

        // Rendimiento diario: mejor día, peor día y promedio.
        LocalDate mejorDia = null;
        double mejorDiaIngresos = -1;
        LocalDate peorDia = null;
        double peorDiaIngresos = Double.MAX_VALUE;
        for (Map.Entry<LocalDate, Double> e : porDia.entrySet()) {
            if (e.getValue() > mejorDiaIngresos) {
                mejorDiaIngresos = e.getValue();
                mejorDia = e.getKey();
            }
            if (e.getValue() < peorDiaIngresos) {
                peorDiaIngresos = e.getValue();
                peorDia = e.getKey();
            }
        }
        if (porDia.isEmpty()) {
            peorDiaIngresos = 0;
        }

        double promedioDiario = porDia.isEmpty() ? 0 : totalIngresos / porDia.size();

        // Tendencia: ingresos de la primera vs segunda mitad del período.
        double ingresoPrimeraMitad = 0;
        double ingresoSegundaMitad = 0;
        if (!porDia.isEmpty()) {
            LocalDate primerDia = porDia.keySet().iterator().next();
            LocalDate ultimoDia = null;
            for (LocalDate d : porDia.keySet()) ultimoDia = d;
            long diasTotales = java.time.temporal.ChronoUnit.DAYS.between(primerDia, ultimoDia) + 1;
            LocalDate corte = primerDia.plusDays(diasTotales / 2);
            for (Map.Entry<LocalDate, Double> e : porDia.entrySet()) {
                if (e.getKey().isBefore(corte)) {
                    ingresoPrimeraMitad += e.getValue();
                } else {
                    ingresoSegundaMitad += e.getValue();
                }
            }
        }

        return new ResumenVentas(
                ventas.size(),
                completadasList.size(),
                anuladas,
                totalIngresos,
                completadasList.isEmpty() ? 0 : totalIngresos / completadasList.size(),
                unidades,
                ivaRecaudado,
                descuentos,
                top,
                menos,
                porDia,
                transaccionesPorDia,
                mejorDia,
                Math.max(mejorDiaIngresos, 0),
                peorDia,
                peorDiaIngresos,
                promedioDiario,
                ingresoPrimeraMitad,
                ingresoSegundaMitad
        );
    }

    // ── PDF ──────────────────────────────────────────────────────────────────

    private void agregarEncabezado(Document doc, LocalDate fechaInicio, LocalDate fechaFin) throws DocumentException {
        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph titulo = new Paragraph(NOMBRE_FARMACIA + " - Reporte de ventas", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph periodo = new Paragraph("Período: " + formatearPeriodo(fechaInicio, fechaFin), fSubtitulo);
        periodo.setAlignment(Element.ALIGN_CENTER);
        doc.add(periodo);

        doc.add(Chunk.NEWLINE);
    }

    private void agregarBloqueKpis(Document doc, ResumenVentas r) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        Paragraph titulo = new Paragraph("Análisis del período", fSeccion);
        titulo.setSpacingBefore(8);
        titulo.setSpacingAfter(4);
        doc.add(titulo);

        Font fLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fValor = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{2f, 1.5f, 2f, 1.5f});

        filaKpi(tabla, "Cantidad de ventas", String.valueOf(r.totalVentas()), fLabel, fValor);
        filaKpi(tabla, "Completadas / Anuladas", r.completadas() + " / " + r.anuladas(), fLabel, fValor);
        filaKpi(tabla, "Unidades vendidas", String.valueOf(r.unidadesVendidas()), fLabel, fValor);
        filaKpi(tabla, "Ticket promedio", peso(r.ticketPromedio()), fLabel, fValor);
        filaKpi(tabla, "Total del período", peso(r.totalIngresos()), fLabel, fValor);
        filaKpi(tabla, "IVA recaudado", peso(r.ivaRecaudado()), fLabel, fValor);
        filaKpi(tabla, "Descuentos otorgados", peso(r.descuentosOtorgados()), fLabel, fValor);
        filaKpi(tabla, "Promedio diario", peso(r.promedioDiario()), fLabel, fValor);

        String mejorDiaTexto = r.mejorDia() != null
                ? r.mejorDia().format(FMT_FECHA) + " (" + peso(r.mejorDiaIngresos()) + ")"
                : "—";
        String peorDiaTexto = r.peorDia() != null
                ? r.peorDia().format(FMT_FECHA) + " (" + peso(r.peorDiaIngresos()) + ")"
                : "—";

        filaKpi(tabla, "Mejor día", mejorDiaTexto, fLabel, fValor);
        filaKpi(tabla, "Día más bajo", peorDiaTexto, fLabel, fValor);

        double tendencia = r.tendenciaPorcentaje();
        String flecha = tendencia >= 0 ? "▲" : "▼";
        filaKpi(tabla, "Tendencia (2ª mitad vs 1ª)",
                flecha + " " + String.format("%,.1f%%", Math.abs(tendencia)), fLabel, fValor);
        celdaVacia(tabla, fValor);

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaProductos(Document doc, String titulo, List<ProductoVendido> productos) throws DocumentException {
        if (productos == null || productos.isEmpty()) {
            return;
        }

        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Paragraph p = new Paragraph(titulo, fSeccion);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        doc.add(p);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(70);
        tabla.setWidths(new float[]{1f, 4f, 1.5f, 2f});

        celdaHeader(tabla, "#", fBold);
        celdaHeader(tabla, "Producto", fBold);
        celdaHeader(tabla, "Unidades", fBold);
        celdaHeader(tabla, "Ingresos", fBold);

        boolean par = true;
        int posicion = 1;
        for (ProductoVendido pv : productos) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            celdaDato(tabla, String.valueOf(posicion++), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, pv.nombre(), fNormal, bg, Element.ALIGN_LEFT);
            celdaDato(tabla, String.valueOf(pv.unidades()), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, peso(pv.ingresos()), fNormal, bg, Element.ALIGN_RIGHT);
            par = !par;
        }

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarRendimientoDiario(Document doc, ResumenVentas r) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Paragraph p = new Paragraph("Rendimiento por día", fSeccion);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        doc.add(p);

        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(50);
        tabla.setWidths(new float[]{2f, 1.5f, 2f});

        celdaHeader(tabla, "Fecha", fBold);
        celdaHeader(tabla, "Ventas", fBold);
        celdaHeader(tabla, "Ingresos", fBold);

        boolean par = true;
        for (Map.Entry<LocalDate, Double> e : r.ingresosPorDia().entrySet()) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            int ventasDelDia = r.transaccionesPorDia().getOrDefault(e.getKey(), 0);
            celdaDato(tabla, e.getKey().format(FMT_FECHA), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, String.valueOf(ventasDelDia), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, peso(e.getValue()), fNormal, bg, Element.ALIGN_RIGHT);
            par = !par;
        }

        PdfPCell nota = new PdfPCell(new Phrase(
                "Ingresos y ventas por día calculados solo con transacciones completadas.", fNormal));
        nota.setBorder(Rectangle.NO_BORDER);
        nota.setColspan(3);
        nota.setPaddingTop(3);
        tabla.addCell(nota);

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaVentas(Document doc, List<Venta> ventas) throws DocumentException {
        Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Paragraph titulo = new Paragraph("Detalle de transacciones", fSeccion);
        titulo.setSpacingBefore(10);
        titulo.setSpacingAfter(4);
        doc.add(titulo);

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1.5f, 2.5f, 3f, 2f, 2f});

        celdaHeader(tabla, "N° Venta", fBold);
        celdaHeader(tabla, "Fecha", fBold);
        celdaHeader(tabla, "Vendedor", fBold);
        celdaHeader(tabla, "Estado", fBold);
        celdaHeader(tabla, "Total", fBold);

        boolean par = true;
        for (Venta venta : ventas) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;

            celdaDato(tabla, String.format("%06d", venta.getIdVenta()), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, venta.getFecha() != null ? venta.getFecha().format(FMT_FECHA_HORA) : "", fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, venta.getUsuario() != null ? venta.getUsuario().getNombreCompleto() : "—", fNormal, bg, Element.ALIGN_LEFT);
            celdaDato(tabla, venta.getEstado(), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, peso(venta.getTotal()), fNormal, bg, Element.ALIGN_RIGHT);
            par = !par;
        }

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarResumen(Document doc, List<Venta> ventas) throws DocumentException {
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        double totalGeneral = ventas.stream().mapToDouble(Venta::getTotal).sum();

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(45);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setWidths(new float[]{2f, 2f});

        filaTotal(tabla, "Cantidad de ventas:", String.valueOf(ventas.size()), fNormal, fNormal);
        filaTotal(tabla, "Total del período:", peso(totalGeneral), fBold, fBold);

        doc.add(tabla);
    }

    private void filaKpi(PdfPTable tabla, String etiqueta, String valor, Font fLabel, Font fValor) {
        PdfPCell l = new PdfPCell(new Phrase(etiqueta, fLabel));
        l.setBackgroundColor(COLOR_SECCION);
        l.setPadding(4);
        tabla.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(valor, fValor));
        v.setPadding(4);
        v.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabla.addCell(v);
    }

    private void celdaVacia(PdfPTable tabla, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase("", font));
        cell.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cell);
        PdfPCell cell2 = new PdfPCell(new Phrase("", font));
        cell2.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cell2);
    }

    private void celdaHeader(PdfPTable tabla, String texto, Font font) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, font.getSize(), Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texto, f));
        cell.setBackgroundColor(COLOR_HEADER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);
    }

    private void celdaDato(PdfPTable tabla, String texto, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        cell.setHorizontalAlignment(align);
        tabla.addCell(cell);
    }

    private void filaTotal(PdfPTable tabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell l = new PdfPCell(new Phrase(label, fLabel));
        l.setBorder(Rectangle.NO_BORDER);
        l.setHorizontalAlignment(Element.ALIGN_RIGHT);
        l.setPaddingRight(6);
        l.setPaddingBottom(3);
        tabla.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(valor, fValor));
        v.setBorder(Rectangle.NO_BORDER);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPaddingBottom(3);
        tabla.addCell(v);
    }

    private String peso(Double valor) {
        if (valor == null) return "$ 0";
        return String.format("$ %,.0f", valor);
    }

    private double valor(Double v) {
        return v != null ? v : 0.0;
    }

    // ── Excel ────────────────────────────────────────────────────────────────

    private void escribirHojaDetalle(
            Workbook workbook, List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin,
            CellStyle estiloTitulo, CellStyle estiloHeader, CellStyle estiloMoneda,
            CellStyle estiloFecha, CellStyle estiloTotal
    ) {
        Sheet sheet = workbook.createSheet("Reporte de ventas");

        int filaActual = 0;

        Row filaTitulo = sheet.createRow(filaActual++);
        Cell celdaTitulo = filaTitulo.createCell(0);
        celdaTitulo.setCellValue(NOMBRE_FARMACIA + " - Reporte de ventas");
        celdaTitulo.setCellStyle(estiloTitulo);

        Row filaPeriodo = sheet.createRow(filaActual++);
        filaPeriodo.createCell(0).setCellValue("Período: " + formatearPeriodo(fechaInicio, fechaFin));

        filaActual++;

        Row filaHeader = sheet.createRow(filaActual++);
        String[] encabezados = {"N° Venta", "Fecha", "Vendedor", "Estado", "Total"};
        for (int i = 0; i < encabezados.length; i++) {
            Cell celda = filaHeader.createCell(i);
            celda.setCellValue(encabezados[i]);
            celda.setCellStyle(estiloHeader);
        }

        for (Venta venta : ventas) {
            Row fila = sheet.createRow(filaActual++);
            fila.createCell(0).setCellValue(venta.getIdVenta());
            Cell celdaFecha = fila.createCell(1);
            celdaFecha.setCellValue(venta.getFecha() != null ? venta.getFecha().format(FMT_FECHA_HORA) : "");
            celdaFecha.setCellStyle(estiloFecha);
            fila.createCell(2).setCellValue(venta.getUsuario() != null ? venta.getUsuario().getNombreCompleto() : "—");
            fila.createCell(3).setCellValue(venta.getEstado());
            Cell celdaTotal = fila.createCell(4);
            celdaTotal.setCellValue(valor(venta.getTotal()));
            celdaTotal.setCellStyle(estiloMoneda);
        }

        filaActual++;
        Row filaResumenCantidad = sheet.createRow(filaActual++);
        filaResumenCantidad.createCell(0).setCellValue("Cantidad de ventas:");
        filaResumenCantidad.createCell(1).setCellValue(ventas.size());

        Row filaResumenTotal = sheet.createRow(filaActual++);
        Cell etiquetaTotal = filaResumenTotal.createCell(0);
        etiquetaTotal.setCellValue("Total del período:");
        etiquetaTotal.setCellStyle(estiloTotal);
        Cell valorTotal = filaResumenTotal.createCell(1);
        valorTotal.setCellValue(ventas.stream().mapToDouble(v -> valor(v.getTotal())).sum());
        valorTotal.setCellStyle(estiloMoneda);

        for (int i = 0; i < encabezados.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void escribirHojaAnalisis(
            Workbook workbook, ResumenVentas r,
            CellStyle estiloTitulo, CellStyle estiloHeader, CellStyle estiloMoneda
    ) {
        Sheet sheet = workbook.createSheet("Análisis");

        int fila = 0;

        Row t = sheet.createRow(fila++);
        Cell ct = t.createCell(0);
        ct.setCellValue(NOMBRE_FARMACIA + " - Análisis del período");
        ct.setCellStyle(estiloTitulo);

        fila++;

        fila = escribirPar(sheet, fila, "Cantidad de ventas:", String.valueOf(r.totalVentas()));
        fila = escribirPar(sheet, fila, "Ventas completadas:", String.valueOf(r.completadas()));
        fila = escribirPar(sheet, fila, "Ventas anuladas:", String.valueOf(r.anuladas()));
        fila = escribirParMoneda(sheet, fila, "Ingresos del período (completadas):", r.totalIngresos(), estiloMoneda);
        fila = escribirParMoneda(sheet, fila, "Ticket promedio:", r.ticketPromedio(), estiloMoneda);
        fila = escribirPar(sheet, fila, "Unidades vendidas:", String.valueOf(r.unidadesVendidas()));
        fila = escribirParMoneda(sheet, fila, "IVA recaudado:", r.ivaRecaudado(), estiloMoneda);
        fila = escribirParMoneda(sheet, fila, "Descuentos otorgados:", r.descuentosOtorgados(), estiloMoneda);
        fila = escribirParMoneda(sheet, fila, "Promedio diario:", r.promedioDiario(), estiloMoneda);

        fila++;

        Row hTop = sheet.createRow(fila++);
        String[] encabezadosTop = {"Top productos", "Unidades", "Ingresos"};
        for (int i = 0; i < encabezadosTop.length; i++) {
            Cell c = hTop.createCell(i);
            c.setCellValue(encabezadosTop[i]);
            c.setCellStyle(estiloHeader);
        }
        for (ProductoVendido pv : r.topProductos()) {
            Row fRow = sheet.createRow(fila++);
            fRow.createCell(0).setCellValue(pv.nombre());
            fRow.createCell(1).setCellValue(pv.unidades());
            Cell ing = fRow.createCell(2);
            ing.setCellValue(pv.ingresos());
            ing.setCellStyle(estiloMoneda);
        }

        fila++;

        Row hMenos = sheet.createRow(fila++);
        String[] encabezadosMenos = {"Productos menos vendidos", "Unidades", "Ingresos"};
        for (int i = 0; i < encabezadosMenos.length; i++) {
            Cell c = hMenos.createCell(i);
            c.setCellValue(encabezadosMenos[i]);
            c.setCellStyle(estiloHeader);
        }
        for (ProductoVendido pv : r.menosVendidos()) {
            Row fRow = sheet.createRow(fila++);
            fRow.createCell(0).setCellValue(pv.nombre());
            fRow.createCell(1).setCellValue(pv.unidades());
            Cell ing = fRow.createCell(2);
            ing.setCellValue(pv.ingresos());
            ing.setCellStyle(estiloMoneda);
        }

        fila++;

        Row hDia = sheet.createRow(fila++);
        String[] encabezadosDia = {"Rendimiento por día", "Ingresos"};
        for (int i = 0; i < encabezadosDia.length; i++) {
            Cell c = hDia.createCell(i);
            c.setCellValue(encabezadosDia[i]);
            c.setCellStyle(estiloHeader);
        }
        for (Map.Entry<LocalDate, Double> e : r.ingresosPorDia().entrySet()) {
            Row fRow = sheet.createRow(fila++);
            fRow.createCell(0).setCellValue(e.getKey().format(FMT_FECHA));
            Cell ing = fRow.createCell(1);
            ing.setCellValue(e.getValue());
            ing.setCellStyle(estiloMoneda);
        }

        fila++;
        double tendencia = r.tendenciaPorcentaje();
        Row filaTendencia = sheet.createRow(fila++);
        filaTendencia.createCell(0).setCellValue("Tendencia (2ª mitad vs 1ª):");
        filaTendencia.createCell(1).setCellValue((tendencia >= 0 ? "+" : "") + String.format("%.1f%%", tendencia));

        if (!r.ingresosPorDia().isEmpty()) {
            Row filaMejor = sheet.createRow(fila++);
            filaMejor.createCell(0).setCellValue("Mejor día:");
            filaMejor.createCell(1).setCellValue(r.mejorDia() != null ? r.mejorDia().format(FMT_FECHA) : "—");
            Cell m = filaMejor.createCell(2);
            m.setCellValue(r.mejorDiaIngresos());
            m.setCellStyle(estiloMoneda);

            Row filaPeor = sheet.createRow(fila++);
            filaPeor.createCell(0).setCellValue("Día más bajo:");
            filaPeor.createCell(1).setCellValue(r.peorDia() != null ? r.peorDia().format(FMT_FECHA) : "—");
            Cell p = filaPeor.createCell(2);
            p.setCellValue(r.peorDiaIngresos());
            p.setCellStyle(estiloMoneda);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }

    private int escribirPar(Sheet sheet, int fila, String etiqueta, String valor) {
        Row row = sheet.createRow(fila);
        row.createCell(0).setCellValue(etiqueta);
        Cell c = row.createCell(1);
        try {
            c.setCellValue(Long.parseLong(valor));
        } catch (NumberFormatException ex) {
            c.setCellValue(valor);
        }
        return fila + 1;
    }

    private int escribirParMoneda(Sheet sheet, int fila, String etiqueta, double valor, CellStyle estiloMoneda) {
        Row row = sheet.createRow(fila);
        row.createCell(0).setCellValue(etiqueta);
        Cell c = row.createCell(1);
        c.setCellValue(valor);
        c.setCellStyle(estiloMoneda);
        return fila + 1;
    }

    private CellStyle estiloTitulo(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle estiloHeader(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle estiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$ #,##0"));
        return style;
    }

    private CellStyle estiloFecha(Workbook workbook) {
        return workbook.createCellStyle();
    }

    private CellStyle estiloTotal(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private String formatearPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null && fechaFin == null) {
            return "Todas las fechas";
        }
        String inicio = fechaInicio != null ? fechaInicio.format(FMT_FECHA) : "...";
        String fin = fechaFin != null ? fechaFin.format(FMT_FECHA) : "...";
        return inicio + " - " + fin;
    }
}
