package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.AlertaResponse;
import co.edu.unbosque.backend.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para alertas automáticas de inventario.
 *
 * @author juanjo2748
 */
@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*")
@Tag(name = "Alertas", description = "Generación y gestión de alertas de inventario")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @PostMapping("/generar")
    @Operation(
        summary = "Generar alertas de inventario",
        description = "Escanea productos con stock bajo y lotes próximos a vencer. Evita duplicados."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false, content = @Content)
    public ResponseEntity<List<AlertaResponse>> generarAlertas() {
        return ResponseEntity.ok(alertaService.generarAlertas());
    }

    @GetMapping
    @Operation(summary = "Listar alertas")
    public ResponseEntity<List<AlertaResponse>> listarAlertas(
            @RequestParam(defaultValue = "false") boolean soloNoLeidas) {
        return ResponseEntity.ok(alertaService.listarAlertas(soloNoLeidas));
    }

    @PatchMapping("/{id}/leer")
    @Operation(summary = "Marcar alerta como leída")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        alertaService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/leer-todas")
    @Operation(summary = "Marcar todas las alertas como leídas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        alertaService.marcarTodasComoLeidas();
        return ResponseEntity.noContent().build();
    }
}
