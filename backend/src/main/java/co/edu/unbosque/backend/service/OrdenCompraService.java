package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.DetalleOrdenCompra;
import co.edu.unbosque.backend.model.entity.OrdenCompra;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.request.AgregarDetalleOrdenRequest;
import co.edu.unbosque.backend.model.request.CrearOrdenCompraRequest;
import co.edu.unbosque.backend.repository.DetalleOrdenCompraRepository;
import co.edu.unbosque.backend.repository.OrdenCompraRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicación para gestión de órdenes de compra.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CurrentUserService currentUserService;

    public OrdenCompraService(
            OrdenCompraRepository ordenCompraRepository,
            DetalleOrdenCompraRepository detalleOrdenCompraRepository,
            ProveedorRepository proveedorRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            CurrentUserService currentUserService
    ) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.detalleOrdenCompraRepository = detalleOrdenCompraRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public OrdenCompra crearOrdenCompra(CrearOrdenCompraRequest request) {
        validarCreacionOrden(request);

        Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe proveedor con id " + request.proveedorId()
                ));

        String usuarioActual = currentUserService.getCurrentUsername();
        Usuario usuario = usuarioRepository.findByUsername(usuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario actual no encontrado"
                ));

        OrdenCompra orden = new OrdenCompra();
        orden.setProveedor(proveedor);
        orden.setUsuario(usuario);
        orden.setFechaPedido(LocalDateTime.now());
        orden.setFechaEsperada(request.fechaEsperada());
        orden.setEstado("PENDIENTE");
        orden.setTotalEsperado(request.totalEsperado() != null ? request.totalEsperado() : 0.0);
        orden.setObservaciones(request.observaciones());

        return ordenCompraRepository.save(orden);
    }

    @Transactional
    public DetalleOrdenCompra agregarDetalleOrden(Long ordenId, AgregarDetalleOrdenRequest request) {
        validarAgregarDetalle(request);

        OrdenCompra orden = obtenerOrdenPorId(ordenId);

        if (!"PENDIENTE".equals(orden.getEstado())) {
            throw new BusinessException("No se pueden agregar detalles a una orden que no está en estado PENDIENTE");
        }

        Producto producto = null;
        if (request.productoId() != null) {
            producto = productoRepository.findById(request.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe producto con id " + request.productoId()
                    ));
        }

        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setOrden(orden);
        detalle.setProducto(producto);
        detalle.setNombreProducto(request.nombreProducto());
        detalle.setDescripcionProducto(request.descripcionProducto());
        detalle.setCantidadPedida(request.cantidadPedida());
        detalle.setPrecioUnitarioPactado(request.precioUnitarioPactado());

        DetalleOrdenCompra detalleGuardado = detalleOrdenCompraRepository.save(detalle);

        orden.setTotalEsperado(orden.getTotalEsperado() + 
                (request.cantidadPedida() * request.precioUnitarioPactado()));
        ordenCompraRepository.save(orden);

        return detalleGuardado;
    }

    @Transactional(readOnly = true)
    public OrdenCompra obtenerOrdenPorId(Long ordenId) {
        return ordenCompraRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe orden de compra con id " + ordenId
                ));
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> listarOrdenesPendientes() {
        return ordenCompraRepository.findByEstadoOrderByFechaPedidoDesc("PENDIENTE");
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> listarOrdenesPorProveedor(Long proveedorId) {
        return ordenCompraRepository.findByProveedor_IdProveedorOrderByFechaPedidoDesc(proveedorId);
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> listarTodasOrdenes() {
        return ordenCompraRepository.findAll();
    }

    private void validarCreacionOrden(CrearOrdenCompraRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de creación de orden es obligatoria");
        }
        if (request.proveedorId() == null) {
            throw new BusinessException("El ID del proveedor es obligatorio (proveedorId)");
        }
        if (request.totalEsperado() != null && request.totalEsperado() <= 0) {
            throw new BusinessException("El total esperado debe ser mayor a cero");
        }
    }

    private void validarAgregarDetalle(AgregarDetalleOrdenRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de detalle es obligatoria");
        }
        if (request.cantidadPedida() == null || request.cantidadPedida() <= 0) {
            throw new BusinessException("La cantidad pedida debe ser mayor a cero");
        }
        if (request.precioUnitarioPactado() == null || request.precioUnitarioPactado() <= 0) {
            throw new BusinessException("El precio unitario pactado debe ser mayor a cero");
        }
    }
}
