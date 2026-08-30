package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.ActualizarDevolucionRequest;
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
@Tag(name = "Devoluciones", description = "Registro, consulta, actualización y eliminación de devoluciones a proveedores")
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

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar devolución a proveedor",
            description = "Permite actualizar campos no transaccionales (motivo, observaciones, estado). No altera inventario. El cambio de estado valida rol: empleados solo PENDIENTE↔ENVIADA y PENDIENTE→CERRADA; admin puede cualquier transición.")
    public ResponseEntity<DevolucionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarDevolucionRequest request,
            @RequestParam(required = false) String usuarioResponsable) {
        return ResponseEntity.ok(devolucionService.actualizar(id, request, usuarioResponsable));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de devolución a proveedor",
            description = "Cambio rápido de estado con control por rol. Valores: PENDIENTE | ENVIADA | ACEPTADA | RECHAZADA | CERRADA. Empleados solo PENDIENTE↔ENVIADA y PENDIENTE→CERRADA; ADMIN/REGENTE cualquier transición.")
    public ResponseEntity<DevolucionResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @RequestParam(required = false) String usuarioResponsable) {
        String estado = body != null ? body.get("estado") : null;
        // Compatibilidad: si front envía usuarioResponsable en body, úsalo
        if ((usuarioResponsable == null || usuarioResponsable.isBlank()) && body != null && body.get("usuarioResponsable") != null) {
            usuarioResponsable = body.get("usuarioResponsable");
        }
        return ResponseEntity.ok(devolucionService.cambiarEstado(id, estado, usuarioResponsable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar devolución a proveedor",
            description = "Revierte el inventario descontado (devuelve stock al producto y lote) y elimina el registro.")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) String usuarioResponsable) {
        devolucionService.eliminar(id, usuarioResponsable);
        return ResponseEntity.noContent().build();
    }
}
