package co.edu.unbosque.backend.service;

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
import java.util.List;

/**
 * Servicio para generar reportes de ventas exportables en PDF y Excel.
 *
 * @author Angie Tatiana Ortiz
 */
@Service
public class ReporteVentasService {

    private static final String NOMBRE_FARMACIA = "Droguería Farmarosita";
    private static final Color COLOR_HEADER = new Color(41, 128, 185);
    private static final Color COLOR_FILA_PAR = new Color(235, 245, 251);

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera el reporte de ventas en formato PDF.
     */
    public byte[] generarReportePdf(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            agregarEncabezado(doc, fechaInicio, fechaFin);
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
     */
    public byte[] generarReporteExcel(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte de ventas");

            CellStyle estiloTitulo = estiloTitulo(workbook);
            CellStyle estiloHeader = estiloHeader(workbook);
            CellStyle estiloMoneda = estiloMoneda(workbook);
            CellStyle estiloFecha = estiloFecha(workbook);
            CellStyle estiloTotal = estiloTotal(workbook);

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

            double totalGeneral = 0.0;
            for (Venta venta : ventas) {
                Row fila = sheet.createRow(filaActual++);
                fila.createCell(0).setCellValue(venta.getIdVenta());
                Cell celdaFecha = fila.createCell(1);
                celdaFecha.setCellValue(venta.getFecha().format(FMT_FECHA_HORA));
                celdaFecha.setCellStyle(estiloFecha);
                fila.createCell(2).setCellValue(venta.getUsuario().getNombreCompleto());
                fila.createCell(3).setCellValue(venta.getEstado());
                Cell celdaTotal = fila.createCell(4);
                celdaTotal.setCellValue(venta.getTotal());
                celdaTotal.setCellStyle(estiloMoneda);
                totalGeneral += venta.getTotal();
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
            valorTotal.setCellValue(totalGeneral);
            valorTotal.setCellStyle(estiloMoneda);

            for (int i = 0; i < encabezados.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el reporte de ventas en Excel: " + e.getMessage(), e);
        }
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

    private void agregarTablaVentas(Document doc, List<Venta> ventas) throws DocumentException {
        Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);

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
            celdaDato(tabla, venta.getFecha().format(FMT_FECHA_HORA), fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, venta.getUsuario().getNombreCompleto(), fNormal, bg, Element.ALIGN_LEFT);
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

    // ── Excel ────────────────────────────────────────────────────────────────

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
