package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
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
 * Controlador REST para proveedores.
 *
 * @author juanjo2748
 */
@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
@Tag(name = "Proveedores", description = "Gestión de proveedores de productos")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores activos")
    public ResponseEntity<List<ProveedorResponse>> listar() {
        return ResponseEntity.ok(proveedorService.listarActivos()
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por id")
    public ResponseEntity<ProveedorResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(proveedorService.obtenerPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Crear proveedor")
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody CrearProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(proveedorService.crear(request)));
    }

    private ProveedorResponse toResponse(Proveedor p) {
        return new ProveedorResponse(p.getIdProveedor(), p.getNombre(), p.getNit(),
                p.getContacto(), p.getTelefono(), p.getEmail(), p.getEstado());
    }
}
