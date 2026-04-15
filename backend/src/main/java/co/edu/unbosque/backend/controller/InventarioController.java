package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.AjusteInventarioRequest;
import co.edu.unbosque.backend.model.request.IngresoLoteRequest;
import co.edu.unbosque.backend.model.response.CategoriaResponse;
import co.edu.unbosque.backend.model.response.LoteResponse;
import co.edu.unbosque.backend.model.response.MovimientoInventarioResponse;
import co.edu.unbosque.backend.model.response.ProductoResponse;
import co.edu.unbosque.backend.service.InventarioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para operaciones de inventario.
 *
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Operaciones para lotes, ajustes y movimientos de inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * Registra el ingreso de un lote nuevo al inventario.
     */
    @PostMapping("/lotes")
    @Operation(summary = "Registrar ingreso de lote")
    public ResponseEntity<LoteResponse> registrarIngresoLote(@Valid @RequestBody IngresoLoteRequest request) {
        Lote loteGuardado = inventarioService.registrarIngresoLote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toLoteResponse(loteGuardado));
    }

    /**
     * Aplica un ajuste de inventario.
     */
    @PatchMapping("/ajustes")
    @Operation(summary = "Aplicar ajuste de inventario")
    public ResponseEntity<LoteResponse> ajustarInventario(@Valid @RequestBody AjusteInventarioRequest request) {
        return ResponseEntity.ok(toLoteResponse(inventarioService.ajustarInventario(request)));
    }

    /**
     * Lista lotes disponibles de un producto.
     */
    @GetMapping("/productos/{productoId}/lotes-disponibles")
    @Operation(summary = "Listar lotes disponibles por producto")
    public ResponseEntity<List<LoteResponse>> listarLotesDisponibles(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.listarLotesDisponiblesPorProducto(productoId)
                .stream()
                .map(this::toLoteResponse)
                .toList());
    }

    /**
     * Lista lotes que vencen hasta una fecha dada.
     */
    @GetMapping("/lotes/proximos-a-vencer")
    @Operation(summary = "Listar lotes proximos a vencer")
    public ResponseEntity<List<LoteResponse>> listarLotesProximosAVencer(@RequestParam LocalDate fechaCorte) {
        return ResponseEntity.ok(inventarioService.listarLotesProximosAVencer(fechaCorte)
                .stream()
                .map(this::toLoteResponse)
                .toList());
    }

    /**
     * Consulta el historial de movimientos de un producto.
     */
    @GetMapping("/productos/{productoId}/movimientos")
    @Operation(summary = "Listar movimientos de inventario por producto")
    public ResponseEntity<List<MovimientoInventarioResponse>> listarMovimientosPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.listarMovimientosPorProducto(productoId)
                .stream()
                .map(this::toMovimientoResponse)
                .toList());
    }

    private LoteResponse toLoteResponse(Lote lote) {
        return new LoteResponse(
                lote.getIdLote(),
                lote.getNumeroLote(),
                lote.getFechaVencimiento(),
                lote.getCantidad(),
                toProductoResponse(lote.getProducto())
        );
    }

    private MovimientoInventarioResponse toMovimientoResponse(MovimientoInventario movimiento) {
        return new MovimientoInventarioResponse(
                movimiento.getIdMovimiento(),
                movimiento.getProducto() != null ? movimiento.getProducto().getUniqueID() : null,
                movimiento.getNombreProducto(),
                movimiento.getLote() != null ? movimiento.getLote().getIdLote() : null,
                movimiento.getTipoMovimiento(),
                movimiento.getCantidadAnterior(),
                movimiento.getCantidadNueva(),
                movimiento.getDiferencia(),
                movimiento.getMotivo(),
                movimiento.getReferenciaDocumento(),
                movimiento.getUsuarioResponsable(),
                movimiento.getFechaMovimiento()
        );
    }

    private ProductoResponse toProductoResponse(Producto producto) {
        if (producto == null) {
            return null;
        }
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
