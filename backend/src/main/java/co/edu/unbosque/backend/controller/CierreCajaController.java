package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.CierreCajaRequest;
import co.edu.unbosque.backend.model.response.CierreCajaResponse;
import co.edu.unbosque.backend.model.response.CierreResumenResponse;
import co.edu.unbosque.backend.service.CierreCajaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cierres-caja")
@Tag(name = "Cierre de Caja", description = "Resumen y persistencia de cierres de caja por periodo")
public class CierreCajaController {

    private final CierreCajaService cierreCajaService;

    public CierreCajaController(CierreCajaService cierreCajaService) {
        this.cierreCajaService = cierreCajaService;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Obtener resumen en vivo de caja para un periodo sin persistir")
    public ResponseEntity<CierreResumenResponse> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Double montoInicial
    ) {
        return ResponseEntity.ok(cierreCajaService.generarResumen(desde, hasta, usuarioId, montoInicial));
    }

    @PostMapping
    @Operation(summary = "Crear y persistir un cierre de caja")
    public ResponseEntity<CierreCajaResponse> crear(@RequestBody(required = false) CierreCajaRequest request) {
        CierreCajaResponse creado = cierreCajaService.crearCierre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar cierres de caja")
    public ResponseEntity<List<CierreCajaResponse>> listar() {
        return ResponseEntity.ok(cierreCajaService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cierre por id")
    public ResponseEntity<CierreCajaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(cierreCajaService.obtenerPorId(id));
    }
}
