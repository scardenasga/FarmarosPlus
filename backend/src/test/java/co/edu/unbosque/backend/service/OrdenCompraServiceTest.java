package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.*;
import co.edu.unbosque.backend.model.request.AgregarDetalleOrdenRequest;
import co.edu.unbosque.backend.model.request.CrearOrdenCompraRequest;
import co.edu.unbosque.backend.repository.DetalleOrdenCompraRepository;
import co.edu.unbosque.backend.repository.OrdenCompraRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private DetalleOrdenCompraRepository detalleOrdenCompraRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private OrdenCompraService ordenCompraService;

    private Proveedor buildProveedor() {
        Proveedor p = new Proveedor();
        p.setIdProveedor(1L);
        p.setNombre("Farmaceutica XYZ");
        p.setEstado("ACTIVO");
        return p;
    }

    private Usuario buildUsuario() {
        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setUsername("admin1");
        u.setRol("ADMIN");
        u.setEstado("ACTIVO");
        return u;
    }

    private OrdenCompra buildOrden() {
        OrdenCompra o = new OrdenCompra();
        o.setIdOrden(1L);
        o.setProveedor(buildProveedor());
        o.setUsuario(buildUsuario());
        o.setFechaPedido(LocalDateTime.now());
        o.setEstado("PENDIENTE");
        o.setTotalEsperado(0.0);
        return o;
    }

    // ================================================================
    // crearOrdenCompra
    // ================================================================

    @Test
    void crearOrdenCompra_exitoso_creaConEstadoPendiente() {
        CrearOrdenCompraRequest request = new CrearOrdenCompraRequest(
                1L, LocalDateTime.now().plusDays(7), 50000.0, "Urgente"
        );

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(buildProveedor()));
        when(currentUserService.getCurrentUsername()).thenReturn("admin1");
        when(usuarioRepository.findByUsername("admin1")).thenReturn(Optional.of(buildUsuario()));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> {
            OrdenCompra o = inv.getArgument(0);
            o.setIdOrden(1L);
            return o;
        });

        OrdenCompra resultado = ordenCompraService.crearOrdenCompra(request);

        ArgumentCaptor<OrdenCompra> captor = ArgumentCaptor.forClass(OrdenCompra.class);
        verify(ordenCompraRepository).save(captor.capture());
        OrdenCompra guardado = captor.getValue();

        assertEquals("PENDIENTE", guardado.getEstado());
        assertEquals("Urgente", guardado.getObservaciones());
    }

    @Test
    void crearOrdenCompra_proveedorNoExiste_lanzaResourceNotFoundException() {
        CrearOrdenCompraRequest request = new CrearOrdenCompraRequest(
                99L, null, null, null
        );

        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> ordenCompraService.crearOrdenCompra(request));
    }

    @Test
    void crearOrdenCompra_requestNulo_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> ordenCompraService.crearOrdenCompra(null));
    }

    @Test
    void crearOrdenCompra_sinProveedorId_lanzaBusinessException() {
        CrearOrdenCompraRequest request = new CrearOrdenCompraRequest(
                null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> ordenCompraService.crearOrdenCompra(request));
    }

    // ================================================================
    // agregarDetalleOrden
    // ================================================================

    @Test
    void agregarDetalleOrden_productoNoExiste_lanzaResourceNotFoundException() {
        OrdenCompra orden = buildOrden();
        orden.setTotalEsperado(0.0);

        AgregarDetalleOrdenRequest request = new AgregarDetalleOrdenRequest(
                5L, "Amoxicilina 500mg", "Caja x 20", 100, 1500.0
        );

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> ordenCompraService.agregarDetalleOrden(1L, request));
    }

    @Test
    void agregarDetalleOrden_exitoso_agregaDetalleYActualizaTotal() {
        OrdenCompra orden = buildOrden();
        orden.setTotalEsperado(0.0);

        Producto producto = new Producto();
        producto.setUniqueID(5L);
        producto.setNombre("Amoxicilina 500mg");

        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setIdDetalle(1L);
        detalle.setProducto(producto);
        detalle.setNombreProducto("Amoxicilina 500mg");
        detalle.setCantidadPedida(100);
        detalle.setPrecioUnitarioPactado(1500.0);

        AgregarDetalleOrdenRequest request = new AgregarDetalleOrdenRequest(
                5L, "Amoxicilina 500mg", "Caja x 20", 100, 1500.0
        );

        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(detalleOrdenCompraRepository.save(any(DetalleOrdenCompra.class))).thenAnswer(inv -> {
            DetalleOrdenCompra d = inv.getArgument(0);
            d.setIdDetalle(1L);
            return d;
        });
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenReturn(orden);

        DetalleOrdenCompra resultado = ordenCompraService.agregarDetalleOrden(1L, request);

        assertEquals(150000.0, orden.getTotalEsperado());
        assertEquals(100, resultado.getCantidadPedida());
    }

    @Test
    void agregarDetalleOrden_ordenNoPendiente_lanzaBusinessException() {
        OrdenCompra orden = buildOrden();
        orden.setEstado("CERRADA");

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));

        AgregarDetalleOrdenRequest request = new AgregarDetalleOrdenRequest(
                null, "Amoxicilina", null, 100, 1500.0
        );

        assertThrows(BusinessException.class,
                () -> ordenCompraService.agregarDetalleOrden(1L, request));
    }

    @Test
    void agregarDetalleOrden_cantidadCero_lanzaBusinessException() {
        AgregarDetalleOrdenRequest request = new AgregarDetalleOrdenRequest(
                null, "Amoxicilina", null, 0, 1500.0
        );

        assertThrows(BusinessException.class,
                () -> ordenCompraService.agregarDetalleOrden(1L, request));
    }

    @Test
    void agregarDetalleOrden_precioCero_lanzaBusinessException() {
        AgregarDetalleOrdenRequest request = new AgregarDetalleOrdenRequest(
                null, "Amoxicilina", null, 100, 0.0
        );

        assertThrows(BusinessException.class,
                () -> ordenCompraService.agregarDetalleOrden(1L, request));
    }

    // ================================================================
    // obtenerOrdenPorId
    // ================================================================

    @Test
    void obtenerOrdenPorId_inexistente_lanzaResourceNotFoundException() {
        when(ordenCompraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> ordenCompraService.obtenerOrdenPorId(99L));
    }

    // ================================================================
    // listarOrdenesPendientes
    // ================================================================

    @Test
    void listarOrdenesPendientes_debeRetornarSoloPendientes() {
        when(ordenCompraRepository.findByEstadoOrderByFechaPedidoDesc("PENDIENTE"))
                .thenReturn(List.of(buildOrden()));

        List<OrdenCompra> resultado = ordenCompraService.listarOrdenesPendientes();

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.getFirst().getEstado());
    }

    // ================================================================
    // listarOrdenesPorProveedor
    // ================================================================

    @Test
    void listarOrdenesPorProveedor_debeRetornarOrdenesDelProveedor() {
        when(ordenCompraRepository.findByProveedor_IdProveedorOrderByFechaPedidoDesc(1L))
                .thenReturn(List.of(buildOrden()));

        List<OrdenCompra> resultado = ordenCompraService.listarOrdenesPorProveedor(1L);

        assertEquals(1, resultado.size());
    }

    // ================================================================
    // listarTodasOrdenes
    // ================================================================

    @Test
    void listarTodasOrdenes_debeRetornarTodas() {
        when(ordenCompraRepository.findAll()).thenReturn(List.of());

        List<OrdenCompra> resultado = ordenCompraService.listarTodasOrdenes();

        assertEquals(0, resultado.size());
    }
}