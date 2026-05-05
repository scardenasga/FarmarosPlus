/*package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.model.request.AnularVentaRequest;
import co.edu.unbosque.backend.service.FacturaService;
import co.edu.unbosque.backend.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VentaController.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaService ventaService;

    @MockitoBean
    private FacturaService facturaService;

    @Test
    void registrarVenta_debeRetornar201() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("admin1");
        usuario.setNombreCompleto("Admin Uno");

        Venta venta = new Venta();
        venta.setIdVenta(1L);
        venta.setUsuario(usuario);
        venta.setEstado("COMPLETADA");
        venta.setTotal(7000.0);
        venta.setFecha(java.time.LocalDateTime.now());
        venta.setDetalles(new HashSet<>());
        venta.setPagos(new HashSet<>());

        when(ventaService.registrarVenta(any())).thenReturn(venta);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 1,
                                  "descuento": 0.0,
                                  "detalles": [
                                    {
                                      "productoId": 1,
                                      "loteId": 1,
                                      "cantidad": 2
                                    }
                                  ],
                                  "pagos": [
                                    {
                                      "tipo": "EFECTIVO",
                                      "monto": 7000.0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(7000.0));
    }

    @Test
    void obtenerVenta_inexistente_debeRetornar404() throws Exception {
        when(ventaService.obtenerVentaDetallada(99L))
                .thenThrow(new ResourceNotFoundException("No existe la venta con id 99"));

        mockMvc.perform(get("/api/ventas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe la venta con id 99"));
    }

    @Test
    void obtenerVentaPorId_debeRetornar200() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("vendedor1");

        Venta venta = new Venta();
        venta.setIdVenta(1L);
        venta.setUsuario(usuario);
        venta.setEstado("COMPLETADA");
        venta.setTotal(5000.0);
        venta.setDetalles(new HashSet<>());
        venta.setPagos(new HashSet<>());

        when(ventaService.obtenerVentaDetallada(1L)).thenReturn(venta);

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void anularVenta_debeRetornar200() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("admin1");
        usuario.setNombreCompleto("Admin Uno");

        Venta venta = new Venta();
        venta.setIdVenta(1L);
        venta.setUsuario(usuario);
        venta.setEstado("ANULADA");
        venta.setFecha(java.time.LocalDateTime.now());
        venta.setDetalles(new HashSet<>());
        venta.setPagos(new HashSet<>());

        when(ventaService.eliminarVenta(eq(1L), any(AnularVentaRequest.class))).thenReturn(venta);

        mockMvc.perform(patch("/api/ventas/1/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "motivoAnulacion": "Devolucion por cliente",
                                  "usuarioResponsable": "admin1",
                                  "usuarioId": 1,
                                  "confirmacion": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"));
    }
}
}*/
