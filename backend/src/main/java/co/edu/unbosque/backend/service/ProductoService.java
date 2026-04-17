package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.entity.HistorialPrecioProducto;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import co.edu.unbosque.backend.model.request.CrearProductoRequest;
import co.edu.unbosque.backend.model.request.IngresoProductoRequest;
import co.edu.unbosque.backend.repository.CategoriaRepository;
import co.edu.unbosque.backend.repository.HistorialPrecioProductoRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
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
    private static final String TIPO_MOVIMIENTO_INGRESO = "COMPRA";

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final HistorialPrecioProductoRepository historialPrecioProductoRepository;
    private final CurrentUserService currentUserService;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            LoteRepository loteRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            HistorialPrecioProductoRepository historialPrecioProductoRepository,
            CurrentUserService currentUserService
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.loteRepository = loteRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.historialPrecioProductoRepository = historialPrecioProductoRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Crea un producto nuevo con stock inicial obligatorio. La operación registra
     * siempre un movimiento inicial y opcionalmente un lote.
     *
     * @param request datos de creación
     * @return producto persistido
     */
    @Transactional
    public Producto crearProducto(CrearProductoRequest request) {
        validarCreacionProducto(request);

        String codigoBarras = normalizarTexto(request.codigoBarras());
        if (productoRepository.existsByCodigoBarrasIgnoreCase(codigoBarras)) {
            throw new BusinessException("Ya existe un producto con codigo de barras " + codigoBarras);
        }

        Categoria categoria = obtenerCategoriaOpcional(request.categoriaId());
        int stockInicial = valorEnteroSeguro(request.stockInicial());

        Producto producto = new Producto();
        producto.setCategoria(categoria);
        producto.setNombre(normalizarTexto(request.nombre()));
        producto.setDescripcion(normalizarTexto(request.descripcion()));
        producto.setCodigoBarras(codigoBarras);
        producto.setStockMinimo(valorEnteroSeguro(request.stockMinimo()));
        producto.setStockActual(stockInicial);
        producto.setCosto(request.costo());
        producto.setPrecioVenta(request.precioVenta());
        producto.setPorcentajeIva(request.porcentajeIva() != null ? request.porcentajeIva() : 0.0);
        producto.setEstado(normalizarEstadoProducto(request.estado()));

        validarProducto(producto);
        Producto productoGuardado = productoRepository.save(producto);

        registrarIngresoInicial(productoGuardado, stockInicial, normalizarTexto(request.numeroLote()));
        return productoGuardado;
    }

    /**
     * Aumenta stock de un producto existente identificado por código de barras.
     * Si el request trae lote, se crea un nuevo lote; si además cambia el precio
     * de venta, se registra historial de precio.
     *
     * @param codigoBarras código de barras del producto
     * @param request datos del ingreso
     * @return producto actualizado
     */
    @Transactional
    public Producto ingresarStock(String codigoBarras, IngresoProductoRequest request) {
        validarIngresoProducto(request);

        Producto producto = productoRepository.findByCodigoBarrasForUpdate(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto con codigo de barras " + codigoBarras
                ));

        int stockAnterior = valorEnteroSeguro(producto.getStockActual());
        int stockNuevo = stockAnterior + request.cantidad();

        Lote loteGuardado = null;
        if (request.numeroLote() != null && !request.numeroLote().isBlank()) {
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setNumeroLote(normalizarTexto(request.numeroLote()));
            lote.setCantidad(request.cantidad());
            loteGuardado = loteRepository.save(lote);
        }

        Double costoAnterior = producto.getCosto();
        Double precioAnterior = producto.getPrecioVenta();

        if (request.nuevoCosto() != null) {
            producto.setCosto(request.nuevoCosto());
        }
        if (request.nuevoPrecioVenta() != null) {
            producto.setPrecioVenta(request.nuevoPrecioVenta());
        }

        producto.setStockActual(stockNuevo);
        producto.setMargenGanancia(calcularMargen(producto.getCosto(), producto.getPrecioVenta()));

        Producto productoActualizado = productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(productoActualizado);
        movimiento.setNombreProducto(productoActualizado.getNombre());
        movimiento.setLote(loteGuardado);
        movimiento.setTipoMovimiento(TIPO_MOVIMIENTO_INGRESO);
        movimiento.setCantidadAnterior(stockAnterior);
        movimiento.setCantidadNueva(stockNuevo);
        movimiento.setDiferencia(request.cantidad());
        movimiento.setUsuarioResponsable(currentUserService.getCurrentUsername());
        movimientoInventarioRepository.save(movimiento);

        if (request.nuevoPrecioVenta() != null && !request.nuevoPrecioVenta().equals(precioAnterior)) {
            HistorialPrecioProducto historial = new HistorialPrecioProducto();
            historial.setProducto(productoActualizado);
            historial.setNombreProducto(productoActualizado.getNombre());
            historial.setPrecioAnterior(precioAnterior);
            historial.setPrecioNuevo(productoActualizado.getPrecioVenta());
            historial.setCostoAnterior(costoAnterior);
            historial.setCostoNuevo(productoActualizado.getCosto());
            historial.setMotivo("Actualizacion de precio durante ingreso de stock");
            historial.setUsuarioResponsable(currentUserService.getCurrentUsername());
            historialPrecioProductoRepository.save(historial);
        }

        return productoActualizado;
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
    public Producto actualizarPrecio(String codigoBarras, CambioPrecioProductoRequest request) {
        validarCambioPrecio(request);

        Producto producto = productoRepository.findByCodigoBarrasForUpdate(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto con codigo de barras " + codigoBarras
                ));

        HistorialPrecioProducto historial = new HistorialPrecioProducto();
        historial.setProducto(producto);
        historial.setNombreProducto(producto.getNombre());
        historial.setPrecioAnterior(producto.getPrecioVenta());
        historial.setPrecioNuevo(request.nuevoPrecioVenta());
        historial.setCostoAnterior(producto.getCosto());
        historial.setCostoNuevo(request.nuevoCosto());
        historial.setMotivo(request.motivo());
        historial.setUsuarioResponsable(currentUserService.getCurrentUsername());

        producto.setCosto(request.nuevoCosto());
        producto.setPrecioVenta(request.nuevoPrecioVenta());
        producto.setMargenGanancia(calcularMargen(request.nuevoCosto(), request.nuevoPrecioVenta()));

        Producto productoActualizado = productoRepository.save(producto);
        historialPrecioProductoRepository.save(historial);
        return productoActualizado;
    }

    private void registrarIngresoInicial(Producto producto, int stockInicial, String numeroLote) {
        if (stockInicial <= 0) {
            return;
        }

        Lote loteGuardado = null;
        if (numeroLote != null && !numeroLote.isBlank()) {
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setNumeroLote(numeroLote);
            lote.setCantidad(stockInicial);
            loteGuardado = loteRepository.save(lote);
        }

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setNombreProducto(producto.getNombre());
        movimiento.setLote(loteGuardado);
        movimiento.setTipoMovimiento(TIPO_MOVIMIENTO_INGRESO);
        movimiento.setCantidadAnterior(0);
        movimiento.setCantidadNueva(stockInicial);
        movimiento.setDiferencia(stockInicial);
        movimiento.setUsuarioResponsable(currentUserService.getCurrentUsername());
        movimientoInventarioRepository.save(movimiento);
    }

    private Categoria obtenerCategoriaOpcional(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoria con id " + categoriaId));
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new BusinessException("El producto no puede ser nulo");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }
        if (producto.getCodigoBarras() == null || producto.getCodigoBarras().isBlank()) {
            throw new BusinessException("El codigo de barras del producto es obligatorio");
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

    private void validarCreacionProducto(CrearProductoRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de creacion del producto es obligatoria");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }
        if (request.codigoBarras() == null || request.codigoBarras().isBlank()) {
            throw new BusinessException("El codigo de barras es obligatorio");
        }
        if (request.costo() == null || request.costo() < 0) {
            throw new BusinessException("El costo del producto debe ser mayor o igual a cero");
        }
        if (request.precioVenta() == null || request.precioVenta() < 0) {
            throw new BusinessException("El precio de venta debe ser mayor o igual a cero");
        }
        if (request.stockInicial() == null || request.stockInicial() <= 0) {
            throw new BusinessException("El stock inicial debe ser mayor a cero");
        }
        if (request.stockMinimo() != null && request.stockMinimo() < 0) {
            throw new BusinessException("El stock minimo no puede ser negativo");
        }
        if (request.numeroLote() != null && !request.numeroLote().isBlank() && valorEnteroSeguro(request.stockInicial()) <= 0) {
            throw new BusinessException("Si se registra lote, el stock inicial debe ser mayor a cero");
        }
    }

    private void validarIngresoProducto(IngresoProductoRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de ingreso de producto es obligatoria");
        }
        if (request.cantidad() == null || request.cantidad() <= 0) {
            throw new BusinessException("La cantidad a ingresar debe ser mayor a cero");
        }
        if (request.nuevoCosto() != null && request.nuevoCosto() < 0) {
            throw new BusinessException("El nuevo costo no puede ser negativo");
        }
        if (request.nuevoPrecioVenta() != null && request.nuevoPrecioVenta() < 0) {
            throw new BusinessException("El nuevo precio de venta no puede ser negativo");
        }
    }

    private void validarCambioPrecio(CambioPrecioProductoRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de cambio de precio es obligatoria");
        }
        if (request.nuevoCosto() == null || request.nuevoCosto() < 0) {
            throw new BusinessException("El nuevo costo debe ser mayor o igual a cero");
        }
        if (request.nuevoPrecioVenta() == null || request.nuevoPrecioVenta() < 0) {
            throw new BusinessException("El nuevo precio de venta debe ser mayor o igual a cero");
        }
    }

    private Double calcularMargen(Double costo, Double precioVenta) {
        if (costo == null || precioVenta == null || costo <= 0) {
            return 0.0;
        }
        return ((precioVenta - costo) / costo) * 100.0;
    }

    private Integer valorEnteroSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private String normalizarEstadoProducto(String estado) {
        if (estado == null || estado.isBlank()) {
            return "ACTIVO";
        }
        return estado.trim().toUpperCase();
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim().replaceAll("\\s+", " ");
    }
}
