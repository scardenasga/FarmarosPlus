package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.DevolucionProveedorRequest;
import co.edu.unbosque.backend.model.response.DevolucionProveedorResponse;
import co.edu.unbosque.backend.service.DevolucionProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para devoluciones de productos a proveedores.
 *
 * @author juanjo2748
 */
@RestController
@RequestMapping("/api/devoluciones")
@CrossOrigin(origins = "*")
@Tag(name = "Devoluciones", description = "Registro de devoluciones de productos a proveedores")
public class DevolucionProveedorController {

    private final DevolucionProveedorService devolucionService;

    public DevolucionProveedorController(DevolucionProveedorService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @PostMapping
    @Operation(summary = "Registrar devolución a proveedor",
               description = "Registra la devolución y descuenta automáticamente el inventario")
    public ResponseEntity<DevolucionProveedorResponse> registrarDevolucion(
            @Valid @RequestBody DevolucionProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(devolucionService.registrarDevolucion(request));
    }

    @GetMapping
    @Operation(summary = "Listar devoluciones", description = "Con idProveedor filtra por proveedor")
    public ResponseEntity<List<DevolucionProveedorResponse>> listarDevoluciones(
            @RequestParam(required = false) Long idProveedor) {
        List<DevolucionProveedorResponse> lista = idProveedor != null
                ? devolucionService.listarPorProveedor(idProveedor)
                : devolucionService.listarDevoluciones();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener devolución con detalle completo")
    public ResponseEntity<DevolucionProveedorResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(devolucionService.obtenerPorId(id));
    }
}
