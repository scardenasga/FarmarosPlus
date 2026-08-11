package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.DetalleDevolucionCliente;
import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.DevolucionCliente;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.model.request.ActualizarDevolucionClienteRequest;
import co.edu.unbosque.backend.model.request.DetalleDevolucionClienteRequest;
import co.edu.unbosque.backend.model.request.RegistrarDevolucionClienteRequest;
import co.edu.unbosque.backend.model.response.DevolucionClienteResponse;
import co.edu.unbosque.backend.repository.DetalleDevolucionClienteRepository;
import co.edu.unbosque.backend.repository.DevolucionClienteRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import co.edu.unbosque.backend.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevolucionClienteServiceTest {

    @Mock private DevolucionClienteRepository devolucionRepository;
    @Mock private DetalleDevolucionClienteRepository detalleDevolucionRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;

    @InjectMocks
    private DevolucionClienteService devolucionClienteService;

    private Usuario buildUsuario() {
        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setUsername("admin");
        u.setNombreCompleto("Administrador");
        u.setEstado("ACTIVO");
        u.setRol("ADMIN");
        return u;
    }

    private Producto buildProducto(Long id, int stock) {
        Producto p = new Producto();
        p.setUniqueID(id);
        p.setNombre("Acetaminofen");
        p.setStockActual(stock);
        return p;
    }

    private Lote buildLote(Long id, Producto producto, int cantidad) {
        Lote lote = new Lote();
        lote.setIdLote(id);
        lote.setNumeroLote("L001");
        lote.setCantidad(cantidad);
        lote.setProducto(producto);
        return lote;
    }

    private Venta buildVenta(DetalleVenta detalleVenta) {
        Venta venta = new Venta();
        venta.setIdVenta(5L);
        venta.setEstado("COMPLETADA");
        venta.setDetalles(new HashSet<>(List.of(detalleVenta)));
        return venta;
    }

    @Test
    void registrar_debeIncrementarStockYGuardarMovimiento() {
        Usuario usuario = buildUsuario();
        Producto producto = buildProducto(10L, 20);
        Lote lote = buildLote(1L, producto, 5);

        DetalleVenta detalleVenta = new DetalleVenta();
        detalleVenta.setIdDetalle(12L);
        detalleVenta.setProducto(producto);
        detalleVenta.setLote(lote);
        detalleVenta.setCantidad(3);

        Venta venta = buildVenta(detalleVenta);

        when(ventaRepository.findWithDetallesAndPagosByIdVenta(5L)).thenReturn(Optional.of(venta));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(detalleDevolucionRepository.sumarCantidadDevueltaPorDetalleVenta(12L)).thenReturn(0);
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(devolucionRepository.save(any())).thenAnswer(invocation -> {
            DevolucionCliente devolucion = invocation.getArgument(0);
            devolucion.setIdDevolucionCliente(99L);
            return devolucion;
        });
        when(movimientoRepository.saveAll(any())).thenReturn(List.of());

        RegistrarDevolucionClienteRequest request = new RegistrarDevolucionClienteRequest(
                5L,
                "admin",
                "Juan Perez",
                "100200300",
                "No quedo satisfecho",
                List.of(new DetalleDevolucionClienteRequest(12L, 2))
        );

        DevolucionClienteResponse response = devolucionClienteService.registrar(request);

        assertEquals(22, producto.getStockActual());
        assertEquals(7, lote.getCantidad());
        assertEquals(5L, response.idVenta());
        assertEquals("Juan Perez", response.nombreCliente());
        assertEquals(1, response.detalles().size());
    }

    @Test
    void registrar_cuandoExcedeCantidadDisponible_debeFallAR() {
        Usuario usuario = buildUsuario();
        Producto producto = buildProducto(10L, 20);
        Lote lote = buildLote(1L, producto, 5);

        DetalleVenta detalleVenta = new DetalleVenta();
        detalleVenta.setIdDetalle(12L);
        detalleVenta.setProducto(producto);
        detalleVenta.setLote(lote);
        detalleVenta.setCantidad(3);

        Venta venta = buildVenta(detalleVenta);

        when(ventaRepository.findWithDetallesAndPagosByIdVenta(5L)).thenReturn(Optional.of(venta));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(detalleDevolucionRepository.sumarCantidadDevueltaPorDetalleVenta(12L)).thenReturn(1);

        RegistrarDevolucionClienteRequest request = new RegistrarDevolucionClienteRequest(
                5L,
                "admin",
                null,
                null,
                null,
                List.of(new DetalleDevolucionClienteRequest(12L, 3))
        );

        assertThrows(BusinessException.class, () -> devolucionClienteService.registrar(request));
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    void registrar_cuandoVentaNoExiste_debeLanzarResourceNotFoundException() {
        when(ventaRepository.findWithDetallesAndPagosByIdVenta(9L)).thenReturn(Optional.empty());

        RegistrarDevolucionClienteRequest request = new RegistrarDevolucionClienteRequest(
                9L,
                "admin",
                null,
                null,
                null,
                List.of(new DetalleDevolucionClienteRequest(12L, 1))
        );

        assertThrows(ResourceNotFoundException.class, () -> devolucionClienteService.registrar(request));
    }

    @Test
    void listar_debeRetornarLista() {
        DevolucionCliente devolucion = new DevolucionCliente();
        devolucion.setIdDevolucionCliente(1L);
        devolucion.setFecha(java.time.LocalDateTime.now());
        devolucion.setVenta(new Venta());
        devolucion.getVenta().setIdVenta(5L);
        devolucion.setUsuario(new Usuario());
        devolucion.getUsuario().setUsername("admin");
        devolucion.setDetalles(List.of());

        when(devolucionRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(devolucion));

        List<DevolucionClienteResponse> resultado = devolucionClienteService.listar();

        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.getFirst().idVenta());
    }

    // ── eliminar ─────────────────────────────────────────────────────────────

    @Test
    void eliminar_debeDescontarStockDeLoteYProductoYGuardarMovimiento() {
        Usuario usuario = buildUsuario();
        Producto producto = buildProducto(10L, 22);
        Lote lote = buildLote(1L, producto, 7);

        DetalleVenta detalleVenta = new DetalleVenta();
        detalleVenta.setIdDetalle(12L);
        detalleVenta.setProducto(producto);
        detalleVenta.setLote(lote);
        detalleVenta.setCantidad(3);

        Venta venta = buildVenta(detalleVenta);

        DetalleDevolucionCliente detalle = new DetalleDevolucionCliente();
        detalle.setDetalleVenta(detalleVenta);
        detalle.setProducto(producto);
        detalle.setLote(lote);
        detalle.setNombreProducto(producto.getNombre());
        detalle.setNumeroLote(lote.getNumeroLote());
        detalle.setCantidad(2);

        DevolucionCliente devolucion = new DevolucionCliente();
        devolucion.setIdDevolucionCliente(99L);
        devolucion.setVenta(venta);
        devolucion.setUsuario(usuario);
        devolucion.setUsuarioResponsable("admin");
        devolucion.setDetalles(List.of(detalle));

        when(devolucionRepository.findWithDetallesByIdDevolucionCliente(99L)).thenReturn(Optional.of(devolucion));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(movimientoRepository.saveAll(any())).thenReturn(List.of());

        devolucionClienteService.eliminar(99L, "admin");

        assertEquals(20, producto.getStockActual());
        assertEquals(5, lote.getCantidad());
        verify(devolucionRepository).delete(devolucion);
    }

    @Test
    void eliminar_cuandoStockInsuficiente_debeLanzarBusinessException() {
        Usuario usuario = buildUsuario();
        Producto producto = buildProducto(10L, 1);
        Lote lote = buildLote(1L, producto, 1);

        DetalleVenta detalleVenta = new DetalleVenta();
        detalleVenta.setIdDetalle(12L);
        detalleVenta.setProducto(producto);
        detalleVenta.setLote(lote);
        detalleVenta.setCantidad(3);

        Venta venta = buildVenta(detalleVenta);

        DetalleDevolucionCliente detalle = new DetalleDevolucionCliente();
        detalle.setDetalleVenta(detalleVenta);
        detalle.setProducto(producto);
        detalle.setLote(lote);
        detalle.setNombreProducto(producto.getNombre());
        detalle.setCantidad(2);

        DevolucionCliente devolucion = new DevolucionCliente();
        devolucion.setIdDevolucionCliente(99L);
        devolucion.setVenta(venta);
        devolucion.setUsuario(usuario);
        devolucion.setDetalles(List.of(detalle));

        when(devolucionRepository.findWithDetallesByIdDevolucionCliente(99L)).thenReturn(Optional.of(devolucion));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        assertThrows(BusinessException.class, () -> devolucionClienteService.eliminar(99L, "admin"));
        verify(devolucionRepository, never()).delete(any());
    }

    // ── actualizar ───────────────────────────────────────────────────────────

    @Test
    void actualizar_debeModificarDatosInformativos() {
        Usuario usuario = buildUsuario();
        Venta venta = new Venta();
        venta.setIdVenta(5L);
        venta.setEstado("COMPLETADA");
        venta.setDetalles(new HashSet<>());

        DevolucionCliente devolucion = new DevolucionCliente();
        devolucion.setIdDevolucionCliente(99L);
        devolucion.setVenta(venta);
        devolucion.setUsuario(usuario);
        devolucion.setUsuarioResponsable("admin");
        devolucion.setDetalles(List.of());

        when(devolucionRepository.findWithDetallesByIdDevolucionCliente(99L)).thenReturn(Optional.of(devolucion));
        when(devolucionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DevolucionClienteResponse response = devolucionClienteService.actualizar(99L,
                new ActualizarDevolucionClienteRequest("Nuevo Cliente", "123456", "Otro motivo"));

        assertEquals("Nuevo Cliente", response.nombreCliente());
        assertEquals("123456", response.documentoCliente());
        assertEquals("Otro motivo", response.motivo());
    }
}
