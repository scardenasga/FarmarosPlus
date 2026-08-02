package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.InsufficientStockException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.*;
import co.edu.unbosque.backend.model.request.*;
import co.edu.unbosque.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private MovimientoInventarioRepository movimientoInventarioRepository;

    @InjectMocks
    private VentaService ventaService;

    private Usuario buildVendedor() {
        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setUsername("vendedor1");
        u.setRol("VENDEDOR");
        u.setEstado("ACTIVO");
        return u;
    }

    private Usuario buildAdmin() {
        Usuario u = new Usuario();
        u.setIdUsuario(2L);
        u.setUsername("admin1");
        u.setRol("ADMIN");
        u.setEstado("ACTIVO");
        return u;
    }

    private Producto buildProducto() {
        Producto p = new Producto();
        p.setUniqueID(10L);
        p.setNombre("Ibuprofeno 400mg");
        p.setPrecioVenta(3500.0);
        p.setStockActual(50);
        p.setEstado("ACTIVO");
        return p;
    }

    private Lote buildLote(Producto producto) {
        Lote l = new Lote();
        l.setIdLote(5L);
        l.setNumeroLote("LOT-001");
        l.setCantidad(20);
        l.setProducto(producto);
        return l;
    }

    private CrearVentaRequest buildRequestValido() {
        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, 5L, 2, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 7000.0);
        return new CrearVentaRequest(1L, 0.0, List.of(detalle), List.of(pago));
    }

    // ================================================================
    // registrarVenta
    // ================================================================

    @Test
    void registrarVenta_exitoso_descuentaStockYGuardaMovimiento() {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(lote));
        when(ventaRepository.saveAndFlush(any())).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(100L);
            return v;
        });
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(100L))
                .thenReturn(Optional.empty());

        ventaService.registrarVenta(buildRequestValido());

        assertEquals(48, producto.getStockActual());
        assertEquals(18, lote.getCantidad());
        verify(movimientoInventarioRepository).saveAll(anyList());
    }

    @Test
    void registrarVenta_usuarioNoExiste_lanzaResourceNotFoundException() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ventaService.registrarVenta(buildRequestValido()));

        verify(ventaRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrarVenta_loteNoExiste_lanzaResourceNotFoundException() {
        Producto producto = buildProducto();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ventaService.registrarVenta(buildRequestValido()));
    }

    @Test
    void registrarVenta_loteNoPerteneceAlProducto_lanzaBusinessException() {
        Producto producto = buildProducto();
        Producto otro = buildProducto();
        otro.setUniqueID(99L);
        Lote lote = buildLote(otro);

        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, 5L, 2, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 7000.0);
        CrearVentaRequest request = new CrearVentaRequest(1L, 0.0, List.of(detalle), List.of(pago));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(lote));

        assertThrows(BusinessException.class,
                () -> ventaService.registrarVenta(request));
    }

    @Test
    void registrarVenta_stockProductoInsuficiente_lanzaInsufficientStockException() {
        Producto producto = buildProducto();
        producto.setStockActual(1);
        Lote lote = buildLote(producto);

        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, 5L, 5, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 17500.0);
        CrearVentaRequest request = new CrearVentaRequest(1L, 0.0, List.of(detalle), List.of(pago));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        lenient().when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(lote));

        assertThrows(InsufficientStockException.class,
                () -> ventaService.registrarVenta(request));
    }

    @Test
    void registrarVenta_stockLoteInsuficiente_lanzaInsufficientStockException() {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);
        lote.setCantidad(1);

        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, 5L, 5, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 17500.0);
        CrearVentaRequest request = new CrearVentaRequest(1L, 0.0, List.of(detalle), List.of(pago));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(lote));

        assertThrows(InsufficientStockException.class,
                () -> ventaService.registrarVenta(request));
    }

    @Test
    void registrarVenta_productoInactivo_lanzaBusinessException() {
        Producto producto = buildProducto();
        producto.setEstado("INACTIVO");
        Lote lote = buildLote(producto);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        lenient().when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(lote));

        assertThrows(BusinessException.class,
                () -> ventaService.registrarVenta(buildRequestValido()));
    }

    @Test
    void registrarVenta_descuentoMayorA1_lanzaBusinessException() {
        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, 5L, 2, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 7000.0);
        CrearVentaRequest request = new CrearVentaRequest(1L, 1.5, List.of(detalle), List.of(pago));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));

        assertThrows(BusinessException.class,
                () -> ventaService.registrarVenta(request));
    }

    @Test
