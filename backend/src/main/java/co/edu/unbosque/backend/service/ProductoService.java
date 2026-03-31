package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.HistorialPrecioProducto;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.repository.HistorialPrecioProductoRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación para el catálogo de productos.
 * Agrupa operaciones simples de consulta y cambios transaccionales de precio.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final HistorialPrecioProductoRepository historialPrecioProductoRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            HistorialPrecioProductoRepository historialPrecioProductoRepository
    ) {
        this.productoRepository = productoRepository;
        this.historialPrecioProductoRepository = historialPrecioProductoRepository;
    }

    /**
     * Guarda un producto nuevo o existente.
     *
     * @param producto entidad a persistir
     * @return producto persistido
     */
    @Transactional
    public Producto guardarProducto(Producto producto) {
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    /**
     * Recupera un producto por id o lanza excepción.
     *
     * @param productoId identificador del producto
     * @return producto encontrado
     */
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + productoId));
    }

    /**
     * Lista todos los productos activos.
     *
     * @return productos activos ordenados por nombre
     */
    @Transactional(readOnly = true)
    public List<Producto> listarProductosActivos() {
        return productoRepository.findByEstadoOrderByNombreAsc("ACTIVO");
    }

    /**
     * Recupera productos cuyo stock actual está por debajo o igual al mínimo.
     *
     * @return productos con stock bajo
     */
    @Transactional(readOnly = true)
    public List<Producto> listarProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    /**
     * Actualiza precio y costo de un producto y registra el historial del cambio
     * en la misma transacción para evitar inconsistencias parciales.
     *
     * @param request datos del cambio
     * @return producto actualizado
     */
    @Transactional
    public Producto actualizarPrecio(CambioPrecioProductoRequest request) {
        validarCambioPrecio(request);

        Producto producto = productoRepository.findByIdForUpdate(request.productoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto con id " + request.productoId()
                ));

        HistorialPrecioProducto historial = new HistorialPrecioProducto();
        historial.setProducto(producto);
        historial.setNombreProducto(producto.getNombre());
        historial.setPrecioAnterior(producto.getPrecioVenta());
        historial.setPrecioNuevo(request.nuevoPrecioVenta());
        historial.setCostoAnterior(producto.getCosto());
        historial.setCostoNuevo(request.nuevoCosto());
        historial.setMotivo(request.motivo());
        historial.setUsuarioResponsable(request.usuarioResponsable());

        producto.setCosto(request.nuevoCosto());
        producto.setPrecioVenta(request.nuevoPrecioVenta());
        producto.setMargenGanancia(calcularMargen(request.nuevoCosto(), request.nuevoPrecioVenta()));

        Producto productoActualizado = productoRepository.save(producto);
        historialPrecioProductoRepository.save(historial);
        return productoActualizado;
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new BusinessException("El producto no puede ser nulo");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }
        if (producto.getCosto() == null || producto.getCosto() < 0) {
            throw new BusinessException("El costo del producto debe ser mayor o igual a cero");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta() < 0) {
            throw new BusinessException("El precio de venta debe ser mayor o igual a cero");
        }
        if (producto.getStockActual() == null) {
            producto.setStockActual(0);
        }
        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(0);
        }
        producto.setMargenGanancia(calcularMargen(producto.getCosto(), producto.getPrecioVenta()));
    }

    private void validarCambioPrecio(CambioPrecioProductoRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de cambio de precio es obligatoria");
        }
        if (request.productoId() == null) {
            throw new BusinessException("El id del producto es obligatorio");
        }
        if (request.nuevoCosto() == null || request.nuevoCosto() < 0) {
            throw new BusinessException("El nuevo costo debe ser mayor o igual a cero");
        }
        if (request.nuevoPrecioVenta() == null || request.nuevoPrecioVenta() < 0) {
            throw new BusinessException("El nuevo precio de venta debe ser mayor o igual a cero");
        }
        if (request.usuarioResponsable() == null || request.usuarioResponsable().isBlank()) {
            throw new BusinessException("El usuario responsable del cambio es obligatorio");
        }
    }

    private Double calcularMargen(Double costo, Double precioVenta) {
        if (costo == null || precioVenta == null || costo <= 0) {
            return 0.0;
        }
        return ((precioVenta - costo) / costo) * 100.0;
    }
}
