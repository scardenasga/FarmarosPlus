package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.CompraResponse;
import co.edu.unbosque.backend.model.response.DetalleCompraResponse;
import co.edu.unbosque.backend.service.CompraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompraController.class)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompraService compraService;

    private CompraResponse buildResponse() {
        return new CompraResponse(1L, 1L, "Laboratorio XYZ", "admin",
                "FAC-001", null, LocalDateTime.now(), 50000.0,
                List.of(new DetalleCompraResponse(10L, "Amoxicilina", 10, 5000.0, 50000.0)));
    }

    @Test
    void registrar_debeRetornar201ConTotal() throws Exception {
        when(compraService.registrar(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idProveedor": 1,
                                  "usuarioResponsable": "admin",
                                  "detalles": [
                                    {"idProducto": 10, "cantidad": 10, "precioUnitario": 5000.0}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(50000.0))
                .andExpect(jsonPath("$.nombreProveedor").value("Laboratorio XYZ"))
                .andExpect(jsonPath("$.detalles[0].nombreProducto").value("Amoxicilina"));
    }

    @Test
    void registrar_sinProveedor_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioResponsable": "admin",
                                  "detalles": [
                                    {"idProducto": 10, "cantidad": 10, "precioUnitario": 5000.0}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_sinDetalles_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/compras")
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
    void listar_sinFiltro_debeRetornar200() throws Exception {
        when(compraService.listar(null)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreProveedor").value("Laboratorio XYZ"));
    }

    @Test
    void listar_conFiltroProveedor_debeRetornar200() throws Exception {
        when(compraService.listar(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/compras?idProveedor=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(50000.0));
    }
}
