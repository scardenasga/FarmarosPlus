/*package co.edu.unbosque.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarVenta_debeRetornar201() throws Exception {

        Map<String, Object> request = Map.of(
                "usuarioId", 1,
                "descuento", 0.0,
                "detalles", List.of(
                        Map.of(
                                "productoId", 1,
                                "loteId", 1,
                                "cantidad", 2
                        )
                ),
                "pagos", List.of(
                        Map.of(
                                "tipo", "EFECTIVO",
                                "monto", 7000.0
                        )
                )
        );

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}*/