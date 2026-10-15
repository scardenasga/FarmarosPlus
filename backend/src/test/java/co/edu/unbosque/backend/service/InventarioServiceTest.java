package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.InsufficientStockException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.AjusteInventarioRequest;
import co.edu.unbosque.backend.model.request.IngresoLoteRequest;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private InventarioService inventarioService;

    private Producto buildProducto() {
        Producto p = new Producto();
        p.setUniqueID(1L);
        p.setNombre("Amoxicilina 500mg");
        p.setStockActual(100);
        p.setEstado("ACTIVO");
        return p;
    }

    private Lote buildLote(Producto producto) {
        Lote l = new Lote();
        l.setIdLote(1L);
        l.setNumeroLote("LOT-001");
        l.setCantidad(50);
        l.setProducto(producto);
        l.setFechaVencimiento(LocalDate.now().plusYears(1));
        return l;
    }

    // ================================================================
    // registrarIngresoLote
    // ================================================================

    @Test
    void registrarIngresoLote_exitoso_incrementaStockYGuardaMovimiento() {
        Producto producto = buildProducto();
        IngresoLoteRequest request = new IngresoLoteRequest(
                1L, "LOT-NEW", LocalDate.now().plusMonths(6), 25, "Entrada inicial", null
        );

        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(producto));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> {
            Lote l = inv.getArgument(0);
            l.setIdLote(2L);
            return l;
        });
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        Lote resultado = inventarioService.registrarIngresoLote(request);

        assertEquals(125, producto.getStockActual());
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarIngresoLote_productoNoExiste_lanzaResourceNotFoundException() {
        IngresoLoteRequest request = new IngresoLoteRequest(
                99L, "LOT-999", LocalDate.now().plusMonths(6), 10, null, null
        );

        when(productoRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventarioService.registrarIngresoLote(request));
    }

    @Test
    void registrarIngresoLote_requestNulo_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> inventarioService.registrarIngresoLote(null));
    }

    @Test
    void registrarIngresoLote_sinProductoId_lanzaBusinessException() {
        IngresoLoteRequest request = new IngresoLoteRequest(
                null, "LOT-001", LocalDate.now().plusMonths(6), 10, null, null
        );

        assertThrows(BusinessException.class,
                () -> inventarioService.registrarIngresoLote(request));
    }

    @Test
    void registrarIngresoLote_numeroLoteVacio_lanzaBusinessException() {
        IngresoLoteRequest request = new IngresoLoteRequest(
                1L, "   ", LocalDate.now().plusMonths(6), 10, null, null
        );

        assertThrows(BusinessException.class,
                () -> inventarioService.registrarIngresoLote(request));
    }

    @Test
    void registrarIngresoLote_cantidadCero_lanzaBusinessException() {
        IngresoLoteRequest request = new IngresoLoteRequest(
                1L, "LOT-001", LocalDate.now().plusMonths(6), 0, null, null
        );

        assertThrows(BusinessException.class,
                () -> inventarioService.registrarIngresoLote(request));
    }

    // ================================================================
    // ajustarInventario
    // ================================================================

    @Test
    void ajustarInventario_positivo_incrementaStockYLote() {
        Producto producto = buildProducto();
        producto.setStockActual(100);
        Lote lote = buildLote(producto);
        lote.setCantidad(50);

        AjusteInventarioRequest request = new AjusteInventarioRequest(
                1L, 10, "AJUSTE", "Conteo fisico", null
        );

        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(producto));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        Lote resultado = inventarioService.ajustarInventario(request);

        assertEquals(60, lote.getCantidad());
        assertEquals(110, producto.getStockActual());
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void ajustarInventario_negativo_decrementaStockYLote() {
        Producto producto = buildProducto();
        producto.setStockActual(100);
        Lote lote = buildLote(producto);
        lote.setCantidad(50);

        AjusteInventarioRequest request = new AjusteInventarioRequest(
                1L, -5, "VENTA", "Venta mostrador", null
        );

        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(producto));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(currentUserService.getCurrentUsername()).thenReturn("SISTEMA");

        Lote resultado = inventarioService.ajustarInventario(request);

        assertEquals(45, lote.getCantidad());
        assertEquals(95, producto.getStockActual());
    }

    @Test
    void ajustarInventario_loteNoExiste_lanzaResourceNotFoundException() {
        AjusteInventarioRequest request = new AjusteInventarioRequest(
                99L, 10, "AJUSTE", "Conteo", null
        );

        when(loteRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventarioService.ajustarInventario(request));
    }

    @Test
    void ajustarInventario_stockLoteInsuficiente_lanzaInsufficientStockException() {
        Producto producto = buildProducto();
        producto.setStockActual(10);
        Lote lote = buildLote(producto);
        lote.setCantidad(5);

        AjusteInventarioRequest request = new AjusteInventarioRequest(
                1L, -10, "AJUSTE", "Salida mayor", null
        );

        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(producto));

        assertThrows(InsufficientStockException.class,
                () -> inventarioService.ajustarInventario(request));
    }

    @Test
    void ajustarInventario_stockProductoInsuficiente_lanzaInsufficientStockException() {
        Producto producto = buildProducto();
        producto.setStockActual(5);
        Lote lote = buildLote(producto);
        lote.setCantidad(10);

        AjusteInventarioRequest request = new AjusteInventarioRequest(
                1L, -10, "AJUSTE", "Salida mayor", null
        );

        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(producto));

        assertThrows(InsufficientStockException.class,
                () -> inventarioService.ajustarInventario(request));
    }

    @Test
    void ajustarInventario_diferenciaCero_lanzaBusinessException() {
        AjusteInventarioRequest request = new AjusteInventarioRequest(
                1L, 0, "AJUSTE", null, null
        );

        assertThrows(BusinessException.class,
                () -> inventarioService.ajustarInventario(request));
    }

    @Test
    void ajustarInventario_requestNulo_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> inventarioService.ajustarInventario(null));
    }

    // ================================================================
    // listarLotesDisponiblesPorProducto
    // ================================================================

    @Test
    void listarLotesDisponiblesPorProducto_debeRetornarLotes() {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        when(loteRepository.findLotesDisponiblesPorProducto(1L)).thenReturn(List.of(lote));

        List<Lote> resultado = inventarioService.listarLotesDisponiblesPorProducto(1L);

        assertEquals(1, resultado.size());
        assertEquals("LOT-001", resultado.getFirst().getNumeroLote());
    }

    // ================================================================
    // listarLotesProximosAVencer
    // ================================================================

    @Test
    void listarLotesProximosAVencer_debeRetornarLotesQueVencen() {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        when(loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(
                LocalDate.now(), LocalDate.now().plusMonths(1)))
                .thenReturn(List.of(lote));

        List<Lote> resultado = inventarioService.listarLotesProximosAVencer(LocalDate.now().plusMonths(1));

        assertEquals(1, resultado.size());
    }

    // ================================================================
    // listarMovimientosPorProducto
    // ================================================================

    @Test
    void listarMovimientosPorProducto_debeRetornarMovimientos() {
        Producto producto = buildProducto();
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setIdMovimiento(1L);
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento("COMPRA");

        when(movimientoInventarioRepository.findByProducto_UniqueIDOrderByFechaMovimientoDesc(1L))
                .thenReturn(List.of(movimiento));

        List<MovimientoInventario> resultado = inventarioService.listarMovimientosPorProducto(1L);

        assertEquals(1, resultado.size());
        assertEquals("COMPRA", resultado.getFirst().getTipoMovimiento());
    }
}