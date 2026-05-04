package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
                "ACTIVO",
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
                "ACTIVO",
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

        verify(loteRepository).save(any(Lote.class));
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
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
                "ACTIVO",
                null
        );

        assertThrows(BusinessException.class, () -> productoService.crearProducto(request));
    }

    @Test
    void ingresarStock_conCambioPrecio_debeCrearMovimientoEHistorial() {
        IngresoProductoRequest request = new IngresoProductoRequest(30, "LOT-02", 16000.0, 23000.0);

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
    void ingresarStock_sinCambioDePrecio_noDebeCrearHistorial() {
        IngresoProductoRequest request = new IngresoProductoRequest(10, null, null, null);

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
}
