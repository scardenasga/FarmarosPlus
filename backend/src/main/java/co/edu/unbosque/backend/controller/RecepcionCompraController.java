package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.DetalleRecepcionCompra;
import co.edu.unbosque.backend.model.entity.RecepcionCompra;
import co.edu.unbosque.backend.model.request.ActualizarEstadoPagoRequest;
import co.edu.unbosque.backend.model.request.AgregarDetalleRecepcionRequest;
import co.edu.unbosque.backend.model.request.RegistrarRecepcionRequest;
import co.edu.unbosque.backend.model.response.DetalleRecepcionCompraResponse;
import co.edu.unbosque.backend.model.response.OrdenCompraResumenResponse;
import co.edu.unbosque.backend.model.response.RecepcionCompraResponse;
import co.edu.unbosque.backend.model.response.UsuarioResumenResponse;
import co.edu.unbosque.backend.service.RecepcionCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para operaciones de recepciones de compra.
 *
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/recepciones-compra")
@Tag(name = "Recepciones de Compra", description = "Operaciones de gestión de recepciones de compra")
public class RecepcionCompraController {

    private final RecepcionCompraService recepcionCompraService;

    public RecepcionCompraController(RecepcionCompraService recepcionCompraService) {
        this.recepcionCompraService = recepcionCompraService;
    }

    /**
     * Registra una nueva recepción de compra.
     */
    @PostMapping
    @Operation(summary = "Registrar recepción de compra")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Nueva Recepción",
                                    value = """
                                            {
                                              "ordenId": 1,
                                              "estado": "COMPLETA",
                                              "totalRecepcion": 50000.0,
                                              "observaciones": "Recepción sin novedad"
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<RecepcionCompraResponse> registrarRecepcion(
            @Valid @RequestBody RegistrarRecepcionRequest request
    ) {
        RecepcionCompra recepcionGuardada = recepcionCompraService.registrarRecepcion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toRecepcionResponse(recepcionGuardada));
    }

    /**
     * Agrega un detalle a una recepción de compra.
     */
    @PostMapping("/{recepcionId}/detalles")
    @Operation(summary = "Agregar detalle a recepción de compra")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Nuevo Detalle",
                                    value = """
                                            {
                                              "detalleOrdenId": 1,
                                              "productoId": 5,
                                              "cantidadRecibida": 100,
                                              "costoUnitarioReal": 148.5
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<DetalleRecepcionCompraResponse> agregarDetalleRecepcion(
            @PathVariable Long recepcionId,
            @Valid @RequestBody AgregarDetalleRecepcionRequest request
    ) {
        DetalleRecepcionCompra detalleGuardado = recepcionCompraService.agregarDetalleRecepcion(
                recepcionId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetalleResponse(detalleGuardado));
    }

    /**
     * Actualiza el estado de pago de una recepción.
     */
    @PatchMapping("/{recepcionId}/estado-pago")
    @Operation(summary = "Actualizar estado de pago de recepción")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Actualizar Pago",
                                    value = """
                                            {
                                              "estadoPago": "PAGADO",
                                              "montoPagado": 50000.0
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<RecepcionCompraResponse> actualizarEstadoPago(
            @PathVariable Long recepcionId,
            @Valid @RequestBody ActualizarEstadoPagoRequest request
    ) {
        RecepcionCompra recepcionActualizada = recepcionCompraService.actualizarEstadoPago(
                recepcionId,
                request.estadoPago(),
                request.montoPagado()
        );
        return ResponseEntity.ok(toRecepcionResponse(recepcionActualizada));
    }

    /**
     * Consulta una recepción de compra por su identificador.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar recepción de compra por id")
    public ResponseEntity<RecepcionCompraResponse> obtenerRecepcionPorId(@PathVariable Long id) {
        RecepcionCompra recepcion = recepcionCompraService.obtenerRecepcionPorId(id);
        return ResponseEntity.ok(toRecepcionResponse(recepcion));
    }

    /**
     * Lista recepciones pendientes de pago.
     */
    @GetMapping("/pendientes-pago")
    @Operation(summary = "Listar recepciones pendientes de pago")
    public ResponseEntity<List<RecepcionCompraResponse>> listarRecepcionesPendientes() {
        return ResponseEntity.ok(
                recepcionCompraService.listarRecepcionesPendientes()
                        .stream()
                        .map(this::toRecepcionResponse)
                        .toList()
        );
    }

    /**
     * Lista recepciones por orden de compra.
     */
    @GetMapping("/orden/{ordenId}")
    @Operation(summary = "Listar recepciones por orden de compra")
    public ResponseEntity<List<RecepcionCompraResponse>> listarRecepcionesPorOrden(
            @PathVariable Long ordenId
    ) {
        return ResponseEntity.ok(
                recepcionCompraService.listarRecepcionesPorOrden(ordenId)
                        .stream()
                        .map(this::toRecepcionResponse)
                        .toList()
        );
    }

    /**
     * Lista todas las recepciones de compra.
     */
    @GetMapping
    @Operation(summary = "Listar todas las recepciones de compra")
    public ResponseEntity<List<RecepcionCompraResponse>> listarTodasRecepciones() {
        return ResponseEntity.ok(
                recepcionCompraService.listarTodasRecepciones()
                        .stream()
                        .map(this::toRecepcionResponse)
                        .toList()
        );
    }

    private RecepcionCompraResponse toRecepcionResponse(RecepcionCompra recepcion) {
        return new RecepcionCompraResponse(
                recepcion.getIdRecepcion(),
                toOrdenResumenResponse(recepcion.getOrden()),
                toUsuarioResponse(recepcion.getUsuario()),
                recepcion.getFechaRecepcion(),
                recepcion.getObservaciones(),
                recepcion.getEstado(),
                recepcion.getTotalRecepcion(),
                recepcion.getEstadoPago(),
                recepcion.getMontoPagado(),
                recepcion.getFechaLimitePago(),
                recepcion.getDetalles() != null ?
                        recepcion.getDetalles().stream()
                                .map(d -> new DetalleRecepcionCompraResponse(
                                        d.getIdDetalleRecepcion(),
                                        d.getProducto() != null ? d.getProducto().getUniqueID() : null,
                                        d.getProducto() != null ? d.getProducto().getNombre() : "Producto eliminado",
                                        d.getCantidadRecibida(),
                                        d.getCostoUnitarioReal(),
                                        d.getCantidadRecibida() * (d.getCostoUnitarioReal() != null ? d.getCostoUnitarioReal() : 0.0),
                                        d.getObservaciones()
                                ))
                                .toList()
                        : List.of(),
                recepcion.getFechaCreacion(),
                recepcion.getFechaModificacion()
        );
    }

    private DetalleRecepcionCompraResponse toDetalleResponse(DetalleRecepcionCompra detalle) {
        return new DetalleRecepcionCompraResponse(
                detalle.getIdDetalleRecepcion(),
                detalle.getProducto() != null ? detalle.getProducto().getUniqueID() : null,
                detalle.getProducto() != null ? detalle.getProducto().getNombre() : "Producto eliminado",
                detalle.getCantidadRecibida(),
                detalle.getCostoUnitarioReal(),
                detalle.getCantidadRecibida() * (detalle.getCostoUnitarioReal() != null ? detalle.getCostoUnitarioReal() : 0.0),
                detalle.getObservaciones()
        );
    }

    private OrdenCompraResumenResponse toOrdenResumenResponse(co.edu.unbosque.backend.model.entity.OrdenCompra orden) {
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