void registrarVenta_pagosNoCoinciden_lanzaBusinessException() {
    Producto producto = buildProducto();
    Lote lote = buildLote(producto);

    VentaDetalleRequest detalle = new VentaDetalleRequest(10L, 5L, 2, null);
    PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 999.0);
    CrearVentaRequest request = new CrearVentaRequest(
            1L,
            0.0,
            List.of(detalle),
            List.of(pago)
    );

    when(usuarioRepository.findById(1L))
            .thenReturn(Optional.of(buildVendedor()));

    when(productoRepository.findById(10L))
            .thenReturn(Optional.of(producto));

    when(loteRepository.findByIdForUpdate(5L))
            .thenReturn(Optional.of(lote));

    assertThrows(
            BusinessException.class,
            () -> ventaService.registrarVenta(request)
    );

    verify(ventaRepository, never()).saveAndFlush(any());
}

    @Test
    void registrarVenta_sinDetalles_lanzaBusinessException() {
        CrearVentaRequest request = new CrearVentaRequest(1L, 0.0, List.of(), List.of());

        assertThrows(BusinessException.class,
                () -> ventaService.registrarVenta(request));

        verify(usuarioRepository, never()).findById(any());
    }

    // ================================================================
    // FEFO automatico (sin loteId explicito)
    // ================================================================

    @Test
    void registrarVenta_sinLoteId_seleccionaLoteAutomaticamenteFEFO() {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, null, 2, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 7000.0);
        CrearVentaRequest request = new CrearVentaRequest(1L, 0.0, List.of(detalle), List.of(pago));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findLotesDisponiblesPorProducto(10L)).thenReturn(List.of(lote));
        when(ventaRepository.saveAndFlush(any())).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(100L);
            return v;
        });
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(100L))
                .thenReturn(Optional.empty());

        ventaService.registrarVenta(request);

        assertEquals(48, producto.getStockActual());
        assertEquals(18, lote.getCantidad());
        verify(loteRepository).findLotesDisponiblesPorProducto(10L);
    }

    @Test
    void registrarVenta_sinLoteId_repartidoEntreVariosLotes() {
        Producto producto = buildProducto();

        Lote lote1 = new Lote();
        lote1.setIdLote(1L);
        lote1.setNumeroLote("LOT-001");
        lote1.setCantidad(3);
        lote1.setProducto(producto);

        Lote lote2 = new Lote();
        lote2.setIdLote(2L);
        lote2.setNumeroLote("LOT-002");
        lote2.setCantidad(10);
        lote2.setProducto(producto);

        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, null, 5, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 17500.0);
        CrearVentaRequest request = new CrearVentaRequest(1L, 0.0, List.of(detalle), List.of(pago));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findLotesDisponiblesPorProducto(10L)).thenReturn(List.of(lote1, lote2));
        when(ventaRepository.saveAndFlush(any())).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(100L);
            return v;
        });
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(100L))
                .thenReturn(Optional.empty());

        ventaService.registrarVenta(request);

        assertEquals(0, lote1.getCantidad());
        assertEquals(8, lote2.getCantidad());
        assertEquals(45, producto.getStockActual());
    }

    @Test
    void registrarVenta_sinLoteId_stockInsuficienteEnLotes_lanzaInsufficientStockException() {
        Producto producto = buildProducto();

        Lote lote1 = new Lote();
        lote1.setIdLote(1L);
        lote1.setNumeroLote("LOT-001");
        lote1.setCantidad(2);
        lote1.setProducto(producto);

        VentaDetalleRequest detalle = new VentaDetalleRequest(10L, null, 10, null);
        PagoVentaRequest pago = new PagoVentaRequest("EFECTIVO", 35000.0);
        CrearVentaRequest request = new CrearVentaRequest(1L, 0.0, List.of(detalle), List.of(pago));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findLotesDisponiblesPorProducto(10L)).thenReturn(List.of(lote1));

        assertThrows(InsufficientStockException.class,
                () -> ventaService.registrarVenta(request));
    }

    // ================================================================
    // anularVenta
    // ================================================================

    @Test
    void anularVenta_exitosa_revierteStockYMarcaAnulada() {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(2);
        detalle.setProducto(producto);
        detalle.setLote(lote);

        Venta venta = new Venta();
        venta.setIdVenta(1L);
        venta.setEstado("COMPLETADA");
        venta.setDetalles(new HashSet<>(List.of(detalle)));

        when(usuarioRepository.findByUsernameIgnoreCase("admin1"))
                .thenReturn(Optional.of(buildAdmin()));
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(1L))
                .thenReturn(Optional.of(venta));
        when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(lote));
        when(ventaRepository.save(any())).thenReturn(venta);

        Venta resultado = ventaService.anularVenta(1L, "Error en registro", "admin1");

        assertEquals("ANULADA", resultado.getEstado());
        assertEquals(52, producto.getStockActual());
        assertEquals(22, lote.getCantidad());
        verify(movimientoInventarioRepository).saveAll(anyList());
    }

    @Test
    void anularVenta_ventaNoExiste_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByUsernameIgnoreCase("admin1"))
                .thenReturn(Optional.of(buildAdmin()));
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ventaService.anularVenta(99L, "motivo", "admin1"));
    }

    @Test
    void anularVenta_ventaYaAnulada_lanzaBusinessException() {
        Venta venta = new Venta();
        venta.setIdVenta(1L);
        venta.setEstado("ANULADA");

        when(usuarioRepository.findByUsernameIgnoreCase("admin1"))
                .thenReturn(Optional.of(buildAdmin()));
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(1L))
                .thenReturn(Optional.of(venta));

        assertThrows(BusinessException.class,
                () -> ventaService.anularVenta(1L, "motivo", "admin1"));

        verify(ventaRepository, never()).save(any());
    }

    @Test
    void anularVenta_usuarioSinRolAdmin_lanzaBusinessException() {
        when(usuarioRepository.findByUsernameIgnoreCase("vendedor1"))
                .thenReturn(Optional.of(buildVendedor()));

        assertThrows(BusinessException.class,
                () -> ventaService.anularVenta(1L, "motivo", "vendedor1"));
    }

    @Test
    void anularVenta_usuarioInactivo_lanzaBusinessException() {
        Usuario admin = buildAdmin();
        admin.setEstado("INACTIVO");
        when(usuarioRepository.findByUsernameIgnoreCase("admin1"))
                .thenReturn(Optional.of(admin));

        assertThrows(BusinessException.class,
                () -> ventaService.anularVenta(1L, "motivo", "admin1"));
    }

    @Test
    void anularVenta_motivoVacio_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> ventaService.anularVenta(1L, "", "admin1"));
    }

    @Test
    void anularVenta_usuarioNulo_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> ventaService.anularVenta(1L, "motivo", null));
    }

    // ================================================================
    // eliminarVenta
    // ================================================================

    @Test
    void eliminarVenta_exitosa_delegaEnAnularVenta() {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(2);
        detalle.setProducto(producto);
        detalle.setLote(lote);

        Venta venta = new Venta();
        venta.setIdVenta(1L);
        venta.setEstado("COMPLETADA");
        venta.setDetalles(new HashSet<>(List.of(detalle)));

        AnularVentaRequest request = new AnularVentaRequest(
                "Error de caja", "admin1", 2L, true);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(buildAdmin()));
        when(usuarioRepository.findByUsernameIgnoreCase("admin1"))
                .thenReturn(Optional.of(buildAdmin()));
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(1L))
                .thenReturn(Optional.of(venta));
        when(loteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(lote));
        when(ventaRepository.save(any())).thenReturn(venta);

        Venta resultado = ventaService.eliminarVenta(1L, request);

        assertEquals("ANULADA", resultado.getEstado());
    }

    @Test
    void eliminarVenta_sinConfirmacion_lanzaBusinessException() {
        AnularVentaRequest request = new AnularVentaRequest(
                "motivo", "admin1", 2L, false);

        assertThrows(BusinessException.class,
                () -> ventaService.eliminarVenta(1L, request));

        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void eliminarVenta_usuarioSinPermiso_lanzaBusinessException() {
        AnularVentaRequest request = new AnularVentaRequest(
                "motivo", "vendedor1", 1L, true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildVendedor()));

        assertThrows(BusinessException.class,
                () -> ventaService.eliminarVenta(1L, request));
    }

    @Test
    void eliminarVenta_usuarioInactivo_lanzaBusinessException() {
        Usuario admin = buildAdmin();
        admin.setEstado("INACTIVO");
        AnularVentaRequest request = new AnularVentaRequest(
                "motivo", "admin1", 2L, true);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin));

        assertThrows(BusinessException.class,
                () -> ventaService.eliminarVenta(1L, request));
    }
}