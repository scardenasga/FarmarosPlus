package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import co.edu.unbosque.backend.model.response.CategoriaResponse;
import co.edu.unbosque.backend.model.response.ProductoResponse;
import co.edu.unbosque.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para operaciones del catálogo de productos.
 *
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones del catalogo de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Crea o actualiza un producto.
     */
    @PostMapping
    @Operation(summary = "Crear o guardar producto")
    public ResponseEntity<ProductoResponse> guardarProducto(@Valid @RequestBody Producto producto) {
        Producto productoGuardado = productoService.guardarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProductoResponse(productoGuardado));
    }

    /**
     * Consulta un producto por identificador.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar producto por id")
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toProductoResponse(productoService.obtenerProductoPorId(id)));
    }

    /**
     * Lista productos activos.
     */
    @GetMapping("/activos")
    @Operation(summary = "Listar productos activos")
    public ResponseEntity<List<ProductoResponse>> listarActivos() {
        return ResponseEntity.ok(productoService.listarProductosActivos()
                .stream()
                .map(this::toProductoResponse)
                .toList());
    }

    /**
     * Lista productos con stock bajo.
     */
    @GetMapping("/stock-bajo")
    @Operation(summary = "Listar productos con stock bajo")
    public ResponseEntity<List<ProductoResponse>> listarProductosConStockBajo() {
        return ResponseEntity.ok(productoService.listarProductosConStockBajo()
                .stream()
                .map(this::toProductoResponse)
                .toList());
    }

    /**
     * Actualiza costo y precio de venta de un producto.
     */
    @PatchMapping("/{id}/precio")
    @Operation(summary = "Actualizar precio de producto")
    public ResponseEntity<ProductoResponse> actualizarPrecio(
            @PathVariable Long id,
            @Valid @RequestBody CambioPrecioProductoRequest request
    ) {
        CambioPrecioProductoRequest normalizedRequest = new CambioPrecioProductoRequest(
                id,
                request.nuevoCosto(),
                request.nuevoPrecioVenta(),
                request.motivo(),
                request.usuarioResponsable()
        );
        return ResponseEntity.ok(toProductoResponse(productoService.actualizarPrecio(normalizedRequest)));
    }

    private ProductoResponse toProductoResponse(Producto producto) {
        return new ProductoResponse(
                producto.getUniqueID(),
                toCategoriaResponse(producto.getCategoria()),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCodigoBarras(),
                producto.getStockMinimo(),
                producto.getStockActual(),
                producto.getCosto(),
                producto.getPrecioVenta(),
                producto.getMargenGanancia(),
                producto.getEstado()
        );
    }

    private CategoriaResponse toCategoriaResponse(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
