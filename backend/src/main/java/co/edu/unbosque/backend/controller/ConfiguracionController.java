package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.ConfiguracionGananciaRequest;
import co.edu.unbosque.backend.model.response.ConfiguracionGananciaResponse;
import co.edu.unbosque.backend.service.ConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
@Tag(name = "Configuración", description = "Parámetros configurables del sistema")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/ganancia")
    @Operation(summary = "Obtener configuración de ganancia mínima")
    public ResponseEntity<ConfiguracionGananciaResponse> obtenerGanancia() {
        return ResponseEntity.ok(configuracionService.obtenerConfiguracionGanancia());
    }

    @PutMapping("/ganancia")
    @Operation(summary = "Actualizar ganancia mínima por defecto")
    public ResponseEntity<ConfiguracionGananciaResponse> actualizarGanancia(
            @Valid @RequestBody ConfiguracionGananciaRequest request) {
        return ResponseEntity.ok(configuracionService.actualizarGananciaMinima(request));
    }
}
