package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.DevolucionClienteResponse;
import co.edu.unbosque.backend.service.DevolucionClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DevolucionClienteController.class)
class DevolucionClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DevolucionClienteService devolucionClienteService;

    @Test
    void registrar_debeRetornar201() throws Exception {
        DevolucionClienteResponse response = new DevolucionClienteResponse(
                1L, 5L, "Juan Perez", "100200300", "admin",
                "Producto no cumplio expectativa", LocalDateTime.now(), List.of());

        when(devolucionClienteService.registrar(any())).thenReturn(response);

        mockMvc.perform(post("/api/devoluciones-clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idVenta": 5,
                                  "usuarioResponsable": "admin",
                                  "nombreCliente": "Juan Perez",
                                  "documentoCliente": "100200300",
                                  "motivo": "Producto no cumplio expectativa",
                                  "detalles": [
                                    {"idDetalleVenta": 12, "cantidad": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreCliente").value("Juan Perez"));
    }

    @Test
    void registrar_sinVenta_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/devoluciones-clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioResponsable": "admin",
                                  "detalles": [
                                    {"idDetalleVenta": 12, "cantidad": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_debeRetornar200() throws Exception {
        when(devolucionClienteService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/devoluciones-clientes"))
                .andExpect(status().isOk());
    }
}
