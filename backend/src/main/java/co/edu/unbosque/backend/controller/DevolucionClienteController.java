package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.ActualizarDevolucionClienteRequest;
import co.edu.unbosque.backend.model.request.RegistrarDevolucionClienteRequest;
import co.edu.unbosque.backend.model.response.DevolucionClienteResponse;
import co.edu.unbosque.backend.service.DevolucionClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para devoluciones a clientes.
 */
@RestController
@RequestMapping("/api/devoluciones-clientes")
@CrossOrigin(origins = "*")
@Tag(name = "Devoluciones Clientes", description = "Registro, consulta, actualización y eliminación de devoluciones a clientes")
public class DevolucionClienteController {

    private final DevolucionClienteService devolucionClienteService;

    public DevolucionClienteController(DevolucionClienteService devolucionClienteService) {
        this.devolucionClienteService = devolucionClienteService;
    }

    @PostMapping
    @Operation(summary = "Registrar devolución a cliente")
    public ResponseEntity<DevolucionClienteResponse> registrar(
            @Valid @RequestBody RegistrarDevolucionClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(devolucionClienteService.registrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar devoluciones a cliente")
    public ResponseEntity<List<DevolucionClienteResponse>> listar() {
        return ResponseEntity.ok(devolucionClienteService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener devolución a cliente por id")
    public ResponseEntity<DevolucionClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(devolucionClienteService.obtener(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar devolución a cliente",
            description = "Permite actualizar datos informativos del cliente y el motivo. No altera inventario.")
    public ResponseEntity<DevolucionClienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarDevolucionClienteRequest request) {
        return ResponseEntity.ok(devolucionClienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar devolución a cliente",
            description = "Revierte el inventario repuesto (descuenta stock del producto y lote) y elimina el registro.")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) String usuarioResponsable) {
        devolucionClienteService.eliminar(id, usuarioResponsable);
        return ResponseEntity.noContent().build();
    }
}
