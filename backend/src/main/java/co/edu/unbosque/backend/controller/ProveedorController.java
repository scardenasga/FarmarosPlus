package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
import co.edu.unbosque.backend.model.response.ProveedorResponse;
import co.edu.unbosque.backend.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para operaciones de proveedores.
 *
 * @author juanjo2748
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
@Tag(name = "Proveedores", description = "Operaciones de gestión de proveedores")
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
    /**
     * Crea un nuevo proveedor.
     */
    @PostMapping
    @Operation(summary = "Crear proveedor")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Nuevo Proveedor",
                                    value = """
                                            {
                                              "nombre": "Farmacéutica XYZ S.A.",
                                              "nit": "860123456-7",
                                              "telefono": "+57 1 1234567",
                                              "email": "contacto@farmaxyza.com",
                                              "contacto": "Juan Pérez",
                                              "condicionPago": "Neto 30"
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<ProveedorResponse> crearProveedor(
            @Valid @RequestBody CrearProveedorRequest request
    ) {
        Proveedor proveedorGuardado = proveedorService.crearProveedor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProveedorResponse(proveedorGuardado));
    }

    /**
     * Consulta un proveedor por su identificador.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar proveedor por id")
    public ResponseEntity<ProveedorResponse> obtenerProveedorPorId(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.obtenerProveedorPorId(id);
        return ResponseEntity.ok(toProveedorResponse(proveedor));
    }

    /**
     * Lista proveedores activos.
     */
    @GetMapping("/activos")
    @Operation(summary = "Listar proveedores activos")
    public ResponseEntity<List<ProveedorResponse>> listarProveedoresActivos() {
        return ResponseEntity.ok(
                proveedorService.listarProveedoresActivos()
                        .stream()
                        .map(this::toProveedorResponse)
                        .toList()
        );
    }

    /**
     * Lista todos los proveedores.
     */
    @GetMapping
    @Operation(summary = "Listar todos los proveedores")
    public ResponseEntity<List<ProveedorResponse>> listarTodosProveedores() {
        return ResponseEntity.ok(
                proveedorService.listarTodosProveedores()
                        .stream()
                        .map(this::toProveedorResponse)
                        .toList()
        );
    }

    /**
     * Actualiza el estado de un proveedor.
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del proveedor")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Cambiar estado",
                                    value = """
                                            {
                                              "estado": "INACTIVO"
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<ProveedorResponse> actualizarEstadoProveedor(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoProveedorRequest request
    ) {
        Proveedor proveedorActualizado = proveedorService.actualizarEstadoProveedor(id, request);
        return ResponseEntity.ok(toProveedorResponse(proveedorActualizado));
    }

    private ProveedorResponse toProveedorResponse(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getIdProveedor(),
                proveedor.getNombre(),
                proveedor.getNit(),
                proveedor.getTelefono(),
                proveedor.getEmail(),
                proveedor.getContacto(),
                proveedor.getEstado(),
                proveedor.getCondicionPago(),
                proveedor.getFechaCreacion(),
                proveedor.getFechaModificacion()
        );
    }
}
