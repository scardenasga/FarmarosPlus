package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.PagoVenta;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.model.request.AnularVentaRequest;
import co.edu.unbosque.backend.model.request.CrearVentaRequest;
import co.edu.unbosque.backend.model.request.HistoricoFiltroRequest;
import co.edu.unbosque.backend.model.response.CategoriaResponse;
import co.edu.unbosque.backend.model.response.DetalleVentaResponse;
import co.edu.unbosque.backend.model.response.LoteResumenResponse;
import co.edu.unbosque.backend.model.response.PagoVentaResponse;
import co.edu.unbosque.backend.model.response.ProductoResponse;
import co.edu.unbosque.backend.model.response.UsuarioResumenResponse;
import co.edu.unbosque.backend.model.response.VentaResponse;
import co.edu.unbosque.backend.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import co.edu.unbosque.backend.model.request.HistoricoFiltroRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para operaciones del módulo de ventas.
 *
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Operaciones del modulo POS para registrar y consultar ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Registra una venta completa.
     */
    @PostMapping
    @Operation(summary = "Registrar venta")
    public ResponseEntity<VentaResponse> registrarVenta(@Valid @RequestBody CrearVentaRequest request) {
        Venta ventaGuardada = ventaService.registrarVenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toVentaResponse(ventaGuardada));
    }

    /**
     * Consulta una venta con su detalle completo.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar venta por id")
    public ResponseEntity<VentaResponse> obtenerVenta(@PathVariable Long id) {
        return ResponseEntity.ok(toVentaResponse(ventaService.obtenerVentaDetallada(id)));
    }

    /**
     * Lista el histórico de ventas con filtros opcionales
     * Requiere permisos
    */
    @GetMapping("/historico")
    @Operation(summary = "Consultar histórico de ventas")
    public ResponseEntity<List<VentaResponse>> consultarHistorico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) Long idVendedor,
            @RequestParam(required = false) String estado,
            @RequestHeader("X-Username") String username
    ) {
        HistoricoFiltroRequest filtros = new HistoricoFiltroRequest(fechaInicio, fechaFin, idVendedor, estado);
        List<Venta> ventas = ventaService.consultarHistorico(filtros, username);
        return ResponseEntity.ok(ventas.stream().map(this::toVentaResponse).toList());
    }

    /**
     * Anula una venta existente.
     */
    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular venta")
    public ResponseEntity<VentaResponse> anularVenta(
            @PathVariable Long id,
            @Valid @RequestBody AnularVentaRequest request
    ) {
       return ResponseEntity.ok(toVentaResponse(
            ventaService.eliminarVenta(id, request)
    ));
}

    private VentaResponse toVentaResponse(Venta venta) {
        return new VentaResponse(
                venta.getIdVenta(),
                toUsuarioResponse(venta.getUsuario()),
                venta.getFecha(),
                venta.getEstado(),
                venta.getMotivoAnulacion(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getTotal(),
                venta.getDetalles().stream().map(this::toDetalleResponse).toList(),
                venta.getPagos().stream().map(this::toPagoResponse).toList()
        );
    }

    private DetalleVentaResponse toDetalleResponse(DetalleVenta detalle) {
        return new DetalleVentaResponse(
                detalle.getIdDetalle(),
                toProductoResponse(detalle.getProducto()),
                toLoteResumen(detalle.getLote()),
                detalle.getCantidad(),
                detalle.getPrecioUnitarioAplicado(),
                detalle.getSubtotalLinea()
        );
    }

    private PagoVentaResponse toPagoResponse(PagoVenta pago) {
        return new PagoVentaResponse(
                pago.getIdPago(),
                pago.getTipo(),
                pago.getMonto()
        );
    }

    private UsuarioResumenResponse toUsuarioResponse(Usuario usuario) {
        return new UsuarioResumenResponse(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getEstado()
        );
    }

    private LoteResumenResponse toLoteResumen(Lote lote) {
        return new LoteResumenResponse(
                lote.getIdLote(),
                lote.getNumeroLote(),
                lote.getFechaVencimiento()
        );
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