package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.DetalleDevolucionCliente;
import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.DevolucionCliente;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.model.request.ActualizarDevolucionClienteRequest;
import co.edu.unbosque.backend.model.request.DetalleDevolucionClienteRequest;
import co.edu.unbosque.backend.model.request.RegistrarDevolucionClienteRequest;
import co.edu.unbosque.backend.model.response.DetalleDevolucionClienteResponse;
import co.edu.unbosque.backend.model.response.DevolucionClienteResponse;
import co.edu.unbosque.backend.repository.DetalleDevolucionClienteRepository;
import co.edu.unbosque.backend.repository.DevolucionClienteRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import co.edu.unbosque.backend.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio transaccional para devoluciones de cliente.
 */
@Service
public class DevolucionClienteService {

    private final DevolucionClienteRepository devolucionRepository;
    private final DetalleDevolucionClienteRepository detalleDevolucionRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public DevolucionClienteService(DevolucionClienteRepository devolucionRepository,
                                    DetalleDevolucionClienteRepository detalleDevolucionRepository,
                                    VentaRepository ventaRepository,
                                    UsuarioRepository usuarioRepository,
                                    ProductoRepository productoRepository,
                                    LoteRepository loteRepository,
                                    MovimientoInventarioRepository movimientoRepository) {
        this.devolucionRepository = devolucionRepository;
        this.detalleDevolucionRepository = detalleDevolucionRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional
    public DevolucionClienteResponse registrar(RegistrarDevolucionClienteRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de devolución es obligatoria");
        }

        Venta venta = ventaRepository.findWithDetallesAndPagosByIdVenta(request.idVenta())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la venta con id " + request.idVenta()));

        if (!"COMPLETADA".equalsIgnoreCase(venta.getEstado())) {
            throw new BusinessException("Solo se pueden registrar devoluciones sobre ventas completadas");
        }

        Usuario usuario = resolverUsuarioResponsable(request.usuarioResponsable());
        String usuarioResponsable = usuario.getUsername();

        DevolucionCliente devolucion = new DevolucionCliente();
        devolucion.setVenta(venta);
        devolucion.setUsuario(usuario);
        devolucion.setUsuarioResponsable(usuarioResponsable);
        devolucion.setNombreCliente(normalizar(request.nombreCliente()));
        devolucion.setDocumentoCliente(normalizar(request.documentoCliente()));
        devolucion.setMotivo(normalizar(request.motivo()));
        devolucion.setFecha(LocalDateTime.now());

        Map<Long, Integer> cantidadesPorDetalle = agruparCantidades(request.detalles());
        List<DetalleDevolucionCliente> detalles = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cantidadesPorDetalle.entrySet()) {
            Long idDetalleVenta = entry.getKey();
            Integer cantidadSolicitada = entry.getValue();

            DetalleVenta detalleVenta = venta.getDetalles().stream()
                    .filter(detalle -> detalle.getIdDetalle().equals(idDetalleVenta))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "La venta " + request.idVenta() + " no contiene el detalle " + idDetalleVenta));

            validarCantidadDisponible(idDetalleVenta, detalleVenta, cantidadSolicitada);

            Producto producto = productoRepository.findByIdForUpdate(detalleVenta.getProducto().getUniqueID())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el producto con id " + detalleVenta.getProducto().getUniqueID()));

            Lote lote = null;
            if (detalleVenta.getLote() != null) {
                lote = loteRepository.findByIdForUpdate(detalleVenta.getLote().getIdLote())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No existe el lote con id " + detalleVenta.getLote().getIdLote()));
            }

            int stockAnterior = valorSeguro(producto.getStockActual());
            int stockNuevo = stockAnterior + cantidadSolicitada;
            producto.setStockActual(stockNuevo);

            if (lote != null) {
                lote.setCantidad(valorSeguro(lote.getCantidad()) + cantidadSolicitada);
            }

            DetalleDevolucionCliente detalle = new DetalleDevolucionCliente();
            detalle.setDevolucion(devolucion);
            detalle.setDetalleVenta(detalleVenta);
            detalle.setProducto(producto);
            detalle.setLote(lote);
            detalle.setCantidad(cantidadSolicitada);
            detalle.setNombreProducto(producto.getNombre());
            detalle.setNumeroLote(lote != null ? lote.getNumeroLote() : null);
            detalles.add(detalle);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setNombreProducto(producto.getNombre());
            movimiento.setLote(lote);
            movimiento.setTipoMovimiento("DEVOLUCION");
            movimiento.setCantidadAnterior(stockAnterior);
            movimiento.setCantidadNueva(stockNuevo);
            movimiento.setDiferencia(stockNuevo - stockAnterior);
            movimiento.setMotivo(buildMotivo(request, venta));
            movimiento.setUsuarioResponsable(usuarioResponsable);
            movimientos.add(movimiento);
        }

        devolucion.setDetalles(detalles);
        DevolucionCliente guardada = devolucionRepository.save(devolucion);

        String referenciaDocumento = "DEV-CLI-" + guardada.getIdDevolucionCliente();
        movimientos.forEach(movimiento -> movimiento.setReferenciaDocumento(referenciaDocumento));
        movimientoRepository.saveAll(movimientos);

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<DevolucionClienteResponse> listar() {
        return devolucionRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DevolucionClienteResponse obtener(Long id) {
        DevolucionCliente devolucion = devolucionRepository.findWithDetallesByIdDevolucionCliente(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la devolución con id " + id));
        return toResponse(devolucion);
    }

    @Transactional
    public DevolucionClienteResponse actualizar(Long id, ActualizarDevolucionClienteRequest request) {
        DevolucionCliente devolucion = devolucionRepository.findWithDetallesByIdDevolucionCliente(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la devolución con id " + id));

        if (request.nombreCliente() != null) {
            devolucion.setNombreCliente(normalizar(request.nombreCliente()));
        }
        if (request.documentoCliente() != null) {
            devolucion.setDocumentoCliente(normalizar(request.documentoCliente()));
        }
        if (request.motivo() != null) {
            devolucion.setMotivo(normalizar(request.motivo()));
        }

        return toResponse(devolucionRepository.save(devolucion));
    }

    @Transactional
    public void eliminar(Long id, String usuarioResponsable) {
        DevolucionCliente devolucion = devolucionRepository.findWithDetallesByIdDevolucionCliente(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la devolución con id " + id));

        Usuario usuario = resolverUsuarioResponsable(usuarioResponsable);
        String usuarioResp = usuario.getUsername();

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (DetalleDevolucionCliente detalle : devolucion.getDetalles()) {
            if (detalle.getProducto() == null) {
                continue;
            }

            Producto producto = productoRepository.findByIdForUpdate(detalle.getProducto().getUniqueID())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el producto con id " + detalle.getProducto().getUniqueID()));

            int stockAnterior = valorSeguro(producto.getStockActual());
            int stockNuevo = stockAnterior - detalle.getCantidad();
            if (stockNuevo < 0) {
                throw new BusinessException(
                        "No se puede eliminar la devolución: stock insuficiente en el producto "
                                + detalle.getNombreProducto() + " para revertir la entrada.");
            }
            producto.setStockActual(stockNuevo);

            if (detalle.getLote() != null) {
                Lote lote = loteRepository.findByIdForUpdate(detalle.getLote().getIdLote())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No existe el lote con id " + detalle.getLote().getIdLote()));
                int cantLoteNuevo = valorSeguro(lote.getCantidad()) - detalle.getCantidad();
                if (cantLoteNuevo < 0) {
                    throw new BusinessException(
                            "No se puede eliminar la devolución: stock insuficiente en el lote "
                                    + lote.getNumeroLote() + " para revertir la entrada.");
                }
                lote.setCantidad(cantLoteNuevo);
            }

            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(producto);
            mov.setNombreProducto(detalle.getNombreProducto());
            mov.setLote(detalle.getLote());
            mov.setTipoMovimiento("DEVOLUCION");
            mov.setCantidadAnterior(stockAnterior);
            mov.setCantidadNueva(stockNuevo);
            mov.setDiferencia(stockNuevo - stockAnterior);
            mov.setMotivo("Reversión por eliminación de devolución de cliente " + devolucion.getIdDevolucionCliente());
            mov.setReferenciaDocumento("DEV-CLI-" + devolucion.getIdDevolucionCliente() + "-ELIM");
            mov.setUsuarioResponsable(usuarioResp);
            movimientos.add(mov);
        }

        movimientoRepository.saveAll(movimientos);
        devolucionRepository.delete(devolucion);
    }

    private void validarCantidadDisponible(Long idDetalleVenta,
                                           DetalleVenta detalleVenta,
                                           Integer cantidadSolicitada) {
        int cantidadVendida = valorSeguro(detalleVenta.getCantidad());
        int cantidadYaDevuelta = valorSeguro(detalleDevolucionRepository.sumarCantidadDevueltaPorDetalleVenta(idDetalleVenta));
        int disponible = cantidadVendida - cantidadYaDevuelta;

        if (cantidadSolicitada == null || cantidadSolicitada <= 0) {
            throw new BusinessException("La cantidad solicitada debe ser mayor a cero");
        }
        if (cantidadSolicitada > disponible) {
            throw new BusinessException(
                    "La cantidad solicitada excede el máximo disponible para devolución. Disponible: " + disponible);
        }
    }

    private Map<Long, Integer> agruparCantidades(List<DetalleDevolucionClienteRequest> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new BusinessException("La devolución debe tener al menos un producto");
        }

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (DetalleDevolucionClienteRequest detalle : detalles) {
            if (detalle == null || detalle.idDetalleVenta() == null) {
                throw new BusinessException("Cada detalle debe indicar un detalle de venta");
            }
            cantidades.merge(detalle.idDetalleVenta(), detalle.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    private String buildMotivo(RegistrarDevolucionClienteRequest request, Venta venta) {
        String cliente = request.nombreCliente() != null && !request.nombreCliente().isBlank()
                ? request.nombreCliente()
                : "cliente";
        String motivo = request.motivo();
        if (motivo != null && !motivo.isBlank()) {
            return "Devolución de " + cliente + ": " + motivo;
        }
        return "Devolución de " + cliente + " sobre venta " + venta.getIdVenta();
    }

    private DevolucionClienteResponse toResponse(DevolucionCliente devolucion) {
        List<DetalleDevolucionClienteResponse> detalles = devolucion.getDetalles().stream()
                .map(detalle -> new DetalleDevolucionClienteResponse(
                        detalle.getDetalleVenta().getIdDetalle(),
                        detalle.getProducto().getUniqueID(),
                        detalle.getNombreProducto(),
                        detalle.getNumeroLote(),
                        detalle.getCantidad()))
                .toList();

        return new DevolucionClienteResponse(
                devolucion.getIdDevolucionCliente(),
                devolucion.getVenta().getIdVenta(),
                devolucion.getNombreCliente(),
                devolucion.getDocumentoCliente(),
                devolucion.getUsuarioResponsable(),
                devolucion.getMotivo(),
                devolucion.getFecha(),
                detalles
        );
    }

    private int valorSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private Usuario resolverUsuarioResponsable(String usernameSolicitud) {
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

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio.replaceAll("\\s+", " ");
    }
}
