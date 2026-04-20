package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.ActualizarConfiguracionAlertaRequest;
import co.edu.unbosque.backend.model.response.AlertaResponse;
import co.edu.unbosque.backend.model.response.ConfiguracionAlertaResponse;
import co.edu.unbosque.backend.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    /**
     * Genera alertas por stock bajo y vencimiento próximo, evitando duplicados.
     * Retorna las alertas no leídas resultantes.
     */
    @PostMapping("/generar")
    @Operation(
        summary = "Generar alertas de inventario",
        description = "No requiere cuerpo. Escanea automáticamente productos con stock bajo y lotes próximos a vencer."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false, content = @Content)
    public ResponseEntity<List<AlertaResponse>> generarAlertas() {
        return ResponseEntity.ok(alertaService.generarAlertas());
    }

    /**
     * Lista alertas. Con soloNoLeidas=true retorna solo las pendientes.
     */
    @GetMapping
    @Operation(summary = "Listar alertas de inventario")
    public ResponseEntity<List<AlertaResponse>> listarAlertas(
            @RequestParam(defaultValue = "false") boolean soloNoLeidas) {
        return ResponseEntity.ok(alertaService.listarAlertas(soloNoLeidas));
    }

    /**
     * Marca una alerta como leída.
     */
    @PatchMapping("/{id}/leer")
    @Operation(summary = "Marcar alerta como leída")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        alertaService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca todas las alertas no leídas como leídas.
     */
    @PatchMapping("/leer-todas")
    @Operation(summary = "Marcar todas las alertas como leídas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        alertaService.marcarTodasComoLeidas();
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna la configuración actual de alertas.
     */
    @GetMapping("/configuracion")
    @Operation(summary = "Obtener configuración de alertas")
    public ResponseEntity<ConfiguracionAlertaResponse> obtenerConfiguracion() {
        return ResponseEntity.ok(alertaService.obtenerConfiguracion());
    }

    /**
     * Actualiza el umbral de días para vencimiento próximo.
     */
    @PutMapping("/configuracion")
    @Operation(summary = "Actualizar configuración de alertas")
    public ResponseEntity<ConfiguracionAlertaResponse> actualizarConfiguracion(
            @Valid @RequestBody ActualizarConfiguracionAlertaRequest request) {
        return ResponseEntity.ok(alertaService.actualizarConfiguracion(request));
    }
}
