package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.DevolucionResponse;
import co.edu.unbosque.backend.service.DevolucionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DevolucionController.class)
class DevolucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DevolucionService devolucionService;

    @Test
    void registrar_debeRetornar201() throws Exception {
        DevolucionResponse response = new DevolucionResponse(
                1L, 1L, "Proveedor ABC", "admin", "Producto vencido",
                LocalDateTime.now(), List.of());

        when(devolucionService.registrar(any())).thenReturn(response);

        mockMvc.perform(post("/api/devoluciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idProveedor": 1,
                                  "usuarioResponsable": "admin",
                                  "detalles": [
                                    {"idProducto": 10, "idLote": 1, "cantidad": 5}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreProveedor").value("Proveedor ABC"));
    }

    @Test
    void registrar_sinProveedor_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/devoluciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioResponsable": "admin",
                                  "detalles": [
                                    {"idProducto": 10, "idLote": 1, "cantidad": 5}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_sinDetalles_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/devoluciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idProveedor": 1,
                                  "usuarioResponsable": "admin",
                                  "detalles": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_debeRetornar200() throws Exception {
        when(devolucionService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/devoluciones"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizar_debeRetornar200() throws Exception {
        DevolucionResponse response = new DevolucionResponse(
                1L, 1L, "Proveedor ABC", "admin", "Motivo nuevo",
                LocalDateTime.now(), List.of());

        when(devolucionService.actualizar(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/devoluciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "motivo": "Motivo nuevo",
                                  "observaciones": "Nota"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("Motivo nuevo"));
    }

    @Test
    void eliminar_debeRetornar204() throws Exception {
        doNothing().when(devolucionService).eliminar(eq(1L), any());

        mockMvc.perform(delete("/api/devoluciones/1")
                        .param("usuarioResponsable", "admin"))
                .andExpect(status().isNoContent());

        verify(devolucionService).eliminar(1L, "admin");
    }
}
