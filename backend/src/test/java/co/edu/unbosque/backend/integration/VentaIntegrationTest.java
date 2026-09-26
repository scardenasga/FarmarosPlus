package co.edu.unbosque.backend.integration;

import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.model.request.AnularVentaRequest;
import co.edu.unbosque.backend.model.request.CrearVentaRequest;
import co.edu.unbosque.backend.model.request.PagoVentaRequest;
import co.edu.unbosque.backend.model.request.VentaDetalleRequest;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import co.edu.unbosque.backend.repository.VentaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas funcionales integradas del flujo de ventas (SCRUM-18, SCRUM-22 y SCRUM-24).
 *
 * <p>A diferencia de {@code VentaServiceTest} y {@code VentaControllerTest}, esta clase
 * no sustituye servicios ni repositorios por mocks: cada caso atraviesa HTTP, controlador,
 * servicio, JPA y una base SQLite exclusiva bajo {@code target/}.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:venta-integration-test;DB_CLOSE_DELAY=-1;MODE=LEGACY",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.main.lazy-initialization=false"
})
@AutoConfigureMockMvc
class VentaIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private MovimientoInventarioRepository movimientoInventarioRepository;

    private Usuario vendedor;
    private Usuario administrador;
    private Producto producto;
    private Lote lote;

    @BeforeEach
    void prepararDatos() {
        limpiarDatos();
        vendedor = guardarUsuario("vendedor-prueba", "VENDEDOR");
        administrador = guardarUsuario("admin-prueba", "ADMIN");

        producto = new Producto();
        producto.setNombre("Producto de prueba venta");
        producto.setCodigoBarras("TEST-VENTA-001");
        producto.setStockActual(10);
        producto.setStockMinimo(2);
        producto.setCosto(2500.0);
        producto.setPrecioVenta(5000.0);
        producto.setPorcentajeIva(0.0);
        producto.setEstado("ACTIVO");
        producto = productoRepository.saveAndFlush(producto);

        lote = new Lote();
        lote.setNumeroLote("LOTE-VENTA-PRUEBA");
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setCantidad(10);
        lote.setProducto(producto);
        lote = loteRepository.saveAndFlush(lote);
    }

    @AfterEach
    void limpiarBaseDePrueba() {
        limpiarDatos();
    }

    @Test
    void registrarVenta_debePersistirVentaDescontarInventarioYRegistrarMovimiento_SCRUM18() throws Exception {
        CrearVentaRequest solicitud = ventaConCantidadYPagos(
                2,
                List.of(new PagoVentaRequest("EFECTIVO", 6000.0), new PagoVentaRequest("TARJETA", 4000.0))
        );

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonVenta(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.subtotal").value(10000.0))
                .andExpect(jsonPath("$.total").value(10000.0))
                .andExpect(jsonPath("$.pagos.length()").value(2));

        assertThat(productoRepository.findById(producto.getUniqueID()).orElseThrow().getStockActual()).isEqualTo(8);
        assertThat(loteRepository.findById(lote.getIdLote()).orElseThrow().getCantidad()).isEqualTo(8);
        assertThat(ventaRepository.count()).isEqualTo(1);
        assertThat(movimientoInventarioRepository.findByProducto_UniqueIDOrderByFechaMovimientoDesc(producto.getUniqueID()))
                .singleElement()
                .satisfies(movimiento -> {
                    assertThat(movimiento.getTipoMovimiento()).isEqualTo("VENTA");
                    assertThat(movimiento.getDiferencia()).isEqualTo(-2);
                    assertThat(movimiento.getReferenciaDocumento()).startsWith("VENTA-");
                });
    }

    @Test
    void registrarVenta_conStockInsuficienteNoDebePersistirNiModificarInventario_SCRUM18() throws Exception {
        CrearVentaRequest solicitud = ventaConCantidadYPagos(11, List.of(new PagoVentaRequest("EFECTIVO", 55000.0)));

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonVenta(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));

        assertThat(productoRepository.findById(producto.getUniqueID()).orElseThrow().getStockActual()).isEqualTo(10);
        assertThat(loteRepository.findById(lote.getIdLote()).orElseThrow().getCantidad()).isEqualTo(10);
        assertThat(ventaRepository.count()).isZero();
        assertThat(movimientoInventarioRepository.count()).isZero();
    }

    @Test
    void registrarVenta_conPagoInsuficienteNoDebePersistirNiModificarInventario_SCRUM22() throws Exception {
        CrearVentaRequest solicitud = ventaConCantidadYPagos(2, List.of(new PagoVentaRequest("EFECTIVO", 9999.0)));

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonVenta(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("insuficiente")));

        assertThat(productoRepository.findById(producto.getUniqueID()).orElseThrow().getStockActual()).isEqualTo(10);
        assertThat(loteRepository.findById(lote.getIdLote()).orElseThrow().getCantidad()).isEqualTo(10);
        assertThat(ventaRepository.count()).isZero();
        assertThat(movimientoInventarioRepository.count()).isZero();
    }

    @Test
    void anularVenta_debeRestaurarInventarioYRegistrarDevolucion_SCRUM24() throws Exception {
        Long ventaId = registrarVenta(2).getIdVenta();
        AnularVentaRequest solicitud = new AnularVentaRequest(
                "Error de digitacion", administrador.getUsername(), administrador.getIdUsuario(), true
        );

        mockMvc.perform(patch("/api/ventas/{id}/anular", ventaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAnulacion(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"))
                .andExpect(jsonPath("$.motivoAnulacion").value("Error de digitacion"));

        assertThat(productoRepository.findById(producto.getUniqueID()).orElseThrow().getStockActual()).isEqualTo(10);
        assertThat(loteRepository.findById(lote.getIdLote()).orElseThrow().getCantidad()).isEqualTo(10);
        assertThat(movimientoInventarioRepository.findByProducto_UniqueIDOrderByFechaMovimientoDesc(producto.getUniqueID()))
                .extracting(movimiento -> movimiento.getTipoMovimiento())
                .containsExactlyInAnyOrder("VENTA", "DEVOLUCION");
    }

    private Venta registrarVenta(int cantidad) throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonVenta(ventaConCantidadYPagos(
                                cantidad, List.of(new PagoVentaRequest("EFECTIVO", cantidad * 5000.0))
                        ))))
                .andExpect(status().isCreated());
        return ventaRepository.findAll().getFirst();
    }

    private CrearVentaRequest ventaConCantidadYPagos(int cantidad, List<PagoVentaRequest> pagos) {
        return new CrearVentaRequest(
                vendedor.getIdUsuario(),
                0.0,
                List.of(new VentaDetalleRequest(producto.getUniqueID(), lote.getIdLote(), cantidad, null)),
                pagos
        );
    }

    private String jsonVenta(CrearVentaRequest solicitud) {
        VentaDetalleRequest detalle = solicitud.detalles().getFirst();
        String pagos = solicitud.pagos().stream()
                .map(pago -> "{\"tipo\":\"%s\",\"monto\":%s}".formatted(pago.tipo(), pago.monto()))
                .collect(java.util.stream.Collectors.joining(","));
        return "{\"usuarioId\":%d,\"descuento\":%s,\"detalles\":[{\"productoId\":%d,\"loteId\":%d,\"cantidad\":%d}],\"pagos\":[%s]}"
                .formatted(solicitud.usuarioId(), solicitud.descuento(), detalle.productoId(), detalle.loteId(), detalle.cantidad(), pagos);
    }

    private String jsonAnulacion(AnularVentaRequest solicitud) {
        return "{\"motivoAnulacion\":\"%s\",\"usuarioResponsable\":\"%s\",\"usuarioId\":%d,\"confirmacion\":%s}"
                .formatted(solicitud.motivoAnulacion(), solicitud.usuarioResponsable(), solicitud.usuarioId(), solicitud.confirmacion());
    }

    private Usuario guardarUsuario(String username, String rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash("hash-de-prueba");
        usuario.setNombreCompleto(username);
        usuario.setRol(rol);
        usuario.setEstado("ACTIVO");
        return usuarioRepository.saveAndFlush(usuario);
    }

    private void limpiarDatos() {
        movimientoInventarioRepository.deleteAllInBatch();
        ventaRepository.deleteAll();
        loteRepository.deleteAllInBatch();
        productoRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();
    }

}
