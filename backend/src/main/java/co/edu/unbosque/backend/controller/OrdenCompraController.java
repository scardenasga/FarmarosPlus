package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.DetalleOrdenCompra;
import co.edu.unbosque.backend.model.entity.OrdenCompra;
import co.edu.unbosque.backend.model.request.AgregarDetalleOrdenRequest;
import co.edu.unbosque.backend.model.request.CrearOrdenCompraRequest;
import co.edu.unbosque.backend.model.request.OrdenCompraConfirmacionRequest;
import co.edu.unbosque.backend.model.response.DetalleOrdenCompraResponse;
import co.edu.unbosque.backend.model.response.OrdenCompraPreviewResponse;
import co.edu.unbosque.backend.model.response.OrdenCompraResponse;
import co.edu.unbosque.backend.model.response.OrdenCompraResumenResponse;
import co.edu.unbosque.backend.model.response.ProveedorResponse;
import co.edu.unbosque.backend.model.response.ResumenAlertasComprasResponse;
import co.edu.unbosque.backend.model.response.UsuarioResumenResponse;
import co.edu.unbosque.backend.service.OrdenCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controlador REST para operaciones de órdenes de compra.
 *
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
@RestController
@RequestMapping("/api/ordenes-compra")
@Tag(name = "Órdenes de Compra", description = "Operaciones de gestión de órdenes de compra")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    public OrdenCompraController(OrdenCompraService ordenCompraService) {
        this.ordenCompraService = ordenCompraService;
    }

    /**
     * Crea una nueva orden de compra.
     */
    @PostMapping
    @Operation(summary = "Crear orden de compra")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Nueva Orden",
                                    value = """
                                            {
                                              "proveedorId": 1,
                                              "totalEsperado": 50000.0,
                                              "observaciones": "Envío urgente requerido"
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<OrdenCompraResponse> crearOrdenCompra(
            @Valid @RequestBody CrearOrdenCompraRequest request
    ) {
        OrdenCompra ordenGuardada = ordenCompraService.crearOrdenCompra(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toOrdenCompraResponse(ordenGuardada));
    }

    @PostMapping("/confirmar")
    @Operation(summary = "Crear orden de compra con sus detalles")
    public ResponseEntity<OrdenCompraResponse> confirmarOrden(
            @Valid @RequestBody OrdenCompraConfirmacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toOrdenCompraResponse(ordenCompraService.confirmarOrden(request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Editar una orden de compra pendiente")
    public ResponseEntity<OrdenCompraResponse> actualizarOrden(
            @PathVariable Long id, @Valid @RequestBody OrdenCompraConfirmacionRequest request) {
        return ResponseEntity.ok(toOrdenCompraResponse(ordenCompraService.actualizarOrden(id, request)));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @Operation(summary = "Cancelar una orden de compra")
    public ResponseEntity<Void> cancelarOrden(@PathVariable Long id) {
        ordenCompraService.cancelarOrden(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agrega un detalle a una orden de compra.
     */
    @PostMapping("/{ordenId}/detalles")
    @Operation(summary = "Agregar detalle a orden de compra")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Nuevo Detalle",
                                    value = """
                                            {
                                              "productoId": 5,
                                              "nombreProducto": "Dipirona 500mg",
                                              "cantidadPedida": 100,
                                              "precioUnitarioPactado": 150.0
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<DetalleOrdenCompraResponse> agregarDetalleOrden(
            @PathVariable Long ordenId,
            @Valid @RequestBody AgregarDetalleOrdenRequest request
    ) {
        DetalleOrdenCompra detalleGuardado = ordenCompraService.agregarDetalleOrden(ordenId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetalleResponse(detalleGuardado));
    }

    /**
     * Consulta una orden de compra por su identificador.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar orden de compra por id")
    public ResponseEntity<OrdenCompraResponse> obtenerOrdenPorId(@PathVariable Long id) {
        OrdenCompra orden = ordenCompraService.obtenerOrdenPorId(id);
        return ResponseEntity.ok(toOrdenCompraResponse(orden));
    }

    /**
     * Enpoint para obtener el resumen de seguimiento y alertas de compras.
     */
   @GetMapping("/resumen-seguimiento")
    public ResponseEntity<ResumenAlertasComprasResponse> getResumenSeguimiento() {
        return ResponseEntity.ok(ordenCompraService.obtenerResumenAlertasSeguimiento());
    }

    /**
     * Lista órdenes de compra pendientes.
     */
    @GetMapping("/pendientes")
    @Operation(summary = "Listar órdenes de compra pendientes")
    public ResponseEntity<List<OrdenCompraResumenResponse>> listarOrdenesPendientes() {
        return ResponseEntity.ok(
                ordenCompraService.listarOrdenesPendientes()
                        .stream()
                        .map(this::toOrdenCompraResumenResponse)
                        .toList()
        );
    }

    /**
     * Lista órdenes de compra por proveedor.
     */
    @GetMapping("/proveedor/{proveedorId}")
    @Operation(summary = "Listar órdenes de compra por proveedor")
    public ResponseEntity<List<OrdenCompraResumenResponse>> listarOrdenesPorProveedor(
            @PathVariable Long proveedorId
    ) {
        return ResponseEntity.ok(
                ordenCompraService.listarOrdenesPorProveedor(proveedorId)
                        .stream()
                        .map(this::toOrdenCompraResumenResponse)
                        .toList()
        );
    }

    /**
     * Lista todas las órdenes de compra.
     */
    @GetMapping
    @Operation(summary = "Listar todas las órdenes de compra")
    public ResponseEntity<List<OrdenCompraResumenResponse>> listarTodasOrdenes() {
        return ResponseEntity.ok(
                ordenCompraService.listarTodasOrdenes()
                        .stream()
                        .map(this::toOrdenCompraResumenResponse)
                        .toList()
        );
    }

    @GetMapping("/previsualizar-propuesta/{idProveedor}") // 1. Asegúrate que aquí diga idProveedor
@Operation(summary = "Previsualizar orden de compra")
public ResponseEntity<OrdenCompraPreviewResponse> previsualizarOrden(
    @PathVariable("idProveedor") Long idProveedor) { // 2. IMPORTANTE: Pon "idProveedor" dentro del paréntesis
    return ResponseEntity.ok(ordenCompraService.generarPrevisualizacion(idProveedor));
}

    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    

    private OrdenCompraResponse toOrdenCompraResponse(OrdenCompra orden) {
        return new OrdenCompraResponse(
                orden.getIdOrden(),
                toProveedorResponse(orden.getProveedor()),
                toUsuarioResponse(orden.getUsuario()),
                orden.getFechaPedido(),
                orden.getFechaEsperada(),
                orden.getEstado(),
                orden.getTotalEsperado(),
                orden.getObservaciones(),
                orden.getDetalles() != null ? 
                        orden.getDetalles().stream()
                                .map(d -> new DetalleOrdenCompraResponse(
                                        d.getIdDetalle(),
                                        d.getProducto() != null ? d.getProducto().getUniqueID() : null,
                                        d.getNombreProducto(),
                                        d.getDescripcionProducto(),
                                        d.getCantidadPedida(),
                                        d.getPrecioUnitarioPactado(),
                                        d.getCantidadPedida() * d.getPrecioUnitarioPactado()
                                ))
                                .toList() 
                        : List.of(),
                orden.getFechaCreacion(),
                orden.getFechaModificacion()
        );
    }

    private OrdenCompraResumenResponse toOrdenCompraResumenResponse(OrdenCompra orden) {
        return new OrdenCompraResumenResponse(
                orden.getIdOrden(),
                orden.getProveedor().getIdProveedor(),
                orden.getProveedor().getNombre(),
                orden.getFechaPedido(),
                orden.getFechaEsperada(),
                orden.getEstado(),
                orden.getTotalEsperado()
        );
    }

    private DetalleOrdenCompraResponse toDetalleResponse(DetalleOrdenCompra detalle) {
        return new DetalleOrdenCompraResponse(
                detalle.getIdDetalle(),
                detalle.getProducto() != null ? detalle.getProducto().getUniqueID() : null,
                detalle.getNombreProducto(),
                detalle.getDescripcionProducto(),
                detalle.getCantidadPedida(),
                detalle.getPrecioUnitarioPactado(),
                detalle.getCantidadPedida() * detalle.getPrecioUnitarioPactado()
        );
    }

    private ProveedorResponse toProveedorResponse(co.edu.unbosque.backend.model.entity.Proveedor proveedor) {
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

    private UsuarioResumenResponse toUsuarioResponse(co.edu.unbosque.backend.model.entity.Usuario usuario) {
        return new UsuarioResumenResponse(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getEstado()
        );
    }

    
}
