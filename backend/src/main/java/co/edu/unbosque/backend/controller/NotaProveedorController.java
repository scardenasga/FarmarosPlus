package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.NotaProveedor;
import co.edu.unbosque.backend.model.request.CrearNotaProveedorRequest;
import co.edu.unbosque.backend.model.response.NotaProveedorResponse;
import co.edu.unbosque.backend.service.NotaProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para reclamos y observaciones de proveedor.
 */
@RestController
@RequestMapping("/api/proveedores/{idProveedor}/notas")
@CrossOrigin(origins = "*")
@Tag(name = "Notas de proveedor", description = "Registro de reclamos y observaciones sobre un proveedor")
public class NotaProveedorController {

    private final NotaProveedorService notaProveedorService;

    public NotaProveedorController(NotaProveedorService notaProveedorService) {
        this.notaProveedorService = notaProveedorService;
    }

    @PostMapping
    @Operation(summary = "Registrar reclamo u observación de proveedor")
    public ResponseEntity<NotaProveedorResponse> crearNota(
            @PathVariable Long idProveedor,
            @Valid @RequestBody CrearNotaProveedorRequest request
    ) {
        NotaProveedor nota = notaProveedorService.crearNota(idProveedor, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(nota));
    }

    @GetMapping
    @Operation(summary = "Listar reclamos y observaciones de un proveedor")
    public ResponseEntity<List<NotaProveedorResponse>> listarNotas(@PathVariable Long idProveedor) {
        return ResponseEntity.ok(
                notaProveedorService.listarPorProveedor(idProveedor)
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private NotaProveedorResponse toResponse(NotaProveedor nota) {
        return new NotaProveedorResponse(
                nota.getIdNota(),
                nota.getTipoNota(),
                nota.getTitulo(),
                nota.getDescripcion(),
                nota.getFechaCreacion(),
                nota.getUsuarioCreacion(),
                nota.getOrdenRelacionada() != null ? nota.getOrdenRelacionada().getIdOrden() : null
        );
    }
}
