package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import co.edu.unbosque.backend.model.request.CrearProductoRequest;
import co.edu.unbosque.backend.model.request.IngresoProductoRequest;
import co.edu.unbosque.backend.model.response.CategoriaResponse;
import co.edu.unbosque.backend.model.response.ProductoResponse;
import co.edu.unbosque.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.bind.annotation.RequestParam;
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
     * Crea un producto.
     */
    @PostMapping
    @Operation(
            summary = "Crear producto",
            description = "Crea un producto con stock inicial obligatorio. Si se envia numeroLote, tambien crea el lote inicial y registra el movimiento de inventario."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Producto sin lote",
                                    value = """
                                            {
                                              "nombre": "Acetaminofen 500mg",
                                              "codigoBarras": "7701234567890",
                                              "stockMinimo": 10,
                                              "stockInicial": 20,
                                              "costo": 8500.0,
                                              "precioVenta": 12000.0,
                                              "estado": "ACTIVO"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Producto con lote",
                                    value = """
                                            {
                                              "categoriaId": 1,
                                              "nombre": "Amoxicilina 500mg",
                                              "codigoBarras": "7709876543210",
                                              "stockMinimo": 5,
                                              "stockInicial": 50,
                                              "costo": 15000.0,
                                              "precioVenta": 22000.0,
                                              "estado": "ACTIVO",
                                              "numeroLote": "AMX-2026-01"
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<ProductoResponse> guardarProducto(@Valid @org.springframework.web.bind.annotation.RequestBody CrearProductoRequest request) {
        Producto productoGuardado = productoService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProductoResponse(productoGuardado));
    }

    /**
     * Registra ingreso de stock para un producto existente usando el codigo de barras.
     */
    @PostMapping("/codigo-barras/{codigoBarras}/ingresos")
    @Operation(
            summary = "Ingresar stock por codigo de barras",
            description = "Aumenta el stock de un producto existente. Si se envia numeroLote, crea un nuevo lote. Si cambia el precio de venta, registra historial de precio."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Ingreso sin lote",
                                    value = """
                                            {
                                              "cantidad": 25
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Ingreso con lote y cambio de precio",
                                    value = """
                                            {
                                              "cantidad": 40,
                                              "numeroLote": "AMX-2026-02",
                                              "nuevoCosto": 16000.0,
                                              "nuevoPrecioVenta": 23500.0
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<ProductoResponse> ingresarStock(
            @PathVariable String codigoBarras,
            @Valid @org.springframework.web.bind.annotation.RequestBody IngresoProductoRequest request
    ) {
        return ResponseEntity.ok(toProductoResponse(productoService.ingresarStock(codigoBarras, request)));
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
     * Busca productos activos por nombre o código de barras.
     * Usado por el frontend al registrar una venta.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos activos por nombre o código")
    public ResponseEntity<List<ProductoResponse>> buscarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String codigo
    ) {
        String termino = nombre != null ? nombre : codigo;
        if (termino == null || termino.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(productoService.buscarActivosPorNombreOCodigo(termino)
                .stream()
                .map(this::toProductoResponse)
                .toList());
    }

    /**
     * Actualiza costo y precio de venta de un producto.
     */
    @PatchMapping("/codigo-barras/{codigoBarras}/precio")
    @Operation(
            summary = "Actualizar precio de producto por codigo de barras",
            description = "Actualiza costo y precio de venta usando el codigo de barras del producto. El usuario responsable se toma automaticamente desde la auditoria del sistema."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Cambio de precio",
                                    value = """
                                            {
                                              "nuevoCosto": 9000.0,
                                              "nuevoPrecioVenta": 13000.0,
                                              "motivo": "Ajuste por proveedor"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Cambio sin motivo",
                                    value = """
                                            {
                                              "nuevoCosto": 9000.0,
                                              "nuevoPrecioVenta": 13000.0
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<ProductoResponse> actualizarPrecio(
            @PathVariable String codigoBarras,
            @Valid @org.springframework.web.bind.annotation.RequestBody CambioPrecioProductoRequest request
    ) {
        return ResponseEntity.ok(toProductoResponse(productoService.actualizarPrecio(codigoBarras, request)));
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