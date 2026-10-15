package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.CumplimientoProveedorResponse;
import co.edu.unbosque.backend.model.response.TopProductoCompradoResponse;
import co.edu.unbosque.backend.service.CompraAnaliticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Analitica del proceso de compras para el dashboard de gestion:
 * top de productos comprados y cumplimiento de entregas por proveedor.
 */
@RestController
@RequestMapping("/api/compras-analitica")
@Tag(name = "Analitica de Compras", description = "Indicadores agregados del proceso de compras")
public class CompraAnaliticaController {

    private final CompraAnaliticaService analiticaService;

    public CompraAnaliticaController(CompraAnaliticaService analiticaService) {
        this.analiticaService = analiticaService;
    }

    /**
     * Top de productos con mayor gasto en compras dentro de un periodo.
     * Por defecto analiza los ultimos 6 meses.
     */
    @GetMapping("/top-productos")
    @Operation(summary = "Top productos mas comprados",
            description = "Productos ordenados por gasto (unidades x costo real) en el periodo indicado.")
    public ResponseEntity<List<TopProductoCompradoResponse>> topProductos(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin
    ) {
        LocalDateTime[] rango = normalizarRango(inicio, fin);
        return ResponseEntity.ok(analiticaService.topProductosComprados(rango[0], rango[1]));
    }

    /**
     * Porcentaje de recepciones entregadas a tiempo por proveedor.
     */
    @GetMapping("/cumplimiento-proveedores")
    @Operation(summary = "Cumplimiento de entregas a tiempo por proveedor",
            description = "Porcentaje de recepciones recibidas en o antes de la fecha esperada de su orden.")
    public ResponseEntity<List<CumplimientoProveedorResponse>> cumplimientoProveedores() {
        return ResponseEntity.ok(analiticaService.cumplimientoProveedores());
    }

    /** Rango por defecto: ultimos 6 meses hasta ahora. */
    private LocalDateTime[] normalizarRango(LocalDate inicio, LocalDate fin) {
        LocalDateTime finDt = fin != null ? fin.atTime(23, 59, 59) : LocalDateTime.now();
        LocalDateTime inicioDt = inicio != null ? inicio.atStartOfDay() : finDt.minusMonths(6);
        return new LocalDateTime[]{inicioDt, finDt};
    }
}
