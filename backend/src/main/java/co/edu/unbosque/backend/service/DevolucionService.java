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
import co.edu.unbosque.backend.model.request.ActualizarDevolucionRequest;
import co.edu.unbosque.backend.model.request.DetalleDevolucionRequest;
import co.edu.unbosque.backend.model.request.RegistrarDevolucionRequest;
import co.edu.unbosque.backend.model.response.DetalleDevolucionResponse;
import co.edu.unbosque.backend.model.response.DevolucionResponse;
import co.edu.unbosque.backend.repository.DevolucionProveedorRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    public DevolucionService(DevolucionProveedorRepository devolucionRepository,
                             ProveedorRepository proveedorRepository,
                             ProductoRepository productoRepository,
                             LoteRepository loteRepository,
                             MovimientoInventarioRepository movimientoRepository,
                             UsuarioRepository usuarioRepository) {
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

        co.edu.unbosque.backend.model.entity.Usuario usuario = resolverUsuarioResponsable(request.usuarioResponsable());
        String usuarioResponsable = usuario.getUsername();

        DevolucionProveedor devolucion = new DevolucionProveedor();
        devolucion.setProveedor(proveedor);
        devolucion.setUsuario(usuario);
        devolucion.setUsuarioResponsable(usuarioResponsable);
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
            mov.setUsuarioResponsable(usuarioResponsable);
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

    @Transactional
    public DevolucionResponse actualizar(Long id, ActualizarDevolucionRequest request, String usernameSolicitante) {
        DevolucionProveedor dev = devolucionRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la devolución con id " + id));

        if (request.motivo() != null) {
            dev.setMotivo(request.motivo().trim());
        }
        dev.setObservaciones(request.observaciones());
        if (request.estado() != null && !request.estado().isBlank()) {
            String nuevo = request.estado().trim().toUpperCase();
            validarEstado(nuevo);
            validarTransicionConRol(dev.getEstado(), nuevo, usernameSolicitante);
            dev.setEstado(nuevo);
        }

        return toResponse(devolucionRepository.save(dev));
    }

    // Compatibilidad: sin usuario (tests/unitarios)
    @Transactional
    public DevolucionResponse actualizar(Long id, ActualizarDevolucionRequest request) {
        return actualizar(id, request, null);
    }

    @Transactional
    public DevolucionResponse cambiarEstado(Long id, String estado, String usernameSolicitante) {
        if (estado == null || estado.isBlank()) {
            throw new BusinessException("El estado es obligatorio.");
        }
        String nuevo = estado.trim().toUpperCase();
        validarEstado(nuevo);
        DevolucionProveedor dev = devolucionRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la devolución con id " + id));
        validarTransicionConRol(dev.getEstado(), nuevo, usernameSolicitante);
        dev.setEstado(nuevo);
        return toResponse(devolucionRepository.save(dev));
    }

    @Transactional
    public DevolucionResponse cambiarEstado(Long id, String estado) {
        return cambiarEstado(id, estado, null);
    }

    private void validarTransicionConRol(String actual, String nuevo, String usernameSolicitante) {
        if (actual != null && actual.equals(nuevo)) return;
        java.util.Set<String> permitidos = java.util.Set.of("PENDIENTE", "ENVIADA", "ACEPTADA", "RECHAZADA", "CERRADA");
        if (!permitidos.contains(nuevo)) {
            throw new BusinessException("Estado inválido. Valores válidos: PENDIENTE, ENVIADA, ACEPTADA, RECHAZADA, CERRADA");
        }
        // Sin usuario (SYSTEM/tests) -> permitir como admin
        if (usernameSolicitante == null || usernameSolicitante.isBlank()) return;
        var usuarioOpt = usuarioRepository.findByUsername(usernameSolicitante.trim());
        if (usuarioOpt.isEmpty()) return; // fallback SISTEMA ya permite
        String rol = usuarioOpt.get().getRol() != null ? usuarioOpt.get().getRol().toUpperCase() : "";
        boolean esAdmin = rol.equals("ADMIN") || rol.equals("REGENTE");
        if (esAdmin) return;
        // Empleado (VENDEDOR/ALMACENISTA): flujo controlado
        // Permitido: PENDIENTE -> ENVIADA (enviar), ENVIADA -> PENDIENTE (retirar), PENDIENTE -> CERRADA (cancelar)
        String a = actual != null ? actual.toUpperCase() : "PENDIENTE";
        boolean permitidoEmpleado = (a.equals("PENDIENTE") && nuevo.equals("ENVIADA"))
                || (a.equals("ENVIADA") && nuevo.equals("PENDIENTE"))
                || (a.equals("PENDIENTE") && nuevo.equals("CERRADA"));
        if (!permitidoEmpleado) {
            throw new BusinessException("Transición no permitida para tu rol (" + rol + "): " + a + " → " + nuevo + ". Solicita aprobación de un administrador (ADMIN/REGENTE) para estados ACEPTADA/RECHAZADA/CERRADA desde ENVIADA.");
        }
    }

    private void validarEstado(String estado) {
        java.util.Set<String> permitidos = java.util.Set.of("PENDIENTE", "ENVIADA", "ACEPTADA", "RECHAZADA", "CERRADA");
        if (!permitidos.contains(estado)) {
            throw new BusinessException("Estado inválido. Valores válidos: PENDIENTE, ENVIADA, ACEPTADA, RECHAZADA, CERRADA");
        }
    }

    @Transactional
    public void eliminar(Long id, String usuarioResponsable) {
        DevolucionProveedor dev = devolucionRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la devolución con id " + id));

        co.edu.unbosque.backend.model.entity.Usuario usuario = resolverUsuarioResponsable(usuarioResponsable);
        String usuarioResp = usuario.getUsername();

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (DetalleDevolucionProveedor detalle : dev.getDetalles()) {
            if (detalle.getProducto() == null || detalle.getLote() == null) {
                continue;
            }

            Producto producto = productoRepository.findByIdForUpdate(detalle.getProducto().getUniqueID())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el producto con id " + detalle.getProducto().getUniqueID()));

            Lote lote = loteRepository.findByIdForUpdate(detalle.getLote().getIdLote())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el lote con id " + detalle.getLote().getIdLote()));

            int stockAnterior = producto.getStockActual();
            int stockNuevo = stockAnterior + detalle.getCantidad();
            int cantLoteNuevo = lote.getCantidad() + detalle.getCantidad();

            producto.setStockActual(stockNuevo);
            lote.setCantidad(cantLoteNuevo);

            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(producto);
            mov.setNombreProducto(detalle.getNombreProducto());
            mov.setLote(lote);
            mov.setTipoMovimiento("DEVOLUCION");
            mov.setCantidadAnterior(stockAnterior);
            mov.setCantidadNueva(stockNuevo);
            mov.setDiferencia(stockNuevo - stockAnterior);
            mov.setMotivo("Reversión por eliminación de devolución a proveedor " + dev.getIdDevolucion());
            mov.setReferenciaDocumento("DEV-PROV-" + dev.getIdDevolucion() + "-ELIM");
            mov.setUsuarioResponsable(usuarioResp);
            movimientos.add(mov);
        }

        movimientoRepository.saveAll(movimientos);
        devolucionRepository.delete(dev);
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
                detallesResp,
                d.getObservaciones(),
                d.getEstado(),
                d.getTipoDevolucion());
    }

    private co.edu.unbosque.backend.model.entity.Usuario resolverUsuarioResponsable(String usernameSolicitud) {
        if (usernameSolicitud != null && !usernameSolicitud.isBlank()) {
            var usuario = usuarioRepository.findByUsername(usernameSolicitud.trim());
            if (usuario.isPresent()) {
                return usuario.get();
            }
        }

        return usuarioRepository.findByUsername("SISTEMA")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario del sistema con username SISTEMA"));
    }
}
