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
import co.edu.unbosque.backend.model.request.DetalleDevolucionRequest;
import co.edu.unbosque.backend.model.request.RegistrarDevolucionRequest;
import co.edu.unbosque.backend.model.response.DetalleDevolucionResponse;
import co.edu.unbosque.backend.model.response.DevolucionResponse;
import co.edu.unbosque.backend.repository.DevolucionProveedorRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio transaccional para devoluciones de productos a proveedor.
 * Actualiza inventario y registra movimiento en la misma transacción.
 *
 * @author juanjo2748
 */
@Service
public class DevolucionService {

    private final DevolucionProveedorRepository devolucionRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final co.edu.unbosque.backend.repository.UsuarioRepository usuarioRepository;

    public DevolucionService(DevolucionProveedorRepository devolucionRepository,
                             ProveedorRepository proveedorRepository,
                             ProductoRepository productoRepository,
                             LoteRepository loteRepository,
                             MovimientoInventarioRepository movimientoRepository,
                             co.edu.unbosque.backend.repository.UsuarioRepository usuarioRepository) {
        this.devolucionRepository = devolucionRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public DevolucionResponse registrar(RegistrarDevolucionRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.idProveedor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el proveedor con id " + request.idProveedor()));

        co.edu.unbosque.backend.model.entity.Usuario usuario = usuarioRepository.findByUsername(request.usuarioResponsable())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario con username " + request.usuarioResponsable()));

        DevolucionProveedor devolucion = new DevolucionProveedor();
        devolucion.setProveedor(proveedor);
        devolucion.setUsuario(usuario);
        devolucion.setUsuarioResponsable(request.usuarioResponsable());
        devolucion.setFecha(LocalDateTime.now());
        devolucion.setMotivo(request.motivo());

        List<DetalleDevolucionProveedor> detalles = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();

        for (DetalleDevolucionRequest detalleReq : request.detalles()) {
            Lote lote = loteRepository.findByIdForUpdate(detalleReq.idLote())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el lote con id " + detalleReq.idLote()));

            Producto producto = productoRepository.findByIdForUpdate(detalleReq.idProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el producto con id " + detalleReq.idProducto()));

            if (!lote.getProducto().getUniqueID().equals(producto.getUniqueID())) {
                throw new BusinessException("El lote " + lote.getNumeroLote()
                        + " no pertenece al producto " + producto.getNombre());
            }

            if (lote.getCantidad() < detalleReq.cantidad()) {
                throw new InsufficientStockException(
                        "Stock insuficiente en el lote " + lote.getNumeroLote()
                        + ". Disponible: " + lote.getCantidad());
            }

            int stockAnterior = producto.getStockActual();
            int stockNuevo = stockAnterior - detalleReq.cantidad();
            int cantLoteNuevo = lote.getCantidad() - detalleReq.cantidad();

            producto.setStockActual(stockNuevo);
            lote.setCantidad(cantLoteNuevo);

            DetalleDevolucionProveedor detalle = new DetalleDevolucionProveedor();
            detalle.setDevolucion(devolucion);
            detalle.setProducto(producto);
            detalle.setLote(lote);
            detalle.setNombreProducto(producto.getNombre());
            detalle.setNumeroLote(lote.getNumeroLote());
            detalle.setCantidad(detalleReq.cantidad());
            detalles.add(detalle);

            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(producto);
            mov.setNombreProducto(producto.getNombre());
            mov.setLote(lote);
            mov.setTipoMovimiento("DEVOLUCION");
            mov.setCantidadAnterior(stockAnterior);
            mov.setCantidadNueva(stockNuevo);
            mov.setDiferencia(stockNuevo - stockAnterior);
            mov.setMotivo("Devolución a proveedor: " + proveedor.getNombre());
            mov.setUsuarioResponsable(request.usuarioResponsable());
            movimientos.add(mov);
        }

        devolucion.setDetalles(detalles);
        DevolucionProveedor guardada = devolucionRepository.save(devolucion);

        String refDoc = "DEV-PROV-" + guardada.getIdDevolucion();
        movimientos.forEach(m -> m.setReferenciaDocumento(refDoc));
        movimientoRepository.saveAll(movimientos);

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<DevolucionResponse> listar() {
        return devolucionRepository.findAllByOrderByFechaDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DevolucionResponse obtener(Long id) {
        DevolucionProveedor dev = devolucionRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la devolución con id " + id));
        return toResponse(dev);
    }

    private DevolucionResponse toResponse(DevolucionProveedor d) {
        List<DetalleDevolucionResponse> detallesResp = d.getDetalles().stream()
                .map(det -> new DetalleDevolucionResponse(
                        det.getProducto().getUniqueID(),
                        det.getNombreProducto(),
                        det.getNumeroLote(),
                        det.getCantidad()))
                .toList();
        return new DevolucionResponse(
                d.getIdDevolucion(),
                d.getProveedor().getIdProveedor(),
                d.getProveedor().getNombre(),
                d.getUsuarioResponsable(),
                d.getMotivo(),
                d.getFecha(),
                detallesResp);
    }
}
