package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.entity.HistorialPrecioProducto;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.ActualizarProductoRequest;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import co.edu.unbosque.backend.model.request.CrearProductoRequest;
import co.edu.unbosque.backend.model.request.IngresoProductoRequest;
import co.edu.unbosque.backend.model.response.ProductoDetalleResponse;
import co.edu.unbosque.backend.model.response.ProductoResponse;
import co.edu.unbosque.backend.repository.CategoriaRepository;
import co.edu.unbosque.backend.repository.HistorialPrecioProductoRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private HistorialPrecioProductoRepository historialPrecioProductoRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProducto_sinLote_debeCrearMovimientoInicial() {
        CrearProductoRequest request = new CrearProductoRequest(
                null,
                "Vitamina C",
                null,
                "7701234500001",
                5,
                20,
                5000.0,
                8000.0,
                0.0,
                false,
                null,
                null
        );

        when(productoRepository.existsByCodigoBarrasIgnoreCase("7701234500001")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setUniqueID(1L);
            return producto;
        });
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        Producto resultado = productoService.crearProducto(request);

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository).save(movimientoCaptor.capture());
        MovimientoInventario movimiento = movimientoCaptor.getValue();

        assertEquals("7701234500001", resultado.getCodigoBarras());
        assertEquals(20, resultado.getStockActual());
        assertEquals("COMPRA", movimiento.getTipoMovimiento());
        assertNull(movimiento.getLote());
        assertEquals("SISTEMA", movimiento.getUsuarioResponsable());
    }

    @Test
    void crearProducto_conLote_debeCrearLoteYMovimientoInicial() {
        CrearProductoRequest request = new CrearProductoRequest(
                3L,
                "Amoxicilina",
                "Caja",
                "7709876543210",
                10,
                50,
                15000.0,
                22000.0,
                0.0,
                true,
                LocalDate.now().plusYears(1),
                "LOT-01"
        );

        Categoria categoria = new Categoria();
        categoria.setIdCategoria(3L);
        categoria.setNombre("Antibioticos");

        when(productoRepository.existsByCodigoBarrasIgnoreCase("7709876543210")).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setUniqueID(9L);
            return producto;
        });
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> {
            Lote lote = invocation.getArgument(0);
            lote.setIdLote(4L);
            return lote;
        });
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        productoService.crearProducto(request);

        ArgumentCaptor<Lote> loteCaptor = ArgumentCaptor.forClass(Lote.class);
        verify(loteRepository).save(loteCaptor.capture());
        assertEquals("LOT-01", loteCaptor.getValue().getNumeroLote());
        assertEquals(request.fechaVencimiento(), loteCaptor.getValue().getFechaVencimiento());
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void crearProducto_sinNumeroLote_peroConFechaVencimiento_debeCrearLoteConNumeroNulo() {
        CrearProductoRequest request = new CrearProductoRequest(
                null,
                "Jarabe",
                null,
                "7701111111111",
                2,
                15,
                3000.0,
                5000.0,
                0.0,
                false,
                LocalDate.now().plusMonths(6),
                null
        );

        when(productoRepository.existsByCodigoBarrasIgnoreCase("7701111111111")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setUniqueID(11L);
            return producto;
        });
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        Producto resultado = productoService.crearProducto(request);

        ArgumentCaptor<Lote> loteCaptor = ArgumentCaptor.forClass(Lote.class);
        verify(loteRepository).save(loteCaptor.capture());

        Lote loteGuardado = loteCaptor.getValue();
        assertEquals(15, resultado.getStockActual());
        assertNull(loteGuardado.getNumeroLote());
        assertEquals(request.fechaVencimiento(), loteGuardado.getFechaVencimiento());
        assertEquals(15, loteGuardado.getCantidad());
    }

    @Test
    void crearProducto_sinStockInicial_debeLanzarBusinessException() {
        CrearProductoRequest request = new CrearProductoRequest(
                null,
                "Vitamina C",
                null,
                "7701234500001",
                5,
                0,
                5000.0,
                8000.0,
                0.0,
                false,
                null,
                null
        );

        assertThrows(BusinessException.class, () -> productoService.crearProducto(request));
    }

    @Test
    void crearProducto_conPrecioInsuficienteParaCubrirIva_debeLanzarBusinessException() {
        CrearProductoRequest request = new CrearProductoRequest(
                null,
                "Acetaminofen",
                null,
                "7709999999999",
                0,
                10,
                1000.0,
                1100.0,
                19.0,
                false,
                null,
                null
        );

        assertThrows(BusinessException.class, () -> productoService.crearProducto(request));
    }

    @Test
    void crearProducto_conNumeroLoteDuplicado_debeLanzarBusinessException() {
        CrearProductoRequest request = new CrearProductoRequest(
                null,
                "Jarabe",
                null,
                "7708888888888",
                0,
                10,
                3000.0,
                5000.0,
                0.0,
                false,
                LocalDate.now().plusMonths(6),
                "LOT-001"
        );

        when(productoRepository.existsByCodigoBarrasIgnoreCase("7708888888888")).thenReturn(false);
        when(loteRepository.existsByNumeroLoteIgnoreCase("LOT-001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> productoService.crearProducto(request));
    }

    @Test
    void ingresarStock_conCambioPrecio_debeCrearMovimientoEHistorial() {
        IngresoProductoRequest request = new IngresoProductoRequest(30, "LOT-02", 16000.0, 23000.0, LocalDate.now().plusYears(1));

        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Amoxicilina");
        producto.setCodigoBarras("7709876543210");
        producto.setStockActual(20);
        producto.setCosto(15000.0);
        producto.setPrecioVenta(22000.0);

        when(productoRepository.findByCodigoBarrasForUpdate("7709876543210")).thenReturn(Optional.of(producto));
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        Producto resultado = productoService.ingresarStock("7709876543210", request);

        assertEquals(50, resultado.getStockActual());
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
        verify(historialPrecioProductoRepository).save(any(HistorialPrecioProducto.class));
    }

    @Test
    void actualizarPrecio_porCodigoBarras_debeActualizarYCrearHistorial() {
        CambioPrecioProductoRequest request = new CambioPrecioProductoRequest(9000.0, 13000.0, null);

        Producto producto = new Producto();
        producto.setUniqueID(7L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setCosto(8000.0);
        producto.setPrecioVenta(12000.0);

        when(productoRepository.findByCodigoBarrasForUpdate("7701234567890")).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        Producto resultado = productoService.actualizarPrecio("7701234567890", request);

        assertEquals(9000.0, resultado.getCosto());
        assertEquals(13000.0, resultado.getPrecioVenta());

        ArgumentCaptor<HistorialPrecioProducto> historialCaptor = ArgumentCaptor.forClass(HistorialPrecioProducto.class);
        verify(historialPrecioProductoRepository).save(historialCaptor.capture());
        assertEquals("SISTEMA", historialCaptor.getValue().getUsuarioResponsable());
    }

    @Test
    void actualizarProducto_porId_debeActualizarCamposEditables() {
        ActualizarProductoRequest request = new ActualizarProductoRequest(
                3L,
                "Acetaminofen 650mg",
                "Caja por 20 tabletas",
                12,
                120,
                9000.0,
                13500.0,
                0.0,
                false,
                "INACTIVO"
        );

        Categoria categoria = new Categoria();
        categoria.setIdCategoria(3L);
        categoria.setNombre("Antibioticos");

        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen");
        producto.setDescripcion("Caja");
        producto.setCodigoBarras("7701234567890");
        producto.setStockMinimo(5);
        producto.setCosto(8000.0);
        producto.setPrecioVenta(12000.0);
        producto.setPorcentajeIva(0.0);
        producto.setRequierePrescripcion(true);
        producto.setEstado("ACTIVO");
        producto.setCategoria(new Categoria());

        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.actualizarProducto(1L, request);

        assertEquals("Acetaminofen 650mg", resultado.getNombre());
        assertEquals("Caja por 20 tabletas", resultado.getDescripcion());
        assertEquals(12, resultado.getStockMinimo());
        assertEquals(120, resultado.getStockActual());
        assertEquals(9000.0, resultado.getCosto());
        assertEquals(13500.0, resultado.getPrecioVenta());
        assertEquals(0.0, resultado.getPorcentajeIva());
        assertEquals(false, resultado.getRequierePrescripcion());
        assertEquals("INACTIVO", resultado.getEstado());
        assertEquals(3L, resultado.getCategoria().getIdCategoria());
    }

    @Test
    void listarLotesPorProducto_debeRetornarLotesOrdenadosPorVencimiento() {
        Producto producto = new Producto();
        producto.setUniqueID(1L);

        Lote lote1 = new Lote();
        lote1.setIdLote(1L);
        lote1.setNumeroLote("LOT-002");
        lote1.setFechaVencimiento(LocalDate.now().plusYears(2));
        lote1.setCantidad(20);
        lote1.setProducto(producto);

        Lote lote2 = new Lote();
        lote2.setIdLote(2L);
        lote2.setNumeroLote("LOT-001");
        lote2.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote2.setCantidad(10);
        lote2.setProducto(producto);

        when(loteRepository.findByProducto_UniqueIDOrderByFechaVencimientoAsc(1L)).thenReturn(List.of(lote2, lote1));

        List<Lote> lotes = productoService.listarLotesPorProducto(1L);

        assertEquals(2, lotes.size());
        assertEquals("LOT-001", lotes.get(0).getNumeroLote());
        assertEquals("LOT-002", lotes.get(1).getNumeroLote());
    }

    @Test
    void obtenerProductoDetallePorId_debeIncluirCategoriaYLotes() {
        Tuple productoDetalle = mock(Tuple.class);
        when(productoDetalle.get("productoId")).thenReturn(1L);
        when(productoDetalle.get("categoriaId")).thenReturn(5L);
        when(productoDetalle.get("categoriaNombre")).thenReturn("Analgesicos");
        when(productoDetalle.get("categoriaDescripcion")).thenReturn("Dolor y fiebre");
        when(productoDetalle.get("nombre")).thenReturn("Acetaminofen");
        when(productoDetalle.get("descripcion")).thenReturn("Caja");
        when(productoDetalle.get("codigoBarras")).thenReturn("7701234567890");
        when(productoDetalle.get("stockMinimo")).thenReturn(5);
        when(productoDetalle.get("stockActual")).thenReturn(20);
        when(productoDetalle.get("costo")).thenReturn(8000.0);
        when(productoDetalle.get("precioVenta")).thenReturn(12000.0);
        when(productoDetalle.get("margenGanancia")).thenReturn(50.0);
        when(productoDetalle.get("porcentajeIva")).thenReturn(0.0);
        when(productoDetalle.get("requierePrescripcion")).thenReturn(false);
        when(productoDetalle.get("estado")).thenReturn("ACTIVO");

        Tuple loteDetalle = mock(Tuple.class);
        when(loteDetalle.get("loteId")).thenReturn(9L);
        when(loteDetalle.get("numeroLote")).thenReturn("LOT-009");
        when(loteDetalle.get("fechaVencimiento")).thenReturn("1830229200");
        when(loteDetalle.get("cantidad")).thenReturn(15);

        when(productoRepository.findDetalleProductoRowById(1L)).thenReturn(Optional.of(productoDetalle));
        when(loteRepository.findDetalleLotesRowsByProducto(1L)).thenReturn(List.of(loteDetalle));

        ProductoDetalleResponse respuesta = productoService.obtenerProductoDetallePorId(1L);

        assertEquals(1L, respuesta.id());
        assertEquals("7701234567890", respuesta.codigoBarras());
        assertEquals("Analgesicos", respuesta.categoria().nombre());
        assertEquals(1, respuesta.lotes().size());
        assertEquals("LOT-009", respuesta.lotes().get(0).numeroLote());
        assertEquals(LocalDate.of(2027, 12, 31), respuesta.lotes().get(0).fechaVencimiento());
    }

    @Test
    void buscarActivosPorNombreOCodigo_debeMapearAProductoResponse() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(4L);
        categoria.setNombre("Analgesicos");
        categoria.setDescripcion("Dolor y fiebre");

        Producto producto = new Producto();
        producto.setUniqueID(7L);
        producto.setCategoria(categoria);
        producto.setNombre("Acetaminofen");
        producto.setDescripcion("Caja");
        producto.setCodigoBarras("7701234567890");
        producto.setStockMinimo(5);
        producto.setStockActual(20);
        producto.setCosto(8000.0);
        producto.setPrecioVenta(12000.0);
        producto.setMargenGanancia(50.0);
        producto.setPorcentajeIva(0.0);
        producto.setRequierePrescripcion(false);
        producto.setEstado("ACTIVO");

        when(productoRepository.buscarActivosPorNombreOCodigo("acetaminofen")).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = productoService.buscarActivosPorNombreOCodigo("acetaminofen");

        assertEquals(1, resultado.size());
        assertEquals("7701234567890", resultado.get(0).codigoBarras());
        assertEquals("Analgesicos", resultado.get(0).categoria().nombre());
    }

    @Test
    void ingresarStock_sinCambioDePrecio_noDebeCrearHistorial() {
        IngresoProductoRequest request = new IngresoProductoRequest(10, null, null, null, LocalDate.now().plusMonths(6));

        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setStockActual(5);
        producto.setCosto(8000.0);
        producto.setPrecioVenta(12000.0);

        when(productoRepository.findByCodigoBarrasForUpdate("7701234567890")).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        productoService.ingresarStock("7701234567890", request);

        verify(historialPrecioProductoRepository, never()).save(any(HistorialPrecioProducto.class));
    }

    @Test
    void ingresarStock_sinFechaVencimiento_debeLanzarBusinessException() {
        IngresoProductoRequest request = new IngresoProductoRequest(10, null, null, null, null);

        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setStockActual(5);
        producto.setCosto(8000.0);
        producto.setPrecioVenta(12000.0);

        assertThrows(BusinessException.class, () -> productoService.ingresarStock("7701234567890", request));
    }
}
