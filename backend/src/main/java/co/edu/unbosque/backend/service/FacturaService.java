package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.PagoVenta;
import co.edu.unbosque.backend.model.entity.Venta;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para generacion de facturas de venta en formato PDF.
 * Usa la libreria OpenPDF (fork LGPL de iText 5).
 *
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
@Service
public class FacturaService {

    private static final String NOMBRE_FARMACIA = "Droguería Farmarosita";
    private static final String NIT             = "NIT: 900.123.456-7";
    private static final String DIRECCION       = "Ubaté, Cundinamarca";
    private static final String TELEFONO        = "Tel: 310 123 4567";
    private static final Color  COLOR_HEADER    = new Color(41, 128, 185);
    private static final Color  COLOR_FILA_PAR  = new Color(235, 245, 251);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera el PDF de la factura para una venta completada.
     *
     * @param venta venta con detalles y pagos ya cargados
     * @return bytes del PDF generado
     */
    public byte[] generarFactura(Venta venta) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            agregarEncabezado(doc, venta);
            agregarTablaItems(doc, venta);
            agregarTotales(doc, venta);
            agregarPagos(doc, venta);
            agregarPie(doc);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar la factura PDF: " + e.getMessage(), e);
        }
    }

    // ── Secciones ──────────────────────────────────────────────────────────────

    private void agregarEncabezado(Document doc, Venta venta) throws DocumentException {
        Font fTitulo    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fNormal    = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font fBold      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

        Paragraph titulo = new Paragraph(NOMBRE_FARMACIA, fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph nit = new Paragraph(NIT, fSubtitulo);
        nit.setAlignment(Element.ALIGN_CENTER);
        doc.add(nit);

        Paragraph dir = new Paragraph(DIRECCION + "  |  " + TELEFONO, fSubtitulo);
        dir.setAlignment(Element.ALIGN_CENTER);
        doc.add(dir);

        doc.add(Chunk.NEWLINE);

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{1.5f, 2.5f});

        celdaInfo(info, "Factura N°:", String.format("%06d", venta.getIdVenta()), fBold, fNormal);
        celdaInfo(info, "Fecha:",      venta.getFecha().format(FMT),               fBold, fNormal);
        celdaInfo(info, "Cajero:",     venta.getUsuario().getNombreCompleto(),      fBold, fNormal);
        celdaInfo(info, "Estado:",     venta.getEstado(),                           fBold, fNormal);

        doc.add(info);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarTablaItems(Document doc, Venta venta) throws DocumentException {
        Font fBold   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3.5f, 1f, 2f, 1.2f, 2f, 2f});

        celdaHeader(tabla, "Producto",    fBold);
        celdaHeader(tabla, "Cant.",       fBold);
        celdaHeader(tabla, "P. Unitario", fBold);
        celdaHeader(tabla, "IVA %",       fBold);
        celdaHeader(tabla, "IVA $",       fBold);
        celdaHeader(tabla, "Total línea", fBold);

        boolean par = true;
        for (DetalleVenta d : venta.getDetalles()) {
            Color bg = par ? Color.WHITE : COLOR_FILA_PAR;
            double totalLinea = d.getSubtotalLinea() + (d.getIvaLinea() != null ? d.getIvaLinea() : 0.0);

            celdaDato(tabla, d.getProducto().getNombre(),                                      fNormal, bg, Element.ALIGN_LEFT);
            celdaDato(tabla, String.valueOf(d.getCantidad()),                                  fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, peso(d.getPrecioUnitarioAplicado()),                              fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tabla, String.format("%.0f%%", d.getProducto().getPorcentajeIva()),      fNormal, bg, Element.ALIGN_CENTER);
            celdaDato(tabla, peso(d.getIvaLinea()),                                            fNormal, bg, Element.ALIGN_RIGHT);
            celdaDato(tabla, peso(totalLinea),                                                 fNormal, bg, Element.ALIGN_RIGHT);
            par = !par;
        }

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarTotales(Document doc, Venta venta) throws DocumentException {
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font fBold   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(45);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setWidths(new float[]{2f, 2f});

        filaTotal(tabla, "Subtotal:",  peso(venta.getSubtotal()),  fNormal, fNormal);
        filaTotal(tabla, "IVA:",       peso(venta.getIva()),        fNormal, fNormal);

        if (venta.getDescuento() != null && venta.getDescuento() > 0.0) {
            filaTotal(tabla, "Descuento:", "- " + peso(venta.getDescuento()), fNormal, fNormal);
        }

        filaTotal(tabla, "TOTAL:", peso(venta.getTotal()), fBold, fBold);

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarPagos(Document doc, Venta venta) throws DocumentException {
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font fBold   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

        Paragraph titulo = new Paragraph("Forma de pago:", fBold);
        doc.add(titulo);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(45);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setWidths(new float[]{2f, 2f});

        for (PagoVenta pago : venta.getPagos()) {
            filaTotal(tabla, pago.getTipo() + ":", peso(pago.getMonto()), fNormal, fNormal);
        }

        if (venta.getCambio() != null && venta.getCambio() > 0.0) {
            filaTotal(tabla, "Cambio:", peso(venta.getCambio()), fBold, fBold);
        }

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarPie(Document doc) throws DocumentException {
        Font fSmall = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);
        Paragraph pie = new Paragraph("¡Gracias por su compra!  —  Conserve esta factura", fSmall);
        pie.setAlignment(Element.ALIGN_CENTER);
        doc.add(pie);
    }

    // ── Helpers de celdas ──────────────────────────────────────────────────────

    private void celdaInfo(PdfPTable tabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell l = new PdfPCell(new Phrase(label, fLabel));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPaddingBottom(3);
        tabla.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(valor, fValor));
        v.setBorder(Rectangle.NO_BORDER);
        v.setPaddingBottom(3);
        tabla.addCell(v);
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
}
