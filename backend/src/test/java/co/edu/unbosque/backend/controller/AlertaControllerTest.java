package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.AlertaResponse;
import co.edu.unbosque.backend.service.AlertaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlertaController.class)
class AlertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertaService alertaService;

    @Test
    void generarAlertas_debeRetornar200ConLista() throws Exception {
        AlertaResponse alerta = new AlertaResponse(1L, "STOCK_MINIMO",
                "Stock bajo: Amoxicilina", "Stock actual: 2", false, LocalDateTime.now());

        when(alertaService.generarAlertas()).thenReturn(List.of(alerta));

        mockMvc.perform(post("/api/alertas/generar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("STOCK_MINIMO"))
                .andExpect(jsonPath("$[0].leida").value(false));
    }

    @Test
    void listarAlertas_sinFiltro_debeRetornar200() throws Exception {
        when(alertaService.listarAlertas(false)).thenReturn(List.of());

        mockMvc.perform(get("/api/alertas"))
                .andExpect(status().isOk());
    }

    @Test
    void listarAlertas_conFiltroNoLeidas_debeRetornar200() throws Exception {
        AlertaResponse alerta = new AlertaResponse(2L, "PROXIMO_VENCIMIENTO",
                "Vencimiento próximo: Ibuprofeno", "Lote L001 vence el 2025-06-01", false, LocalDateTime.now());

        when(alertaService.listarAlertas(true)).thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/alertas?soloNoLeidas=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("PROXIMO_VENCIMIENTO"));
    }

    @Test
    void marcarComoLeida_debeRetornar204() throws Exception {
        doNothing().when(alertaService).marcarComoLeida(1L);

        mockMvc.perform(patch("/api/alertas/1/leer"))
                .andExpect(status().isNoContent());
    }

    @Test
    void marcarTodasComoLeidas_debeRetornar204() throws Exception {
        doNothing().when(alertaService).marcarTodasComoLeidas();

        mockMvc.perform(patch("/api/alertas/leer-todas"))
                .andExpect(status().isNoContent());
    }
}
