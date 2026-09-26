package co.edu.unbosque.backend.integration;

import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.AjusteInventarioRequest;
import co.edu.unbosque.backend.model.request.IngresoLoteRequest;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas funcionales integradas de ingreso y ajuste de inventario (SCRUM-27 y SCRUM-28). */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventario-integration-test;DB_CLOSE_DELAY=-1;MODE=LEGACY",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.main.lazy-initialization=false"
})
@AutoConfigureMockMvc
class InventarioIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private MovimientoInventarioRepository movimientoInventarioRepository;

    private Producto producto;

    @BeforeEach
    void prepararDatos() {
        limpiarDatos();
        producto = new Producto();
        producto.setNombre("Producto inventario prueba");
        producto.setCodigoBarras("TEST-INVENTARIO-001");
        producto.setStockActual(0);
        producto.setStockMinimo(2);
        producto.setCosto(1000.0);
        producto.setPrecioVenta(2000.0);
        producto.setEstado("ACTIVO");
        producto = productoRepository.saveAndFlush(producto);
    }

    @AfterEach
    void limpiarBaseDePrueba() {
        limpiarDatos();
    }

    @Test
    void ingresarLoteDebeActualizarProductoYLlevarMovimiento_SCRUM27_SCRUM28() throws Exception {
        IngresoLoteRequest solicitud = new IngresoLoteRequest(
                producto.getUniqueID(), "LOTE-INGRESO-PRUEBA", LocalDate.now().plusYears(1), 15,
                "Ingreso de prueba", "COMPRA-PRUEBA-1"
        );

        String respuesta = mockMvc.perform(post("/api/inventario/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonIngresoLote(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidad").value(15))
                .andExpect(jsonPath("$.producto.id").value(producto.getUniqueID()))
                .andReturn().getResponse().getContentAsString();

        long loteId = idDeRespuesta(respuesta);
        assertThat(productoRepository.findById(producto.getUniqueID()).orElseThrow().getStockActual()).isEqualTo(15);
        assertThat(loteRepository.findById(loteId).orElseThrow().getCantidad()).isEqualTo(15);
        assertThat(movimientoInventarioRepository.findByProducto_UniqueIDOrderByFechaMovimientoDesc(producto.getUniqueID()))
                .singleElement()
                .satisfies(movimiento -> {
                    assertThat(movimiento.getTipoMovimiento()).isEqualTo("COMPRA");
                    assertThat(movimiento.getDiferencia()).isEqualTo(15);
                });
    }

    @Test
    void ajusteQueDejaLoteNegativoDebeRechazarseSinCambios_SCRUM27() throws Exception {
        long loteId = crearLoteConCantidad(5);
        AjusteInventarioRequest solicitud = new AjusteInventarioRequest(
                loteId, -6, "MERMA", "Prueba de límite", "AJUSTE-PRUEBA-1"
        );

        mockMvc.perform(patch("/api/inventario/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAjuste(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("stock suficiente")));

        assertThat(productoRepository.findById(producto.getUniqueID()).orElseThrow().getStockActual()).isEqualTo(5);
        assertThat(loteRepository.findById(loteId).orElseThrow().getCantidad()).isEqualTo(5);
        assertThat(movimientoInventarioRepository.count()).isEqualTo(1);
    }

    private long crearLoteConCantidad(int cantidad) throws Exception {
        IngresoLoteRequest solicitud = new IngresoLoteRequest(
                producto.getUniqueID(), "LOTE-AJUSTE-PRUEBA", LocalDate.now().plusYears(1), cantidad,
                "Datos de preparación", "COMPRA-PRUEBA-2"
        );
        String respuesta = mockMvc.perform(post("/api/inventario/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonIngresoLote(solicitud)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDeRespuesta(respuesta);
    }

    private String jsonIngresoLote(IngresoLoteRequest solicitud) {
        return "{\"productoId\":%d,\"numeroLote\":\"%s\",\"fechaVencimiento\":\"%s\",\"cantidad\":%d,\"motivo\":\"%s\",\"referenciaDocumento\":\"%s\"}"
                .formatted(solicitud.productoId(), solicitud.numeroLote(), solicitud.fechaVencimiento(), solicitud.cantidad(),
                        solicitud.motivo(), solicitud.referenciaDocumento());
    }

    private String jsonAjuste(AjusteInventarioRequest solicitud) {
        return "{\"loteId\":%d,\"diferencia\":%d,\"tipoMovimiento\":\"%s\",\"motivo\":\"%s\",\"referenciaDocumento\":\"%s\"}"
                .formatted(solicitud.loteId(), solicitud.diferencia(), solicitud.tipoMovimiento(), solicitud.motivo(), solicitud.referenciaDocumento());
    }

    private long idDeRespuesta(String respuesta) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)").matcher(respuesta);
        assertThat(matcher.find()).isTrue();
        return Long.parseLong(matcher.group(1));
    }

    private void limpiarDatos() {
        movimientoInventarioRepository.deleteAllInBatch();
        loteRepository.deleteAllInBatch();
        productoRepository.deleteAllInBatch();
    }

}
