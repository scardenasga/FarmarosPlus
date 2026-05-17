package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.ProveedorProducto;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProductoProveedorRequest;
import co.edu.unbosque.backend.model.request.ActualizarProveedorRequest;
import co.edu.unbosque.backend.model.request.AsociarProductoProveedorRequest;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
import co.edu.unbosque.backend.model.response.ProductoProveedorResponse;
import co.edu.unbosque.backend.model.response.ProveedorConsultaResponse;
import co.edu.unbosque.backend.model.response.ProveedorDetalleResponse;
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
    public ResponseEntity<ProveedorResponse> crearProveedor(@Valid @RequestBody CrearProveedorRequest request) {
        Proveedor proveedorGuardado = proveedorService.crearProveedor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProveedorResponse(proveedorGuardado));
    }

    /**
     * Consulta un proveedor por su identificador.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar proveedor por id")
    public ResponseEntity<ProveedorConsultaResponse> obtenerProveedorPorId(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.obtenerProveedorPorId(id);
        return ResponseEntity.ok(toProveedorConsultaResponse(proveedor));
    }

    /**
     * Consulta un proveedor por su identificador con sus productos asociados.
     */
    @GetMapping("/{id}/detalle")
    @Operation(summary = "Consultar proveedor por id con productos")
    public ResponseEntity<ProveedorDetalleResponse> obtenerProveedorDetallePorId(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerProveedorDetallePorId(id));
    }

    /**
     * Lista proveedores activos.
     */
    @GetMapping("/activos")
    @Operation(summary = "Listar proveedores activos")
    public ResponseEntity<List<ProveedorConsultaResponse>> listarProveedoresActivos() {
        return ResponseEntity.ok(
                proveedorService.listarProveedoresActivos()
                        .stream()
                        .map(this::toProveedorConsultaResponse)
                        .toList()
        );
    }

    /**
     * Lista todos los proveedores.
     */
    @GetMapping
    @Operation(summary = "Listar todos los proveedores")
    public ResponseEntity<List<ProveedorConsultaResponse>> listarTodosProveedores() {
        return ResponseEntity.ok(
                proveedorService.listarTodosProveedores()
                        .stream()
                        .map(this::toProveedorConsultaResponse)
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

    /**
     * Actualiza datos del proveedor.
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar proveedor")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Editar proveedor",
                                    value = """
                                            {
                                              "nombre": "Farmacéutica XYZ Actualizada",
                                              "telefono": "+57 601 7654321",
                                              "email": "compras@farmaxyz.com",
                                              "contacto": "Ana Gómez",
                                              "condicionPago": "Contra Entrega"
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<ProveedorResponse> actualizarProveedor(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarProveedorRequest request
    ) {
        return ResponseEntity.ok(toProveedorResponse(proveedorService.actualizarProveedor(id, request)));
    }

    /**
     * Asocia un producto a un proveedor.
     */
    @PostMapping("/{id}/productos")
    @Operation(summary = "Asociar producto a proveedor")
    public ResponseEntity<ProductoProveedorResponse> asociarProducto(
            @PathVariable Long id,
            @Valid @RequestBody AsociarProductoProveedorRequest request
    ) {
        ProveedorProducto relacion = proveedorService.asociarProducto(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProductoProveedorResponse(relacion));
    }

    /**
     * Actualiza el estado de una relación proveedor-producto.
     */
    @PatchMapping("/{id}/productos/{productoId}/estado")
    @Operation(summary = "Actualizar estado de producto asociado al proveedor")
    public ResponseEntity<ProductoProveedorResponse> actualizarEstadoProductoProveedor(
            @PathVariable Long id,
            @PathVariable Long productoId,
            @Valid @RequestBody ActualizarEstadoProductoProveedorRequest request
    ) {
        ProveedorProducto relacion = proveedorService.actualizarEstadoProductoProveedor(id, productoId, request);
        return ResponseEntity.ok(toProductoProveedorResponse(relacion));
    }

    /**
     * Elimina una relación proveedor-producto.
     */
    @DeleteMapping("/{id}/productos/{productoId}")
    @Operation(summary = "Eliminar producto asociado al proveedor")
    public ResponseEntity<Void> eliminarProductoProveedor(
            @PathVariable Long id,
            @PathVariable Long productoId
    ) {
        proveedorService.eliminarProductoProveedor(id, productoId);
        return ResponseEntity.noContent().build();
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

    private ProveedorConsultaResponse toProveedorConsultaResponse(Proveedor proveedor) {
        return new ProveedorConsultaResponse(
                proveedor.getIdProveedor(),
                proveedor.getNombre(),
                proveedor.getNit(),
                proveedor.getTelefono(),
                proveedor.getEmail(),
                proveedor.getContacto(),
                proveedor.getEstado(),
                proveedor.getCondicionPago()
        );
    }

    private ProductoProveedorResponse toProductoProveedorResponse(ProveedorProducto relacion) {
        return new ProductoProveedorResponse(
                relacion.getProducto().getUniqueID(),
                relacion.getProducto().getNombre(),
                relacion.getProducto().getDescripcion(),
                relacion.getProducto().getCodigoBarras(),
                relacion.getProducto().getEstado(),
                relacion.getCodigoProductoProveedor(),
                relacion.getPrecioReferencia(),
                relacion.getEstado()
        );
    }
}
