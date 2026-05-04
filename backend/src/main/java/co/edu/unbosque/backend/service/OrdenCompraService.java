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
import co.edu.unbosque.backend.model.response.OrdenCompraPreviewResponse;
import co.edu.unbosque.backend.model.response.ResumenAlertasComprasResponse;
import co.edu.unbosque.backend.repository.DetalleOrdenCompraRepository;
import co.edu.unbosque.backend.repository.OrdenCompraRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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


/**
 * Obtiene el resumen de alertas de seguimiento para el módulo de compras.
 * 
 */
@Transactional(readOnly = true)
public ResumenAlertasComprasResponse obtenerResumenAlertasSeguimiento() {
    // 1. Buscamos todas las órdenes (usando el repository de Sebastián)
    List<OrdenCompra> todasLasOrdenes = ordenCompraRepository.findAll();
    LocalDateTime ahora = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Calculamos los contadores superiores
    long total = todasLasOrdenes.size();
    long pendientesCount = todasLasOrdenes.stream().filter(o -> "PENDIENTE".equals(o.getEstado())).count();
    long vencidasCount = todasLasOrdenes.stream().filter(o -> "NO_RECIBIDA".equals(o.getEstado())).count();

    // Creamos la lista detallada de alertas (tu bloque de código corregido)
    List<ResumenAlertasComprasResponse.AlertaOrdenDetalle> alertas = todasLasOrdenes.stream()
        .filter(o -> "PENDIENTE".equals(o.getEstado()) || "NO_RECIBIDA".equals(o.getEstado())) 
        .map(o -> {
            long dias = ChronoUnit.DAYS.between(o.getFechaPedido(), ahora);
            String codigo = "OC-" + o.getFechaPedido().getYear() + "-" + String.format("%03d", o.getIdOrden());

            return new ResumenAlertasComprasResponse.AlertaOrdenDetalle(
                o.getIdOrden(),
                codigo,
                o.getProveedor().getNombre(), 
                o.getEstado(),
                o.getFechaPedido().format(formatter),
                dias
            );
        })
        .collect(Collectors.toList());

    // Retornamos el DTO estructurado
    return new ResumenAlertasComprasResponse(total, pendientesCount, vencidasCount, alertas);
}
@Transactional(readOnly = true)
public OrdenCompraPreviewResponse generarPrevisualizacion(Long proveedorId) {
    Proveedor prov = proveedorRepository.findById(proveedorId)
            .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

    // Lógica de recomendación: Productos con stock bajo
    List<OrdenCompraPreviewResponse.ItemPreview> itemsSugeridos = productoRepository.findAll().stream()
            .filter(p -> "ACTIVO".equals(p.getEstado()) && p.getStockActual() <= p.getStockMinimo())
            .map(p -> new OrdenCompraPreviewResponse.ItemPreview(
                p.getUniqueID(),
                p.getNombre(),
                (p.getStockMinimo() * 2) - p.getStockActual(), // Cantidad sugerida
                p.getCosto(),
                ((p.getStockMinimo() * 2) - p.getStockActual()) * p.getCosto(),
                "Recomendación automática por stock bajo"
            )).toList();

    Double total = itemsSugeridos.stream().mapToDouble(OrdenCompraPreviewResponse.ItemPreview::subtotal).sum();

    return new OrdenCompraPreviewResponse(prov.getIdProveedor(), prov.getNombre(), itemsSugeridos, total);
}

}