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
import co.edu.unbosque.backend.service.FacturaService;
import co.edu.unbosque.backend.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para operaciones del módulo de ventas.
 */
@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Operaciones del modulo POS para registrar y consultar ventas")
public class VentaController {

    private final VentaService ventaService;
    private final FacturaService facturaService;

    public VentaController(VentaService ventaService, FacturaService facturaService) {
        this.ventaService = ventaService;
        this.facturaService = facturaService;
    }

    @PostMapping
    @Operation(summary = "Registrar venta")
    public ResponseEntity<VentaResponse> registrarVenta(@Valid @RequestBody CrearVentaRequest request) {
        Venta ventaGuardada = ventaService.registrarVenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toVentaResponse(ventaGuardada));
    }
    @GetMapping("/historico")
@Operation(summary = "Consultar histórico de ventas")
public ResponseEntity<List<VentaResponse>> consultarHistorico(
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaInicio,
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaFin,
        @RequestParam(required = false) Long idVendedor,
        @RequestParam(required = false) String estado,
        @RequestHeader("X-Username") String username
) {
    LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
    LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;
    HistoricoFiltroRequest filtros = new HistoricoFiltroRequest(inicio, fin, idVendedor, estado);
    List<Venta> ventas = ventaService.consultarHistorico(filtros, username);
    return ResponseEntity.ok(ventas.stream().map(this::toVentaResponse).toList());
}

    @GetMapping("/{id}")
    @Operation(summary = "Consultar venta por id")
    public ResponseEntity<VentaResponse> obtenerVenta(@PathVariable Long id) {
        return ResponseEntity.ok(toVentaResponse(ventaService.obtenerVentaDetallada(id)));
    }

    /**
     * Lista el histórico de ventas con filtros opcionales
     */
   

    /**
     * Descarga la factura de una venta en formato PDF.
     */
    @GetMapping(value = "/{id}/factura", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar factura en PDF")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Long id) {
        Venta venta = ventaService.obtenerVentaDetallada(id);
        byte[] pdf = facturaService.generarFactura(venta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"factura-" + String.format("%06d", id) + ".pdf\"")
                .body(pdf);
    }

    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular venta")
    public ResponseEntity<VentaResponse> anularVenta(
            @PathVariable Long id,
            @Valid @RequestBody AnularVentaRequest request
    ) {
        return ResponseEntity.ok(toVentaResponse(ventaService.eliminarVenta(id, request)));
    }

    private VentaResponse toVentaResponse(Venta venta) {
        return new VentaResponse(
                venta.getIdVenta(),
                toUsuarioResponse(venta.getUsuario()),
                venta.getFecha(),
                venta.getEstado(),
                venta.getMotivoAnulacion(),
                venta.getSubtotal(),
                venta.getIva(),
                venta.getDescuento(),
                venta.getTotal(),
                venta.getCambio(),
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
                detalle.getSubtotalLinea(),
                detalle.getIvaLinea()
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
                producto.getPorcentajeIva(),
                producto.getRequierePrescripcion(),
                producto.getEstado()
        );
    }

    private CategoriaResponse toCategoriaResponse(Categoria categoria) {
        if (categoria == null) return null;
        return new CategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}