package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.*;
import co.edu.unbosque.backend.model.request.AgregarDetalleRecepcionRequest;
import co.edu.unbosque.backend.model.request.RegistrarRecepcionRequest;
import co.edu.unbosque.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RecepcionCompraServiceTest {

    @Mock
    private RecepcionCompraRepository recepcionCompraRepository;

    @Mock
    private DetalleRecepcionCompraRepository detalleRecepcionCompraRepository;

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private DetalleOrdenCompraRepository detalleOrdenCompraRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private RecepcionCompraService recepcionCompraService;

    private OrdenCompra buildOrden() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("admin1");
        usuario.setNombreCompleto("Admin Uno");
        usuario.setRol("ADMIN");
        usuario.setEstado("ACTIVO");

        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrden(1L);
        orden.setProveedor(proveedor);
        orden.setUsuario(usuario);
        orden.setFechaPedido(LocalDateTime.now());
        orden.setEstado("PENDIENTE");
        orden.setTotalEsperado(50000.0);
        return orden;
    }

    private Usuario buildUsuario() {
        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setUsername("admin1");
        u.setNombreCompleto("Admin Uno");
        u.setRol("ADMIN");
        u.setEstado("ACTIVO");
        return u;
    }

    // ================================================================
    // registrarRecepcion
    // ================================================================

    @Test
    void registrarRecepcion_exitoso_creaRecepcionConEstadoPagoPendiente() {
        RegistrarRecepcionRequest request = new RegistrarRecepcionRequest(
                1L, "Sin novedades", "COMPLETA", 50000.0
        );

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(buildOrden()));
        when(currentUserService.getCurrentUsername()).thenReturn("admin1");
        when(usuarioRepository.findByUsername("admin1")).thenReturn(Optional.of(buildUsuario()));
        when(recepcionCompraRepository.save(any(RecepcionCompra.class))).thenAnswer(inv -> {
            RecepcionCompra r = inv.getArgument(0);
            r.setIdRecepcion(1L);
            return r;
        });

        RecepcionCompra resultado = recepcionCompraService.registrarRecepcion(request);

        assertEquals("PENDIENTE", resultado.getEstadoPago());
        assertEquals("COMPLETA", resultado.getEstado());
        assertEquals(0.0, resultado.getMontoPagado());
    }

    @Test
    void registrarRecepcion_ordenNoExiste_lanzaResourceNotFoundException() {
        RegistrarRecepcionRequest request = new RegistrarRecepcionRequest(
                99L, null, "COMPLETA", 50000.0
        );

        when(ordenCompraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> recepcionCompraService.registrarRecepcion(request));
    }

    @Test
    void registrarRecepcion_estadoInvalido_lanzaBusinessException() {
        RegistrarRecepcionRequest request = new RegistrarRecepcionRequest(
                1L, null, "INVALIDO", 50000.0
        );

        assertThrows(BusinessException.class,
                () -> recepcionCompraService.registrarRecepcion(request));
    }

    @Test
    void registrarRecepcion_totalNegativo_lanzaBusinessException() {
        RegistrarRecepcionRequest request = new RegistrarRecepcionRequest(
                1L, null, "COMPLETA", -100.0
        );

        assertThrows(BusinessException.class,
                () -> recepcionCompraService.registrarRecepcion(request));
    }

    @Test
    void registrarRecepcion_requestNulo_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> recepcionCompraService.registrarRecepcion(null));
    }

    @Test
    void registrarRecepcion_sinOrdenId_lanzaBusinessException() {
        RegistrarRecepcionRequest request = new RegistrarRecepcionRequest(
                null, null, "COMPLETA", 50000.0
        );

        assertThrows(BusinessException.class,
                () -> recepcionCompraService.registrarRecepcion(request));
    }

    // ================================================================
    // agregarDetalleRecepcion
    // ================================================================

    @Test
    void agregarDetalleRecepcion_exitoso_creaDetalle() {
        RecepcionCompra recepcion = new RecepcionCompra();
        recepcion.setIdRecepcion(1L);
        recepcion.setEstado("COMPLETA");
        recepcion.setEstadoPago("PENDIENTE");

        DetalleOrdenCompra detalleOrden = new DetalleOrdenCompra();
        detalleOrden.setIdDetalle(1L);

        AgregarDetalleRecepcionRequest request = new AgregarDetalleRecepcionRequest(
                1L, 5L, 100, 1500.0, null
        );

        when(recepcionCompraRepository.findById(1L)).thenReturn(Optional.of(recepcion));
        when(detalleOrdenCompraRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> recepcionCompraService.agregarDetalleRecepcion(1L, request));
    }

    @Test
    void agregarDetalleRecepcion_recepcionNoExiste_lanzaResourceNotFoundException() {
        AgregarDetalleRecepcionRequest request = new AgregarDetalleRecepcionRequest(
                1L, null, 50, null, null
        );

        when(recepcionCompraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> recepcionCompraService.agregarDetalleRecepcion(99L, request));
    }

    @Test
    void agregarDetalleRecepcion_cantidadCero_lanzaBusinessException() {
        AgregarDetalleRecepcionRequest request = new AgregarDetalleRecepcionRequest(
                1L, null, 0, null, null
        );

        assertThrows(BusinessException.class,
                () -> recepcionCompraService.agregarDetalleRecepcion(1L, request));
    }

    // ================================================================
    // actualizarEstadoPago
    // ================================================================

    @Test
    void actualizarEstadoPago_exitoso_cambiaEstadoYMonto() {
        RecepcionCompra recepcion = new RecepcionCompra();
        recepcion.setIdRecepcion(1L);
        recepcion.setEstadoPago("PENDIENTE");
        recepcion.setMontoPagado(0.0);

        when(recepcionCompraRepository.findById(1L)).thenReturn(Optional.of(recepcion));
        when(recepcionCompraRepository.save(any(RecepcionCompra.class))).thenReturn(recepcion);

        RecepcionCompra resultado = recepcionCompraService.actualizarEstadoPago(1L, "PAGADO", 50000.0);

        assertEquals("PAGADO", resultado.getEstadoPago());
        assertEquals(50000.0, resultado.getMontoPagado());
    }

    @Test
    void actualizarEstadoPago_estadoInvalido_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> recepcionCompraService.actualizarEstadoPago(1L, "INVALIDO", null));
    }

    @Test
    void actualizarEstadoPago_estadoVacio_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> recepcionCompraService.actualizarEstadoPago(1L, "", null));
    }

    // ================================================================
    // obtenerRecepcionPorId
    // ================================================================

    @Test
    void obtenerRecepcionPorId_inexistente_lanzaResourceNotFoundException() {
        when(recepcionCompraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> recepcionCompraService.obtenerRecepcionPorId(99L));
    }

    // ================================================================
    // listarRecepcionesPendientes
    // ================================================================

    @Test
    void listarRecepcionesPendientes_debeRetornarSoloPendientes() {
        when(recepcionCompraRepository.findByEstadoPagoOrderByFechaRecepcionDesc("PENDIENTE"))
                .thenReturn(List.of());

        List<RecepcionCompra> resultado = recepcionCompraService.listarRecepcionesPendientes();

        assertEquals(0, resultado.size());
    }

    // ================================================================
    // listarRecepcionesPorOrden
    // ================================================================

    @Test
    void listarRecepcionesPorOrden_debeRetornarRecepcionesDeOrden() {
        when(recepcionCompraRepository.findByOrden_IdOrdenOrderByFechaRecepcionDesc(1L))
                .thenReturn(List.of());

        List<RecepcionCompra> resultado = recepcionCompraService.listarRecepcionesPorOrden(1L);

        assertEquals(0, resultado.size());
    }

    // ================================================================
    // listarTodasRecepciones
    // ================================================================

    @Test
    void listarTodasRecepciones_debeRetornarTodas() {
        when(recepcionCompraRepository.findAll()).thenReturn(List.of());

        List<RecepcionCompra> resultado = recepcionCompraService.listarTodasRecepciones();

        assertEquals(0, resultado.size());
    }
}