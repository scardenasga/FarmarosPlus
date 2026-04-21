package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.InsufficientStockException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.DetalleDevolucionProveedor;
import co.edu.unbosque.backend.model.entity.DevolucionProveedor;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.request.DevolucionProveedorRequest;
import co.edu.unbosque.backend.model.response.DevolucionProveedorResponse;
import co.edu.unbosque.backend.repository.DevolucionProveedorRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio transaccional para devoluciones de productos a proveedores.
 * Registra la devolución, descuenta el inventario y deja trazabilidad
 * en movimiento_inventario con tipo DEVOLUCION.
 *
 * @author juanjo2748
 */
@Service
public class DevolucionProveedorService {

    private final DevolucionProveedorRepository devolucionRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final CurrentUserService currentUserService;

    public DevolucionProveedorService(
            DevolucionProveedorRepository devolucionRepository,
            ProveedorRepository proveedorRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            LoteRepository loteRepository,
            MovimientoInventarioRepository movimientoRepository,
            CurrentUserService currentUserService
    ) {
        this.devolucionRepository = devolucionRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Registra una devolución a proveedor de forma atómica.
     * Por cada línea: descuenta el lote (si aplica) y el stock del producto,
     * y deja un movimiento con tipo DEVOLUCION.
     * Si cualquier paso falla, Spring revierte toda la transacción.
     *
     * @param request datos de la devolución
     * @return devolución registrada con sus detalles
     */
    @Transactional
    public DevolucionProveedorResponse registrarDevolucion(DevolucionProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.idProveedor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el proveedor con id " + request.idProveedor()));

        if (!"ACTIVO".equals(proveedor.getEstado())) {
            throw new BusinessException("El proveedor está inactivo y no puede recibir devoluciones");
        }

        String usernameActual = currentUserService.getCurrentUsername();
        Usuario usuario = usuarioRepository.findByUsername(usernameActual)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario en sesión: " + usernameActual));

        DevolucionProveedor devolucion = new DevolucionProveedor();
        devolucion.setProveedor(proveedor);
        devolucion.setUsuario(usuario);
        devolucion.setMotivo(request.motivo());
        devolucion.setEstado("PROCESADA");

        DevolucionProveedor devolucionGuardada = devolucionRepository.save(devolucion);

        for (DevolucionProveedorRequest.DetalleDevolucionRequest detalleReq : request.detalles()) {
            procesarDetalle(devolucionGuardada, detalleReq, usernameActual);
        }

        return toResponse(devolucionRepository.findWithDetallesById(devolucionGuardada.getIdDevolucion())
                .orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<DevolucionProveedorResponse> listarDevoluciones() {
        return devolucionRepository.findAllByOrderByFechaDesc()
                .stream().map(this::toResponseSimple).toList();
    }

    @Transactional(readOnly = true)
    public List<DevolucionProveedorResponse> listarPorProveedor(Long idProveedor) {
        return devolucionRepository.findByProveedor_IdProveedorOrderByFechaDesc(idProveedor)
                .stream().map(this::toResponseSimple).toList();
    }

    @Transactional(readOnly = true)
    public DevolucionProveedorResponse obtenerPorId(Long id) {
        DevolucionProveedor dev = devolucionRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la devolución con id " + id));
        return toResponse(dev);
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private void procesarDetalle(
            DevolucionProveedor devolucion,
            DevolucionProveedorRequest.DetalleDevolucionRequest req,
            String usernameActual
    ) {
        Producto producto = productoRepository.findByIdForUpdate(req.idProducto())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto con id " + req.idProducto()));

        if (!"ACTIVO".equals(producto.getEstado())) {
            throw new BusinessException("El producto '" + producto.getNombre() + "' no está activo");
        }

        int stockAnterior = seguro(producto.getStockActual());
        int stockNuevo = stockAnterior - req.cantidad();

        if (stockNuevo < 0) {
            throw new InsufficientStockException(
                    "Stock insuficiente para devolver " + req.cantidad()
                    + " unidades de '" + producto.getNombre() + "'");
        }

        Lote lote = null;
        String numeroLoteSnapshot = null;

        if (req.idLote() != null) {
            lote = loteRepository.findByIdForUpdate(req.idLote())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el lote con id " + req.idLote()));

            if (!lote.getProducto().getUniqueID().equals(producto.getUniqueID())) {
                throw new BusinessException("El lote " + req.idLote()
                        + " no pertenece al producto " + producto.getNombre());
            }

            int cantidadLoteNueva = seguro(lote.getCantidad()) - req.cantidad();
            if (cantidadLoteNueva < 0) {
                throw new InsufficientStockException(
                        "El lote '" + lote.getNumeroLote() + "' no tiene stock suficiente");
            }

            numeroLoteSnapshot = lote.getNumeroLote();
            lote.setCantidad(cantidadLoteNueva);
            loteRepository.save(lote);
        }

        producto.setStockActual(stockNuevo);
        productoRepository.save(producto);

        DetalleDevolucionProveedor detalle = new DetalleDevolucionProveedor();
        detalle.setDevolucion(devolucion);
        detalle.setProducto(producto);
        detalle.setNombreProducto(producto.getNombre());
        detalle.setLote(lote);
        detalle.setNumeroLote(numeroLoteSnapshot);
        detalle.setCantidad(req.cantidad());
        detalle.setMotivo(req.motivo());
        devolucion.getDetalles().add(detalle);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setNombreProducto(producto.getNombre());
        movimiento.setLote(lote);
        movimiento.setTipoMovimiento("DEVOLUCION");
        movimiento.setCantidadAnterior(stockAnterior);
        movimiento.setCantidadNueva(stockNuevo);
        movimiento.setDiferencia(-req.cantidad());
        movimiento.setMotivo(req.motivo() != null ? req.motivo()
                : "Devolución a proveedor: " + devolucion.getProveedor().getNombre());
        movimiento.setReferenciaDocumento("DEV-" + devolucion.getIdDevolucion());
        movimiento.setUsuarioResponsable(usernameActual);
        movimientoRepository.save(movimiento);
    }

    private DevolucionProveedorResponse toResponse(DevolucionProveedor d) {
        List<DevolucionProveedorResponse.DetalleDevolucionResponse> detallesResp =
                d.getDetalles().stream().map(det -> new DevolucionProveedorResponse.DetalleDevolucionResponse(
                        det.getIdDetalle(),
                        det.getProducto().getUniqueID(),
                        det.getNombreProducto(),
                        det.getLote() != null ? det.getLote().getIdLote() : null,
                        det.getNumeroLote(),
                        det.getCantidad(),
                        det.getMotivo()
                )).toList();

        return new DevolucionProveedorResponse(
                d.getIdDevolucion(),
                d.getProveedor().getIdProveedor(),
                d.getProveedor().getNombre(),
                d.getUsuario().getIdUsuario(),
                d.getUsuario().getNombreCompleto(),
                d.getFecha(),
                d.getEstado(),
                d.getMotivo(),
                detallesResp
        );
    }

    private DevolucionProveedorResponse toResponseSimple(DevolucionProveedor d) {
        return new DevolucionProveedorResponse(
                d.getIdDevolucion(),
                d.getProveedor().getIdProveedor(),
                d.getProveedor().getNombre(),
                d.getUsuario().getIdUsuario(),
                d.getUsuario().getNombreCompleto(),
                d.getFecha(),
                d.getEstado(),
                d.getMotivo(),
                List.of()
        );
    }

    private int seguro(Integer valor) {
        return valor == null ? 0 : valor;
    }
}
