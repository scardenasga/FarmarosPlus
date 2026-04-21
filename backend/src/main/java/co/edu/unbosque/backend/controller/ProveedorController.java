package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.ProveedorRequest;
import co.edu.unbosque.backend.model.response.ProveedorResponse;
import co.edu.unbosque.backend.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de proveedores.
 *
 * @author juanjo2748
 */
@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
@Tag(name = "Proveedores", description = "Gestión de proveedores de productos farmacéuticos")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping
    @Operation(summary = "Crear proveedor")
    public ResponseEntity<ProveedorResponse> crearProveedor(
            @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(proveedorService.crearProveedor(request));
    }

    @GetMapping
    @Operation(summary = "Listar proveedores", description = "Con soloActivos=true retorna solo los activos")
    public ResponseEntity<List<ProveedorResponse>> listarProveedores(
            @RequestParam(defaultValue = "false") boolean soloActivos) {
        List<ProveedorResponse> lista = soloActivos
                ? proveedorService.listarProveedoresActivos()
                : proveedorService.listarProveedores();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<ProveedorResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor")
    public ResponseEntity<ProveedorResponse> actualizarProveedor(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(proveedorService.actualizarProveedor(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar proveedor")
    public ResponseEntity<Void> desactivarProveedor(@PathVariable Long id) {
        proveedorService.desactivarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}
