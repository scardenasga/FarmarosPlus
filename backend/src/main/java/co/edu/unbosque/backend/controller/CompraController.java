package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.request.RegistrarCompraRequest;
import co.edu.unbosque.backend.model.response.CompraResponse;
import co.edu.unbosque.backend.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para compras a proveedores.
 *
 * @author juanjo2748
 */
@RestController
@RequestMapping("/api/compras")
@CrossOrigin(origins = "*")
@Tag(name = "Compras", description = "Registro y consulta de compras a proveedores")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    @Operation(summary = "Registrar compra a proveedor")
    public ResponseEntity<CompraResponse> registrar(
            @Valid @RequestBody RegistrarCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.registrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar compras con filtro opcional por proveedor")
    public ResponseEntity<List<CompraResponse>> listar(
            @RequestParam(required = false) Long idProveedor) {
        return ResponseEntity.ok(compraService.listar(idProveedor));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener compra por id")
    public ResponseEntity<CompraResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtener(id));
    }
}
