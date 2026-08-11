package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.RegistrarDevolucionRequest;
import co.edu.unbosque.backend.model.response.DevolucionResponse;
import co.edu.unbosque.backend.service.DevolucionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para devoluciones a proveedores.
 *
 * @author juanjo2748
 */
@RestController
@RequestMapping("/api/devoluciones")
@CrossOrigin(origins = "*")
@Tag(name = "Devoluciones", description = "Registro y consulta de devoluciones a proveedores")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @PostMapping
    @Operation(summary = "Registrar devolución a proveedor")
    public ResponseEntity<DevolucionResponse> registrar(
            @Valid @RequestBody RegistrarDevolucionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(devolucionService.registrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las devoluciones")
    public ResponseEntity<List<DevolucionResponse>> listar() {
        return ResponseEntity.ok(devolucionService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener devolución por id")
    public ResponseEntity<DevolucionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(devolucionService.obtener(id));
    }
}
