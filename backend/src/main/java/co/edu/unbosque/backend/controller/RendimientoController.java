package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.service.RendimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Reporte de rendimiento y funcionamiento.
 * 100% in-app, offline, embebido en el JAR final.
 * No requiere permisos ni histórico persistido: genera on-demand por rango.
 */
@RestController
@RequestMapping("/api/rendimiento")
@Tag(name = "Rendimiento", description = "Reporte de funcionamiento y velocidad operacional (on-demand, por rango)")
public class RendimientoController {

    private final RendimientoService rendimientoService;

    public RendimientoController(RendimientoService rendimientoService) {
        this.rendimientoService = rendimientoService;
    }

    @GetMapping(value = "/reporte/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar reporte de rendimiento en PDF por rango")
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate hasta
    ) {
        validarRango(desde, hasta);
        byte[] pdf = rendimientoService.generarReportePdf(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-rendimiento.pdf\"")
                .body(pdf);
    }

    @GetMapping(value = "/reporte/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Descargar reporte de rendimiento en Excel por rango")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate hasta
    ) {
        validarRango(desde, hasta);
        byte[] excel = rendimientoService.generarReporteExcel(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-rendimiento.xlsx\"")
                .body(excel);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}
