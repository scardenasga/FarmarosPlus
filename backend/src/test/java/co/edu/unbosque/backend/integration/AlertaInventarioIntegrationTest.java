package co.edu.unbosque.backend.integration;

import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.repository.AlertaGeneralRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.MovimientoInventarioRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba funcional integrada de alertas de inventario (SCRUM-31 y SCRUM-32).
 * Recorre API, servicio, repositorios y SQLite sin usar mocks.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:alerta-integration-test;DB_CLOSE_DELAY=-1;MODE=LEGACY",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.main.lazy-initialization=false"
})
@AutoConfigureMockMvc
class AlertaInventarioIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AlertaGeneralRepository alertaRepository;
    @Autowired private MovimientoInventarioRepository movimientoInventarioRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private ProductoRepository productoRepository;

    @BeforeEach
    void prepararDatos() {
        limpiarDatos();
        Producto producto = new Producto();
        producto.setNombre("Producto con alerta");
        producto.setCodigoBarras("TEST-ALERTA-001");
        producto.setStockActual(1);
        producto.setStockMinimo(5);
        producto.setCosto(1000.0);
        producto.setPrecioVenta(2000.0);
        producto.setEstado("ACTIVO");
        producto = productoRepository.saveAndFlush(producto);

        Lote lote = new Lote();
        lote.setNumeroLote("LOTE-ALERTA-PRUEBA");
        lote.setProducto(producto);
        lote.setCantidad(1);
        lote.setFechaVencimiento(LocalDate.now().plusDays(10));
        loteRepository.saveAndFlush(lote);
    }

    @AfterEach
    void limpiarBaseDePrueba() {
        limpiarDatos();
    }

    @Test
    void generarYMarcarAlertaDebeMantenerTrazabilidadYEvitarDuplicados_SCRUM31_SCRUM32() throws Exception {
        String respuesta = mockMvc.perform(post("/api/alertas/generar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.tipo == 'STOCK_MINIMO')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.tipo == 'PROXIMO_VENCIMIENTO')]").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)").matcher(respuesta);
            assertThat(matcher.find()).isTrue();
            long alertaId = Long.parseLong(matcher.group(1));

        mockMvc.perform(post("/api/alertas/generar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        assertThat(alertaRepository.count()).isEqualTo(2);

        mockMvc.perform(patch("/api/alertas/{id}/leer", alertaId))
                .andExpect(status().isNoContent());

        assertThat(alertaRepository.findById(alertaId).orElseThrow().isLeida()).isTrue();
        assertThat(alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()).hasSize(1);
    }

    private void limpiarDatos() {
        alertaRepository.deleteAllInBatch();
        movimientoInventarioRepository.deleteAllInBatch();
        loteRepository.deleteAllInBatch();
        productoRepository.deleteAllInBatch();
    }

}
