package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.model.request.HistoricoFiltroRequest;
import co.edu.unbosque.backend.service.ReporteVentasService;
import co.edu.unbosque.backend.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la generación de reportes exportables del sistema.
 *
 * @author Angie Tatiana Ortiz
 */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Generación de reportes exportables (PDF/Excel)")
public class ReporteController {

    private final VentaService ventaService;
    private final ReporteVentasService reporteVentasService;

    public ReporteController(VentaService ventaService, ReporteVentasService reporteVentasService) {
        this.ventaService = ventaService;
        this.reporteVentasService = reporteVentasService;
    }

    @GetMapping(value = "/ventas/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar reporte de ventas en PDF")
    public ResponseEntity<byte[]> descargarReporteVentasPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaFin,
            @RequestParam(required = false) Long idVendedor,
            @RequestParam(required = false) String estado,
            @RequestHeader("X-Username") String username
    ) {
        List<Venta> ventas = consultarVentasParaReporte(fechaInicio, fechaFin, idVendedor, estado, username);
        byte[] pdf = reporteVentasService.generarReportePdf(ventas, fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"reporte-ventas.pdf\"")
                .body(pdf);
    }

    @GetMapping(value = "/ventas/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Descargar reporte de ventas en Excel")
    public ResponseEntity<byte[]> descargarReporteVentasExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaFin,
            @RequestParam(required = false) Long idVendedor,
            @RequestParam(required = false) String estado,
            @RequestHeader("X-Username") String username
    ) {
        List<Venta> ventas = consultarVentasParaReporte(fechaInicio, fechaFin, idVendedor, estado, username);
        byte[] excel = reporteVentasService.generarReporteExcel(ventas, fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"reporte-ventas.xlsx\"")
                .body(excel);
    }

    private List<Venta> consultarVentasParaReporte(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long idVendedor,
            String estado,
            String username
    ) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;
        HistoricoFiltroRequest filtros = new HistoricoFiltroRequest(inicio, fin, idVendedor, estado);
        return ventaService.consultarHistorico(filtros, username);
    }
}
