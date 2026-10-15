package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.DetalleOrdenCompra;
import co.edu.unbosque.backend.model.entity.DetalleRecepcionCompra;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.OrdenCompra;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.RecepcionCompra;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.request.AgregarDetalleRecepcionRequest;
import co.edu.unbosque.backend.model.request.RegistrarRecepcionRequest;
import co.edu.unbosque.backend.repository.DetalleOrdenCompraRepository;
import co.edu.unbosque.backend.repository.DetalleRecepcionCompraRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.OrdenCompraRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.RecepcionCompraRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicación para gestión de recepciones de compra.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class RecepcionCompraService {

    private final RecepcionCompraRepository recepcionCompraRepository;
    private final DetalleRecepcionCompraRepository detalleRecepcionCompraRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;

    public RecepcionCompraService(
            RecepcionCompraRepository recepcionCompraRepository,
            DetalleRecepcionCompraRepository detalleRecepcionCompraRepository,
            OrdenCompraRepository ordenCompraRepository,
            DetalleOrdenCompraRepository detalleOrdenCompraRepository,
            ProductoRepository productoRepository,
            LoteRepository loteRepository,
            UsuarioRepository usuarioRepository,
            CurrentUserService currentUserService
    ) {
        this.recepcionCompraRepository = recepcionCompraRepository;
        this.detalleRecepcionCompraRepository = detalleRecepcionCompraRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.detalleOrdenCompraRepository = detalleOrdenCompraRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public RecepcionCompra registrarRecepcion(RegistrarRecepcionRequest request) {
        validarRegistroRecepcion(request);

        OrdenCompra orden = ordenCompraRepository.findById(request.ordenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe orden de compra con id " + request.ordenId()
                ));

        if (!"PENDIENTE".equals(orden.getEstado())) {
            throw new BusinessException("No se puede registrar una recepción para una orden en estado " + orden.getEstado());
        }

        String usuarioActual = currentUserService.getCurrentUsername();
        Usuario usuario = usuarioRepository.findByUsername(usuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario actual no encontrado"
                ));

        RecepcionCompra recepcion = new RecepcionCompra();
        recepcion.setOrden(orden);
        recepcion.setUsuario(usuario);
        recepcion.setFechaRecepcion(LocalDateTime.now());
        recepcion.setObservaciones(request.observaciones());
        recepcion.setEstado(request.estado().toUpperCase());
        recepcion.setTotalRecepcion(request.totalRecepcion());
        recepcion.setEstadoPago("PENDIENTE");
        recepcion.setMontoPagado(0.0);

        // Actualizar el estado de la orden de compra según el tipo de recepción
        if ("COMPLETA".equalsIgnoreCase(request.estado())) {
            orden.setEstado("CERRADA");
        } else if ("RECHAZADA".equalsIgnoreCase(request.estado())) {
            orden.setEstado("NO_RECIBIDA");
        }
        // Si es PARCIAL, la orden permanece PENDIENTE para permitir más recepciones

        // Si la orden no tenia fecha esperada, se toma la fecha de la recepcion:
        // es el dia en que realmente se aprobo/recibio la compra.
        if (orden.getFechaEsperada() == null) {
            orden.setFechaEsperada(recepcion.getFechaRecepcion());
        }

        ordenCompraRepository.save(orden);
        return recepcionCompraRepository.save(recepcion);
    }

    @Transactional
    public DetalleRecepcionCompra agregarDetalleRecepcion(
            Long recepcionId,
            AgregarDetalleRecepcionRequest request
    ) {
        validarAgregarDetalleRecepcion(request);

        RecepcionCompra recepcion = obtenerRecepcionPorId(recepcionId);

        DetalleOrdenCompra detalleOrden = detalleOrdenCompraRepository.findById(request.detalleOrdenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe detalle de orden con id " + request.detalleOrdenId()
                ));

        Producto producto = null;
        if (request.productoId() != null) {
            producto = productoRepository.findById(request.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe producto con id " + request.productoId()
                    ));
        }

        DetalleRecepcionCompra detalle = new DetalleRecepcionCompra();
        detalle.setRecepcion(recepcion);
        detalle.setDetalleOrden(detalleOrden);
        detalle.setProducto(producto);
        detalle.setCantidadRecibida(request.cantidadRecibida());
        detalle.setCostoUnitarioReal(request.costoUnitarioReal());
        detalle.setObservaciones(request.observaciones());

        return detalleRecepcionCompraRepository.save(detalle);
    }

    @Transactional
    public RecepcionCompra actualizarEstadoPago(
            Long recepcionId,
            String estadoPago,
            Double montoPagado
    ) {
        validarEstadoPago(estadoPago);

        RecepcionCompra recepcion = obtenerRecepcionPorId(recepcionId);

        if (montoPagado != null && montoPagado > 0) {
            recepcion.setMontoPagado(montoPagado);
        }
        recepcion.setEstadoPago(estadoPago.toUpperCase());

        return recepcionCompraRepository.save(recepcion);
    }

    @Transactional(readOnly = true)
    public RecepcionCompra obtenerRecepcionPorId(Long recepcionId) {
        return recepcionCompraRepository.findById(recepcionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe recepción de compra con id " + recepcionId
                ));
    }

    @Transactional(readOnly = true)
    public List<RecepcionCompra> listarRecepcionesPendientes() {
        return recepcionCompraRepository.findByEstadoPagoOrderByFechaRecepcionDesc("PENDIENTE");
    }

    @Transactional(readOnly = true)
    public List<RecepcionCompra> listarRecepcionesPorOrden(Long ordenId) {
        return recepcionCompraRepository.findByOrden_IdOrdenOrderByFechaRecepcionDesc(ordenId);
    }

    @Transactional(readOnly = true)
    public List<RecepcionCompra> listarTodasRecepciones() {
        return recepcionCompraRepository.findAll();
    }

    private void validarRegistroRecepcion(RegistrarRecepcionRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de recepción es obligatoria");
        }
        if (request.ordenId() == null) {
            throw new BusinessException("El ID de la orden de compra es obligatorio (ordenId)");
        }
        if (request.estado() == null || request.estado().isBlank()) {
            throw new BusinessException("El estado de recepción es obligatorio (estado)");
        }
        String estadoUpper = request.estado().trim().toUpperCase();
        if (!estadoUpper.equals("PARCIAL") && !estadoUpper.equals("COMPLETA") && !estadoUpper.equals("RECHAZADA")) {
            throw new BusinessException("Estado de recepción inválido. Valores permitidos: PARCIAL, COMPLETA, RECHAZADA");
        }
        if (request.totalRecepcion() == null || request.totalRecepcion() < 0) {
            throw new BusinessException("El total de recepción no puede ser negativo");
        }
    }

    private void validarAgregarDetalleRecepcion(AgregarDetalleRecepcionRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de detalle de recepción es obligatoria");
        }
        if (request.detalleOrdenId() == null) {
            throw new BusinessException("El ID del detalle de la orden original es obligatorio (detalleOrdenId)");
        }
        if (request.cantidadRecibida() == null || request.cantidadRecibida() <= 0) {
            throw new BusinessException("La cantidad recibida debe ser mayor a cero");
        }
    }

    private void validarEstadoPago(String estadoPago) {
        if (estadoPago == null || estadoPago.isBlank()) {
            throw new BusinessException("El estado de pago es obligatorio");
        }
        String estadoUpper = estadoPago.trim().toUpperCase();
        if (!estadoUpper.equals("PENDIENTE") && !estadoUpper.equals("PARCIAL") && !estadoUpper.equals("PAGADO")) {
            throw new BusinessException("Estado de pago inválido. Valores válidos: PENDIENTE, PARCIAL, PAGADO");
        }
    }
}
