package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.request.CrearCategoriaRequest;
import co.edu.unbosque.backend.model.response.CategoriaResponse;
import co.edu.unbosque.backend.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para categorias de producto.
 *
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Operaciones para gestionar categorias de productos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * Crea una categoria.
     */
    @PostMapping
    @Operation(summary = "Crear categoria")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CrearCategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toCategoriaResponse(categoriaService.crearCategoria(request)));
    }

    /**
     * Consulta una categoria por id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria por id")
    public ResponseEntity<CategoriaResponse> obtenerCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(toCategoriaResponse(categoriaService.obtenerCategoriaPorId(id)));
    }

    /**
     * Lista categorias registradas.
     */
    @GetMapping
    @Operation(summary = "Listar categorias")
    public ResponseEntity<List<CategoriaResponse>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias()
                .stream()
                .map(this::toCategoriaResponse)
                .toList());
    }

    private CategoriaResponse toCategoriaResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
