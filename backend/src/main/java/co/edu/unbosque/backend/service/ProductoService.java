package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.entity.HistorialPrecioProducto;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.ActualizarProductoRequest;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import co.edu.unbosque.backend.model.request.CrearProductoRequest;
import co.edu.unbosque.backend.model.request.IngresoProductoRequest;
import co.edu.unbosque.backend.model.response.CategoriaResponse;
import co.edu.unbosque.backend.model.response.LoteProductoResponse;
import co.edu.unbosque.backend.model.response.ProductoDetalleResponse;
import co.edu.unbosque.backend.model.response.ProductoResponse;
import co.edu.unbosque.backend.repository.CategoriaRepository;
import co.edu.unbosque.backend.repository.HistorialPrecioProductoRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
     * El metodo verifica si es posible crear un producto luego verifica si el producto existe.
     *
     * Luego se obtiene la catagoria a la que va a pertenecer el producto y si no hay catagoria entonces se asigna null.
     *
     * luego se crea el producto y anteas de guardarlo se valida que el producto creado sea correcto.
     *
     * finalmente se ingresa el lote con todos los datos que tenemos
     * @param request
     * @return Una Instancia Producto
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
        producto.setRequierePrescripcion(request.requierePrescripcion());
        producto.setEstado("ACTIVO");

        validarProducto(producto);
        Producto productoGuardado = productoRepository.save(producto);

        registrarIngresoInicial(productoGuardado, stockInicial, normalizarTexto(request.numeroLote()), request.fechaVencimiento());
        return productoGuardado;
    }

    /**
     * Se valida que los datos de ingreso productos sean adecuados para continuar.
     *
     * @param codigoBarras
     * @param request
     * @return Una Instancia Producto
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

        validarNumeroLoteUnico(normalizarTexto(request.numeroLote()));
        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setNumeroLote(normalizarTexto(request.numeroLote()));
        lote.setCantidad(request.cantidad());
        lote.setFechaVencimiento(request.fechaVencimiento());
        loteGuardado = loteRepository.save(lote);

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

    @Transactional
    public Producto actualizarProducto(Long productoId, ActualizarProductoRequest request) {
        validarActualizacionProducto(request);

        Producto producto = productoRepository.findByIdForUpdate(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + productoId));

        if (request.categoriaId() != null) {
            producto.setCategoria(obtenerCategoriaOpcional(request.categoriaId()));
        }
        if (request.nombre() != null) {
            String nombre = normalizarTexto(request.nombre());
            if (nombre == null) {
                throw new BusinessException("El nombre del producto no puede estar vacío");
            }
            producto.setNombre(nombre);
        }
        if (request.descripcion() != null) {
            producto.setDescripcion(normalizarTexto(request.descripcion()));
        }
        if (request.stockMinimo() != null) {
            producto.setStockMinimo(request.stockMinimo());
        }
        if (request.stockActual() != null) {
            producto.setStockActual(request.stockActual());
        }
        if (request.costo() != null) {
            producto.setCosto(request.costo());
        }
        if (request.precioVenta() != null) {
            producto.setPrecioVenta(request.precioVenta());
        }
        if (request.porcentajeIva() != null) {
            producto.setPorcentajeIva(request.porcentajeIva());
        }
        if (request.requierePrescripcion() != null) {
            producto.setRequierePrescripcion(request.requierePrescripcion());
        }
        if (request.estado() != null) {
            String estado = request.estado().isBlank()
                    ? null
                    : normalizarEstadoProducto(request.estado());
            if (estado == null) {
                throw new BusinessException("El estado del producto no puede estar vacío");
            }
            producto.setEstado(estado);
        }

        validarPrecioVentaSuficiente(
                valorMonetarioSeguro(producto.getCosto()),
                valorMonetarioSeguro(producto.getPrecioVenta()),
                valorMonetarioSeguro(producto.getPorcentajeIva())
        );
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Long productoId) {
        return productoRepository.findByIdWithCategoria(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + productoId));
    }

    @Transactional(readOnly = true)
    public ProductoDetalleResponse obtenerProductoDetallePorId(Long productoId) {
        if (productoId == null) {
            throw new BusinessException("El id del producto es obligatorio");
        }

        Tuple productoDetalle = productoRepository.findDetalleProductoRowById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + productoId));
        List<LoteProductoResponse> lotes = loteRepository.findDetalleLotesRowsByProducto(productoId).stream()
                .map(this::toLoteProductoResponse)
                .toList();

        Long productoDetalleId = toLong(productoDetalle.get("productoId"));
        CategoriaResponse categoria = toCategoriaResponse(productoDetalle);
        String nombre = (String) productoDetalle.get("nombre");
        String descripcion = (String) productoDetalle.get("descripcion");
        String codigoBarras = (String) productoDetalle.get("codigoBarras");
        Integer stockMinimo = toInteger(productoDetalle.get("stockMinimo"));
        Integer stockActual = toInteger(productoDetalle.get("stockActual"));
        Double costo = toDouble(productoDetalle.get("costo"));
        Double precioVenta = toDouble(productoDetalle.get("precioVenta"));
        Double margenGanancia = toDouble(productoDetalle.get("margenGanancia"));
        Double porcentajeIva = toDouble(productoDetalle.get("porcentajeIva"));
        Boolean requierePrescripcion = toBoolean(productoDetalle.get("requierePrescripcion"));
        String estado = (String) productoDetalle.get("estado");

        return new ProductoDetalleResponse(
                productoDetalleId,
                categoria,
                nombre,
                descripcion,
                codigoBarras,
                stockMinimo,
                stockActual,
                costo,
                precioVenta,
                margenGanancia,
                porcentajeIva,
                requierePrescripcion,
                estado,
                lotes
        );
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductosActivos() {
        return productoRepository.findByEstadoOrderByNombreAsc("ACTIVO");
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    @Transactional(readOnly = true)
    public List<Lote> listarLotesPorProducto(Long productoId) {
        if (productoId == null) {
            throw new BusinessException("El id del producto es obligatorio");
        }
        return loteRepository.findByProducto_UniqueIDOrderByFechaVencimientoAsc(productoId);
    }

    /**
     * Busca productos activos por nombre o código de barras.
     * Usado por el frontend al registrar una venta.
     *
     * @param termino fragmento de nombre o código de barras
     * @return productos activos coincidentes
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarActivosPorNombreOCodigo(String termino) {
        return productoRepository.buscarActivosPorNombreOCodigo(termino).stream()
                .map(this::toProductoResponse)
                .toList();
    }

    @Transactional
    public Producto actualizarPrecio(String codigoBarras, CambioPrecioProductoRequest request) {
        validarCambioPrecio(request);

        Producto producto = productoRepository.findByCodigoBarrasForUpdate(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el producto con codigo de barras " + codigoBarras
                ));

        validarPrecioVentaSuficiente(producto.getCosto(), request.nuevoPrecioVenta(), producto.getPorcentajeIva());

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

    private void registrarIngresoInicial(Producto producto, int stockInicial, String numeroLote, LocalDate fechaVencimiento) {
        if (stockInicial <= 0) return;

        Lote loteGuardado = null;
        if (numeroLote != null || fechaVencimiento != null) {
            validarNumeroLoteUnico(numeroLote);
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setNumeroLote(numeroLote);
            lote.setFechaVencimiento(fechaVencimiento);
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
        if (categoriaId == null) return null;
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoria con id " + categoriaId));
    }

    private void validarProducto(Producto producto) {
        if (producto == null) throw new BusinessException("El producto no puede ser nulo");
        if (producto.getNombre() == null || producto.getNombre().isBlank())
            throw new BusinessException("El nombre del producto es obligatorio");
        if (producto.getCodigoBarras() == null || producto.getCodigoBarras().isBlank())
            throw new BusinessException("El codigo de barras del producto es obligatorio");
        if (producto.getCosto() == null || producto.getCosto() < 0)
            throw new BusinessException("El costo del producto debe ser mayor o igual a cero");
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta() < 0)
            throw new BusinessException("El precio de venta debe ser mayor o igual a cero");
        if (producto.getStockActual() == null) producto.setStockActual(0);
        if (producto.getStockMinimo() == null) producto.setStockMinimo(0);
        producto.setMargenGanancia(calcularMargen(producto.getCosto(), producto.getPrecioVenta()));
    }

    private void validarCreacionProducto(CrearProductoRequest request) {
        if (request == null) throw new BusinessException("La solicitud de creacion del producto es obligatoria");
        if (request.nombre() == null || request.nombre().isBlank())
            throw new BusinessException("El nombre del producto es obligatorio");
        if (request.codigoBarras() == null || request.codigoBarras().isBlank())
            throw new BusinessException("El codigo de barras es obligatorio");
        if (request.costo() == null || request.costo() < 0)
            throw new BusinessException("El costo del producto debe ser mayor o igual a cero");
        if (request.precioVenta() == null || request.precioVenta() < 0)
            throw new BusinessException("El precio de venta debe ser mayor o igual a cero");
        if (request.stockInicial() == null || request.stockInicial() <= 0)
            throw new BusinessException("El stock inicial debe ser mayor a cero");
        if (request.stockMinimo() != null && request.stockMinimo() < 0)
            throw new BusinessException("El stock minimo no puede ser negativo");
        if (request.requierePrescripcion() == null)
            throw new BusinessException("El campo requierePrescripcion es obligatorio");
        validarPrecioVentaSuficiente(request.costo(), request.precioVenta(), request.porcentajeIva());
        if (request.fechaVencimiento() != null && request.fechaVencimiento().isBefore(LocalDate.now()))
            throw new BusinessException("La fecha de vencimiento no puede estar en el pasado");
    }

    private void validarNumeroLoteUnico(String numeroLote) {
        if (numeroLote == null) {
            return;
        }
        String numeroNormalizado = numeroLote.trim();
        if (numeroNormalizado.isBlank()) {
            throw new BusinessException("El numero de lote no puede estar vacío");
        }
        if (loteRepository.existsByNumeroLoteIgnoreCase(numeroNormalizado)) {
            throw new BusinessException("Ya existe un lote con el numero " + numeroNormalizado);
        }
    }

    private void validarIngresoProducto(IngresoProductoRequest request) {
        if (request == null) throw new BusinessException("La solicitud de ingreso de producto es obligatoria");
        if (request.cantidad() == null || request.cantidad() <= 0)
            throw new BusinessException("La cantidad a ingresar debe ser mayor a cero");
        if (request.fechaVencimiento() == null)
            throw new BusinessException("La fecha de vencimiento es obligatoria");
        if (request.fechaVencimiento().isBefore(LocalDate.now()))
            throw new BusinessException("La fecha de vencimiento no puede estar en el pasado");
        if (request.nuevoCosto() != null && request.nuevoCosto() < 0)
            throw new BusinessException("El nuevo costo no puede ser negativo");
        if (request.nuevoPrecioVenta() != null && request.nuevoPrecioVenta() < 0)
            throw new BusinessException("El nuevo precio de venta no puede ser negativo");
    }

    private void validarActualizacionProducto(ActualizarProductoRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de actualizacion del producto es obligatoria");
        }
        boolean tieneCambios =
                request.categoriaId() != null
                        || request.nombre() != null
                        || request.descripcion() != null
                        || request.stockMinimo() != null
                        || request.stockActual() != null
                        || request.costo() != null
                        || request.precioVenta() != null
                        || request.porcentajeIva() != null
                        || request.requierePrescripcion() != null
                        || request.estado() != null;
        if (!tieneCambios) {
            throw new BusinessException("La solicitud de actualizacion debe incluir al menos un campo modificable");
        }
        if (request.nombre() != null && request.nombre().isBlank()) {
            throw new BusinessException("El nombre del producto no puede estar vacío");
        }
        if (request.stockMinimo() != null && request.stockMinimo() < 0) {
            throw new BusinessException("El stock minimo no puede ser negativo");
        }
        if (request.stockActual() != null && request.stockActual() < 0) {
            throw new BusinessException("El stock actual no puede ser negativo");
        }
        if (request.estado() != null && request.estado().isBlank()) {
            throw new BusinessException("El estado del producto no puede estar vacío");
        }
        if (request.estado() != null) {
            validarEstadoProducto(request.estado());
        }
    }

    private void validarCambioPrecio(CambioPrecioProductoRequest request) {
        if (request == null) throw new BusinessException("La solicitud de cambio de precio es obligatoria");
        if (request.nuevoCosto() == null || request.nuevoCosto() < 0)
            throw new BusinessException("El nuevo costo debe ser mayor o igual a cero");
        if (request.nuevoPrecioVenta() == null || request.nuevoPrecioVenta() < 0)
            throw new BusinessException("El nuevo precio de venta debe ser mayor o igual a cero");
    }

    private void validarPrecioVentaSuficiente(Double costo, Double precioVenta, Double porcentajeIva) {
        double costoSeguro = valorMonetarioSeguro(costo);
        double ivaSeguro = valorMonetarioSeguro(porcentajeIva);
        double precioMinimo = costoSeguro * (1.0 + (ivaSeguro / 100.0));
        if (precioVenta == null || precioVenta <= precioMinimo) {
            throw new BusinessException("El precio de venta debe ser mayor a " + precioMinimo
                    + " para cubrir el costo y el IVA, y generar ganancia");
        }
    }

    private Double calcularMargen(Double costo, Double precioVenta) {
        if (costo == null || precioVenta == null || costo <= 0) return 0.0;
        return ((precioVenta - costo) / costo) * 100.0;
    }

    private Integer valorEnteroSeguro(Integer valor) { return valor == null ? 0 : valor; }

    private Double valorMonetarioSeguro(Double valor) { return valor == null ? 0.0 : valor; }

    private void validarEstadoProducto(String estado) {
        String normalizado = estado.trim().toUpperCase();
        if (!"ACTIVO".equals(normalizado) && !"INACTIVO".equals(normalizado) && !"DESCONTINUADO".equals(normalizado)) {
            throw new BusinessException("El estado debe ser ACTIVO, INACTIVO o DESCONTINUADO");
        }
    }

    private String normalizarEstadoProducto(String estado) {
        if (estado == null || estado.isBlank()) return "ACTIVO";
        return estado.trim().toUpperCase();
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) return null;
        return texto.trim().replaceAll("\\s+", " ");
    }

    private CategoriaResponse toCategoriaResponse(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }

    private CategoriaResponse toCategoriaResponse(Tuple productoDetalle) {
        Long categoriaId = toLong(productoDetalle.get("categoriaId"));
        String categoriaNombre = (String) productoDetalle.get("categoriaNombre");
        String categoriaDescripcion = (String) productoDetalle.get("categoriaDescripcion");
        if (categoriaId == null && categoriaNombre == null && categoriaDescripcion == null) {
            return null;
        }
        return new CategoriaResponse(categoriaId, categoriaNombre, categoriaDescripcion);
    }

    private LoteProductoResponse toLoteProductoResponse(Tuple loteDetalle) {
        Long loteId = toLong(loteDetalle.get("loteId"));
        String numeroLote = (String) loteDetalle.get("numeroLote");
        LocalDate fechaVencimiento = toLocalDate(loteDetalle.get("fechaVencimiento"));
        Integer cantidad = toInteger(loteDetalle.get("cantidad"));

        return new LoteProductoResponse(
                loteId,
                numeroLote,
                fechaVencimiento,
                cantidad
        );
    }

    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private Integer toInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private Double toDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return null;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.matches("\\d+")) {
            return toLocalDateFromEpoch(Long.parseLong(text));
        }
        return LocalDate.parse(text.length() > 10 ? text.substring(0, 10) : text);
    }

    private LocalDate toLocalDateFromEpoch(long epochValue) {
        long epochSeconds = epochValue > 99_999_999_999L ? epochValue / 1000 : epochValue;
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private ProductoResponse toProductoResponse(Producto producto) {
        List<LoteProductoResponse> lotes = loteRepository.findByProducto_UniqueIDOrderByFechaVencimientoAsc(producto.getUniqueID())
                .stream()
                .map(l -> new LoteProductoResponse(l.getIdLote(), l.getNumeroLote(), l.getFechaVencimiento(), l.getCantidad()))
                .toList();

        return new ProductoResponse(
                producto.getUniqueID(),
                toCategoriaResponse(producto.getCategoria()),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCodigoBarras(),
                producto.getStockMinimo(),
                producto.getStockActual(),
                producto.getCosto(),
                producto.getPrecioVenta(),
                producto.getMargenGanancia(),
                producto.getPorcentajeIva(),
                producto.getRequierePrescripcion(),
                producto.getEstado(),
                lotes
        );
    }

}
