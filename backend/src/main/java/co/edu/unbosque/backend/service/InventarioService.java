package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.InsufficientStockException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.model.request.AjusteInventarioRequest;
import co.edu.unbosque.backend.model.request.IngresoLoteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio transaccional del módulo de inventario.
 * Coordina cambios de stock entre producto, lote y movimiento histórico.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final CurrentUserService currentUserService;

    public InventarioService(
            ProductoRepository productoRepository,
            LoteRepository loteRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            CurrentUserService currentUserService
    ) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Registra un nuevo lote, incrementa el stock del producto y crea el movimiento
     * histórico dentro de una sola transacción.
     *
     * @param request datos del ingreso del lote
     * @return lote creado
     */
    @Transactional
    public Lote registrarIngresoLote(IngresoLoteRequest request) {
        validarIngresoLote(request);

        Producto producto = productoRepository.findByIdForUpdate(request.productoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto con id " + request.productoId()
                ));

        int stockAnterior = valorSeguro(producto.getStockActual());

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setNumeroLote(request.numeroLote());
        lote.setFechaVencimiento(request.fechaVencimiento());
        lote.setCantidad(request.cantidad());

        producto.setStockActual(stockAnterior + request.cantidad());

        Lote loteGuardado = loteRepository.save(lote);
        productoRepository.save(producto);

        MovimientoInventario movimiento = construirMovimiento(
                producto,
                loteGuardado,
                "COMPRA",
                stockAnterior,
                producto.getStockActual(),
                request.motivo(),
                request.referenciaDocumento(),
                currentUserService.getCurrentUsername()
        );
        movimientoInventarioRepository.save(movimiento);

        return loteGuardado;
    }

    /**
     * Ajusta el stock de un lote y del producto padre en una sola transacción.
     * Si cualquier paso falla, Spring revierte la operación completa.
     *
     * @param request datos del ajuste
     * @return lote ajustado
     */
    @Transactional
    public Lote ajustarInventario(AjusteInventarioRequest request) {
        validarAjusteInventario(request);

        Lote lote = loteRepository.findByIdForUpdate(request.loteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el lote con id " + request.loteId()
                ));
        Producto producto = productoRepository.findByIdForUpdate(lote.getProducto().getUniqueID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto asociado al lote " + request.loteId()
                ));

        int cantidadLoteNueva = valorSeguro(lote.getCantidad()) + request.diferencia();
        int stockProductoAnterior = valorSeguro(producto.getStockActual());
        int stockProductoNuevo = stockProductoAnterior + request.diferencia();

        if (cantidadLoteNueva < 0) {
            throw new InsufficientStockException("El lote no tiene stock suficiente para aplicar el ajuste");
        }
        if (stockProductoNuevo < 0) {
            throw new InsufficientStockException("El producto no tiene stock suficiente para aplicar el ajuste");
        }

        lote.setCantidad(cantidadLoteNueva);
        producto.setStockActual(stockProductoNuevo);

        Lote loteActualizado = loteRepository.save(lote);
        productoRepository.save(producto);

        MovimientoInventario movimiento = construirMovimiento(
                producto,
                loteActualizado,
                request.tipoMovimiento(),
                stockProductoAnterior,
                stockProductoNuevo,
                request.motivo(),
                request.referenciaDocumento(),
                currentUserService.getCurrentUsername()
        );
        movimientoInventarioRepository.save(movimiento);

        return loteActualizado;
    }

    /**
     * Lista lotes disponibles para venta o rotación FEFO.
     *
     * @param productoId identificador del producto
     * @return lotes del producto con cantidad positiva
     */
    @Transactional(readOnly = true)
    public List<Lote> listarLotesDisponiblesPorProducto(Long productoId) {
        return loteRepository.findLotesDisponiblesPorProducto(productoId);
    }

    /**
     * Lista lotes próximos a vencer dentro del rango indicado.
     *
     * @param fechaCorte fecha máxima de vencimiento
     * @return lotes que vencen hasta esa fecha
     */
    @Transactional(readOnly = true)
    public List<Lote> listarLotesProximosAVencer(LocalDate fechaCorte) {
        return loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(LocalDate.now(), fechaCorte);
    }

    /**
     * Recupera la bitácora de movimientos de un producto.
     *
     * @param productoId identificador del producto
     * @return movimientos registrados
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarMovimientosPorProducto(Long productoId) {
        return movimientoInventarioRepository.findByProducto_UniqueIDOrderByFechaMovimientoDesc(productoId);
    }

    private MovimientoInventario construirMovimiento(
            Producto producto,
            Lote lote,
            String tipoMovimiento,
            int cantidadAnterior,
            int cantidadNueva,
            String motivo,
            String referenciaDocumento,
            String usuarioResponsable
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setNombreProducto(producto.getNombre());
        movimiento.setLote(lote);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setCantidadAnterior(cantidadAnterior);
        movimiento.setCantidadNueva(cantidadNueva);
        movimiento.setDiferencia(cantidadNueva - cantidadAnterior);
        movimiento.setMotivo(motivo);
        movimiento.setReferenciaDocumento(referenciaDocumento);
        movimiento.setUsuarioResponsable(usuarioResponsable);
        return movimiento;
    }

    private void validarIngresoLote(IngresoLoteRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de ingreso de lote es obligatoria");
        }
        if (request.productoId() == null) {
            throw new BusinessException("El id del producto es obligatorio");
        }
        if (request.numeroLote() == null || request.numeroLote().isBlank()) {
            throw new BusinessException("El número de lote es obligatorio");
        }
        if (request.cantidad() == null || request.cantidad() <= 0) {
            throw new BusinessException("La cantidad del lote debe ser mayor a cero");
        }
    }

    private void validarAjusteInventario(AjusteInventarioRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de ajuste es obligatoria");
        }
        if (request.loteId() == null) {
            throw new BusinessException("El id del lote es obligatorio");
        }
        if (request.diferencia() == null || request.diferencia() == 0) {
            throw new BusinessException("La diferencia del ajuste no puede ser cero");
        }
        if (request.tipoMovimiento() == null || request.tipoMovimiento().isBlank()) {
            throw new BusinessException("El tipo de movimiento es obligatorio");
        }
    }

    private int valorSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }
}
