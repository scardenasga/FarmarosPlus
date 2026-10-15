package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.*;
import co.edu.unbosque.backend.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void obtenerDashboard_debeRetornar200ConDatosCompletos() throws Exception {
        DashboardResumenResponse resumen = new DashboardResumenResponse(
                50000.0, 5L, 200000.0, 20L, 3L, 2L, 120000.0, 80000.0, 40.0
        );

        DashboardResponse response = new DashboardResponse(
                resumen,
                List.of(new VentaPorDiaResponse("2026-08-20", 50000.0, 5L)),
                List.of(),
                List.of(new InventarioCategoriaResponse("Analgésicos", 100L, 5L)),
                List.of(new ProductoDestacadoResponse(1L, "Amoxicilina", 20L, 100000.0)),
                List.of(new ProductoStockBajoResponse(1L, "Amoxicilina", 2, 10)),
                List.of(new ComparativaMensualResponse("Ago", 200000.0, 120000.0)),
                LocalDateTime.now()
        );

        when(dashboardService.obtenerDashboard(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.ventasDelDia").value(50000.0))
                .andExpect(jsonPath("$.resumen.comprasDelMes").value(120000.0))
                .andExpect(jsonPath("$.resumen.gananciaDelMes").value(80000.0))
                .andExpect(jsonPath("$.comparativaMensual[0].mes").value("Ago"));
    }

    @Test
    void obtenerAnalitica_debeRetornar200ConAnaliticaResponse() throws Exception {
        AnaliticaResumenResponse resumen = new AnaliticaResumenResponse(
                384500.0, 14.2, 142L, 18.0, 34.8, "Analgésicos", 41.0, "Amoxicilina", 48L, 12400.0, 3L
        );

        AnaliticaDashboardResponse response = new AnaliticaDashboardResponse(
                resumen,
                List.of(new VentaPorDiaResponse("2026-08-20", 82000.0, 10L)),
                List.of(),
                List.of(new InventarioCategoriaResponse("Analgésicos", 163000L, 12L)),
                List.of(new ProductoAnaliticaResponse(1L, "Amoxicilina", "Antibióticos", 48L, 120000.0, 42.5, 2, 10, "critical")),
                new AnaliticaInsightsResponse("Amoxicilina", "Lunes", "3 productos"),
                LocalDateTime.now()
        );

        when(dashboardService.obtenerAnalitica(any(), any(), any(), any(), anyBoolean())).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/analitica?comparar=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.ventasFiltradas").value(384500.0))
                .andExpect(jsonPath("$.resumen.categoriaLider").value("Analgésicos"))
                .andExpect(jsonPath("$.productos[0].nombre").value("Amoxicilina"));
    }
}
