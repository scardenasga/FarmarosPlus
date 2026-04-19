package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.InsufficientStockException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.PagoVenta;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import co.edu.unbosque.backend.repository.VentaRepository;
import co.edu.unbosque.backend.model.request.AnularVentaRequest;
import co.edu.unbosque.backend.model.request.CrearVentaRequest;
import co.edu.unbosque.backend.model.request.PagoVentaRequest;
import co.edu.unbosque.backend.model.request.VentaDetalleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;


/**
 * Servicio transaccional del módulo de ventas.
 * Garantiza consistencia entre venta, detalle, pago y movimientos de inventario.
 *
 * El campo {@code descuento} del request se interpreta como PORCENTAJE (0.0 - 1.0).
 * Ejemplo: 0.10 → 10 % de descuento sobre el subtotal.
 * El valor que se persiste en la entidad {@code Venta.descuento} es el MONTO en pesos
 * resultante de aplicar ese porcentaje, para que la respuesta siempre muestre cuánto
 * dinero se descontó realmente.
 *
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
@Service
public class VentaService {

    private static final double TOLERANCIA_MONETARIA = 0.01d;

    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public VentaService(
            VentaRepository ventaRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            LoteRepository loteRepository,
            MovimientoInventarioRepository movimientoInventarioRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    /**
     * Registra una venta completa y actualiza el inventario en una sola transacción.
     *
     * @param request datos de la venta (descuento como porcentaje 0.0-1.0)
     * @return venta persistida con sus detalles y pagos
     */
    @Transactional
    public Venta registrarVenta(CrearVentaRequest request) {
        validarVenta(request);

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario con id " + request.usuarioId()
                ));
        String usuarioResponsable = usuario.getUsername();

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("COMPLETADA");

        double porcentajeDescuento = valorMonetarioSeguro(request.descuento());
        if (porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new BusinessException("El descuento debe ser un porcentaje entre 0.0 y 1.0 (ej. 0.10 para 10%)");
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();
        double subtotal = 0.0;

        for (VentaDetalleRequest detalleRequest : request.detalles()) {

            Lote lote = loteRepository.findByIdForUpdate(detalleRequest.loteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el lote con id " + detalleRequest.loteId()
                    ));

            Producto producto = lote.getProducto();

            if (!producto.getUniqueID().equals(detalleRequest.productoId())) {
                throw new BusinessException(
                        "El lote " + detalleRequest.loteId() +
                        " no pertenece al producto " + detalleRequest.productoId()
                );
            }

            validarDisponibilidad(producto, lote, detalleRequest.cantidad());

            int stockAnterior = valorSeguro(producto.getStockActual());
            int stockNuevo = stockAnterior - detalleRequest.cantidad();
            int cantidadLoteNueva = valorSeguro(lote.getCantidad()) - detalleRequest.cantidad();

            Double precioUnitario = detalleRequest.precioUnitario() != null
                    ? detalleRequest.precioUnitario()
                    : producto.getPrecioVenta();

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setLote(lote);
            detalle.setCantidad(detalleRequest.cantidad());
            detalle.setPrecioUnitarioAplicado(precioUnitario);
            detalle.setSubtotalLinea(precioUnitario * detalleRequest.cantidad());
            detalles.add(detalle);
            subtotal += detalle.getSubtotalLinea();

            producto.setStockActual(stockNuevo);
            lote.setCantidad(cantidadLoteNueva);

            movimientos.add(construirMovimientoVenta(producto, lote, stockAnterior, stockNuevo, usuarioResponsable));
        }

        double montoDescuento = subtotal * porcentajeDescuento;
        double total = subtotal - montoDescuento;

        if (total < 0) {
            throw new BusinessException("El total de la venta no puede ser negativo");
        }

        venta.setDescuento(montoDescuento);

        List<PagoVenta> pagos = construirPagos(venta, request.pagos(), total);

        venta.setSubtotal(subtotal);
        venta.setTotal(total);
        venta.setDetalles(new HashSet<>(detalles));
        venta.setPagos(new HashSet<>(pagos));

        Venta ventaGuardada = ventaRepository.saveAndFlush(venta);
        asignarReferenciaVenta(movimientos, ventaGuardada.getIdVenta());
        movimientoInventarioRepository.saveAll(movimientos);

        return ventaRepository.findWithDetallesAndPagosByIdVenta(ventaGuardada.getIdVenta())
                .orElse(ventaGuardada);
    }

    /**
     * Anula una venta completada, repone inventario y registra movimientos de reversión.
     * Solo usuarios con rol ADMIN o REGENTE y estado ACTIVO pueden anular ventas.
     *
     * @param ventaId identificador de la venta
     * @param motivoAnulacion motivo funcional de la anulación
     * @param usuarioResponsable username del usuario que solicita la anulación
     * @return venta anulada
     */
    @Transactional
    public Venta anularVenta(Long ventaId, String motivoAnulacion, String usuarioResponsable) {
        if (motivoAnulacion == null || motivoAnulacion.isBlank()) {
            throw new BusinessException("El motivo de anulación es obligatorio");
        }
        if (usuarioResponsable == null || usuarioResponsable.isBlank()) {
            throw new BusinessException("El usuario responsable de la anulación es obligatorio");
        }

        // ── CORRECCIÓN: validar rol y estado del usuario responsable ────────────
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(usuarioResponsable)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario: " + usuarioResponsable
                ));
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new BusinessException("El usuario no está activo en el sistema");
        }
        if (!"ADMIN".equalsIgnoreCase(usuario.getRol()) && !"REGENTE".equalsIgnoreCase(usuario.getRol())) {
            throw new BusinessException("Solo usuarios con rol ADMIN o REGENTE pueden anular ventas");
        }
        // ────────────────────────────────────────────────────────────────────────

        Venta venta = ventaRepository.findWithDetallesAndPagosByIdVenta(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la venta con id " + ventaId));

        if (!"COMPLETADA".equalsIgnoreCase(venta.getEstado())) {
            throw new BusinessException("Solo se pueden anular ventas completadas");
        }

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (DetalleVenta detalle : venta.getDetalles()) {

            Lote lote = loteRepository.findByIdForUpdate(detalle.getLote().getIdLote())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el lote con id " + detalle.getLote().getIdLote()
                    ));

            Producto producto = lote.getProducto();

            int stockAnterior = valorSeguro(producto.getStockActual());
            int stockNuevo = stockAnterior + detalle.getCantidad();
            int cantidadLoteNueva = valorSeguro(lote.getCantidad()) + detalle.getCantidad();

            producto.setStockActual(stockNuevo);
            lote.setCantidad(cantidadLoteNueva);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setNombreProducto(producto.getNombre());
            movimiento.setLote(lote);
            movimiento.setTipoMovimiento("DEVOLUCION");
            movimiento.setCantidadAnterior(stockAnterior);
            movimiento.setCantidadNueva(stockNuevo);
            movimiento.setDiferencia(stockNuevo - stockAnterior);
            movimiento.setMotivo(motivoAnulacion);
            movimiento.setReferenciaDocumento("VENTA-" + venta.getIdVenta());
            movimiento.setUsuarioResponsable(usuarioResponsable);
            movimientos.add(movimiento);
        }

        venta.setEstado("ANULADA");
        venta.setMotivoAnulacion(motivoAnulacion);

        Venta ventaAnulada = ventaRepository.save(venta);
        movimientoInventarioRepository.saveAll(movimientos);
        return ventaAnulada;
    }

    /**
     * La venta queda marcada como Anulada en el historial y se revierten
     * los movimientos de inventario.
     *
     * @param ventaId identificador de la venta a eliminar
     * @param request datos de confirmación, usuario y motivo
     * @return venta marcada como ANULADA
     */
    @Transactional
    public Venta eliminarVenta(Long ventaId, AnularVentaRequest request) {
        if (!Boolean.TRUE.equals(request.confirmacion())) {
            throw new BusinessException("Debe confirmar la eliminación para proceder");
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario con id " + request.usuarioId()
                ));

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new BusinessException("El usuario no está activo en el sistema");
        }

        if (!"ADMIN".equalsIgnoreCase(usuario.getRol()) && !"REGENTE".equalsIgnoreCase(usuario.getRol())) {
            throw new BusinessException("Solo usuarios con rol ADMIN o REGENTE pueden eliminar ventas");
        }

        return anularVenta(ventaId, request.motivoAnulacion(), usuario.getUsername());
    }

    /**
     * Recupera una venta con detalles, pagos, productos y lotes en una sola consulta.
     *
     * @param ventaId identificador de la venta
     * @return venta encontrada con todas sus relaciones
     */
    @Transactional(readOnly = true)
    public Venta obtenerVentaDetallada(Long ventaId) {
        return ventaRepository.findWithDetallesAndPagosByIdVenta(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la venta con id " + ventaId));
    }

    /**
     * Construye la lista de pagos y valida que la suma coincida con el total esperado.
     */
    private List<PagoVenta> construirPagos(Venta venta, List<PagoVentaRequest> pagosRequest, double totalEsperado) {
        double totalPagado = 0.0;
        List<PagoVenta> pagos = new ArrayList<>();

        for (PagoVentaRequest pagoRequest : pagosRequest) {
            if (pagoRequest.monto() == null || pagoRequest.monto() <= 0) {
                throw new BusinessException("Cada pago debe tener un monto mayor a cero");
            }
            if (pagoRequest.tipo() == null || pagoRequest.tipo().isBlank()) {
                throw new BusinessException("Cada pago debe tener un tipo válido");
            }

            PagoVenta pago = new PagoVenta();
            pago.setVenta(venta);
            pago.setTipo(pagoRequest.tipo());
            pago.setMonto(pagoRequest.monto());
            pagos.add(pago);
            totalPagado += pagoRequest.monto();
        }

        if (Math.abs(totalPagado - totalEsperado) > TOLERANCIA_MONETARIA) {
            throw new BusinessException("La suma de los pagos debe coincidir con el total de la venta");
        }

        return pagos;
    }

    /**
     * Construye el movimiento de salida asociado a una línea de venta.
     */
    private MovimientoInventario construirMovimientoVenta(
            Producto producto,
            Lote lote,
            int cantidadAnterior,
            int cantidadNueva,
            String usuarioResponsable
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setNombreProducto(producto.getNombre());
        movimiento.setLote(lote);
        movimiento.setTipoMovimiento("VENTA");
        movimiento.setCantidadAnterior(cantidadAnterior);
        movimiento.setCantidadNueva(cantidadNueva);
        movimiento.setDiferencia(cantidadNueva - cantidadAnterior);
        movimiento.setUsuarioResponsable(usuarioResponsable);
        return movimiento;
    }

    /**
     * Agrega la referencia documental de la venta a cada movimiento generado.
     */
    private void asignarReferenciaVenta(List<MovimientoInventario> movimientos, Long ventaId) {
        String referencia = "VENTA-" + ventaId;
        for (MovimientoInventario movimiento : movimientos) {
            movimiento.setReferenciaDocumento(referencia);
        }
    }

    /**
     * Valida las reglas mínimas de entrada para registrar una venta.
     */
    private void validarVenta(CrearVentaRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de venta es obligatoria");
        }
        if (request.usuarioId() == null) {
            throw new BusinessException("El usuario de la venta es obligatorio");
        }
        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new BusinessException("La venta debe tener al menos un detalle");
        }
        if (request.pagos() == null || request.pagos().isEmpty()) {
            throw new BusinessException("La venta debe tener al menos un pago");
        }
        for (VentaDetalleRequest detalle : request.detalles()) {
            if (detalle.productoId() == null) {
                throw new BusinessException("Cada detalle debe indicar un producto");
            }
            if (detalle.loteId() == null) {
                throw new BusinessException("Cada detalle debe indicar un lote");
            }
            if (detalle.cantidad() == null || detalle.cantidad() <= 0) {
                throw new BusinessException("Cada detalle debe tener una cantidad mayor a cero");
            }
            if (detalle.precioUnitario() != null && detalle.precioUnitario() < 0) {
                throw new BusinessException("El precio unitario no puede ser negativo");
            }
        }
        if (request.descuento() != null && request.descuento() < 0) {
            throw new BusinessException("El descuento no puede ser negativo");
        }
    }

    /**
     * Verifica stock agregado y stock por lote antes de confirmar una venta.
     */
    private void validarDisponibilidad(Producto producto, Lote lote, Integer cantidadSolicitada) {
        if (!"ACTIVO".equalsIgnoreCase(producto.getEstado())) {
            throw new BusinessException("Solo se pueden vender productos activos");
        }
        if (valorSeguro(producto.getStockActual()) < cantidadSolicitada) {
            throw new InsufficientStockException("Stock insuficiente para el producto " + producto.getNombre());
        }
        if (valorSeguro(lote.getCantidad()) < cantidadSolicitada) {
            throw new InsufficientStockException("Stock insuficiente en el lote " + lote.getNumeroLote());
        }
    }

    private int valorSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private double valorMonetarioSeguro(Double valor) {
        return valor == null ? 0.0 : valor;
    }
}