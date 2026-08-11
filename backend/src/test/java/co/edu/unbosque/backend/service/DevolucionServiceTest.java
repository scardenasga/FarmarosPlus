package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.InsufficientStockException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.*;
import co.edu.unbosque.backend.model.request.ActualizarDevolucionRequest;
import co.edu.unbosque.backend.model.request.DetalleDevolucionRequest;
import co.edu.unbosque.backend.model.request.RegistrarDevolucionRequest;
import co.edu.unbosque.backend.model.response.DevolucionResponse;
import co.edu.unbosque.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {

    @Mock private DevolucionProveedorRepository devolucionRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DevolucionService devolucionService;

    private Proveedor buildProveedor() {
        Proveedor p = new Proveedor();
        p.setIdProveedor(1L);
        p.setNombre("Proveedor ABC");
        return p;
    }

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
        p.setNombre("Amoxicilina");
        p.setStockActual(stock);
        return p;
    }

    private Lote buildLote(Long id, Producto producto, int cantidad) {
        Lote l = new Lote();
        l.setIdLote(id);
        l.setNumeroLote("L001");
        l.setCantidad(cantidad);
        l.setProducto(producto);
        return l;
    }

    private DevolucionProveedor buildDevolucionGuardada(Proveedor proveedor) {
        DevolucionProveedor d = new DevolucionProveedor();
        d.setIdDevolucion(10L);
        d.setProveedor(proveedor);
        d.setUsuarioResponsable("admin");
        d.setDetalles(List.of());
        return d;
    }

    // ── registrar ────────────────────────────────────────────────────────────

    @Test
    void registrar_debeDescontarStockDeLoteYProducto() {
        Proveedor proveedor = buildProveedor();
        Usuario usuario = buildUsuario();
        Producto producto = buildProducto(10L, 50);
        Lote lote = buildLote(1L, producto, 20);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(devolucionRepository.save(any())).thenAnswer(i -> {
            DevolucionProveedor d = i.getArgument(0);
            d.setIdDevolucion(10L);
            return d;
        });
        when(movimientoRepository.saveAll(any())).thenReturn(List.of());

        RegistrarDevolucionRequest request = new RegistrarDevolucionRequest(
                1L, "admin", "Producto vencido",
                List.of(new DetalleDevolucionRequest(10L, 1L, 5)));

        devolucionService.registrar(request);

        assertEquals(45, producto.getStockActual());
        assertEquals(15, lote.getCantidad());
    }

    @Test
    void registrar_cuandoStockLoteInsuficiente_debeLanzarInsufficientStockException() {
        Proveedor proveedor = buildProveedor();
        Usuario usuario = buildUsuario();
        Producto producto = buildProducto(10L, 3);
        Lote lote = buildLote(1L, producto, 3);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        RegistrarDevolucionRequest request = new RegistrarDevolucionRequest(
                1L, "admin", null,
                List.of(new DetalleDevolucionRequest(10L, 1L, 10)));

        assertThrows(InsufficientStockException.class, () -> devolucionService.registrar(request));
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    void registrar_cuandoLoteNoPertenece_debeLanzarBusinessException() {
        Proveedor proveedor = buildProveedor();
        Usuario usuario = buildUsuario();
        Producto productoReal = buildProducto(10L, 50);
        Producto otroProducto = buildProducto(99L, 50);
        Lote lote = buildLote(1L, otroProducto, 20);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(productoReal));

        RegistrarDevolucionRequest request = new RegistrarDevolucionRequest(
                1L, "admin", null,
                List.of(new DetalleDevolucionRequest(10L, 1L, 5)));

        assertThrows(BusinessException.class, () -> devolucionService.registrar(request));
    }

    @Test
    void registrar_cuandoProveedorNoExiste_debeLanzarResourceNotFoundException() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        RegistrarDevolucionRequest request = new RegistrarDevolucionRequest(
                99L, "admin", null,
                List.of(new DetalleDevolucionRequest(1L, 1L, 1)));

        assertThrows(ResourceNotFoundException.class, () -> devolucionService.registrar(request));
    }

    // ── listar ───────────────────────────────────────────────────────────────

    @Test
    void listar_debeRetornarListaDeRespuestas() {
        Proveedor proveedor = buildProveedor();
        DevolucionProveedor devolucion = buildDevolucionGuardada(proveedor);

        when(devolucionRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(devolucion));

        List<DevolucionResponse> resultado = devolucionService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Proveedor ABC", resultado.getFirst().nombreProveedor());
    }

    // ── eliminar ─────────────────────────────────────────────────────────────

    @Test
    void eliminar_debeReponerStockDeLoteYProductoYGuardarMovimiento() {
        Proveedor proveedor = buildProveedor();
        Usuario usuario = buildUsuario();
        Producto producto = buildProducto(10L, 45);
        Lote lote = buildLote(1L, producto, 15);

        DetalleDevolucionProveedor detalle = new DetalleDevolucionProveedor();
        detalle.setProducto(producto);
        detalle.setLote(lote);
        detalle.setNombreProducto(producto.getNombre());
        detalle.setNumeroLote(lote.getNumeroLote());
        detalle.setCantidad(5);

        DevolucionProveedor devolucion = buildDevolucionGuardada(proveedor);
        devolucion.setIdDevolucion(10L);
        devolucion.setDetalles(List.of(detalle));

        when(devolucionRepository.findWithDetallesById(10L)).thenReturn(Optional.of(devolucion));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(movimientoRepository.saveAll(any())).thenReturn(List.of());

        devolucionService.eliminar(10L, "admin");

        assertEquals(50, producto.getStockActual());
        assertEquals(20, lote.getCantidad());
        verify(devolucionRepository).delete(devolucion);
    }

    @Test
    void eliminar_cuandoNoExiste_debeLanzarResourceNotFoundException() {
        when(devolucionRepository.findWithDetallesById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> devolucionService.eliminar(99L, "admin"));
        verify(devolucionRepository, never()).delete(any());
    }

    // ── actualizar ───────────────────────────────────────────────────────────

    @Test
    void actualizar_debeModificarMotivoYObervaciones() {
        Proveedor proveedor = buildProveedor();
        DevolucionProveedor devolucion = buildDevolucionGuardada(proveedor);
        devolucion.setMotivo("Motivo antiguo");

        when(devolucionRepository.findWithDetallesById(10L)).thenReturn(Optional.of(devolucion));
        when(devolucionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DevolucionResponse response = devolucionService.actualizar(10L,
                new ActualizarDevolucionRequest("Motivo nuevo", "Observación nueva"));

        assertEquals("Motivo nuevo", response.motivo());
        assertEquals("Motivo nuevo", devolucion.getMotivo());
        assertEquals("Observación nueva", devolucion.getObservaciones());
    }

    @Test
    void actualizar_cuandoNoExiste_debeLanzarResourceNotFoundException() {
        when(devolucionRepository.findWithDetallesById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> devolucionService.actualizar(99L, new ActualizarDevolucionRequest("x", null)));
        verify(devolucionRepository, never()).save(any());
    }
}
