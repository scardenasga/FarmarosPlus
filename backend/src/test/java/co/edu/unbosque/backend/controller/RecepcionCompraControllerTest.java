package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.*;
import co.edu.unbosque.backend.model.request.AgregarDetalleRecepcionRequest;
import co.edu.unbosque.backend.model.request.RegistrarRecepcionRequest;
import co.edu.unbosque.backend.service.RecepcionCompraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecepcionCompraController.class)
class RecepcionCompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecepcionCompraService recepcionCompraService;

    private RecepcionCompra buildRecepcion() {
        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrden(1L);

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");
        orden.setProveedor(proveedor);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("admin1");
        usuario.setNombreCompleto("Admin Uno");
        usuario.setRol("ADMIN");
        usuario.setEstado("ACTIVO");
        orden.setUsuario(usuario);

        RecepcionCompra recepcion = new RecepcionCompra();
        recepcion.setIdRecepcion(1L);
        recepcion.setOrden(orden);
        recepcion.setUsuario(usuario);
        recepcion.setFechaRecepcion(LocalDateTime.now());
        recepcion.setEstado("COMPLETA");
        recepcion.setEstadoPago("PENDIENTE");
        recepcion.setTotalRecepcion(50000.0);
        recepcion.setMontoPagado(0.0);
        return recepcion;
    }

    @Test
    void registrarRecepcion_debeRetornar201() throws Exception {
        RecepcionCompra recepcion = buildRecepcion();

        when(recepcionCompraService.registrarRecepcion(any())).thenReturn(recepcion);

        mockMvc.perform(post("/api/recepciones-compra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ordenId": 1,
                                  "estado": "COMPLETA",
                                  "totalRecepcion": 50000.0,
                                  "observaciones": "Sin novedades"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRecepcion").value(1))
                .andExpect(jsonPath("$.estado").value("COMPLETA"));
    }

    @Test
    void agregarDetalleRecepcion_debeRetornar201() throws Exception {
        DetalleRecepcionCompra detalle = new DetalleRecepcionCompra();
        detalle.setIdDetalleRecepcion(1L);
        detalle.setCantidadRecibida(100);
        detalle.setCostoUnitarioReal(1500.0);

        when(recepcionCompraService.agregarDetalleRecepcion(eq(1L), any(AgregarDetalleRecepcionRequest.class)))
                .thenReturn(detalle);

        mockMvc.perform(post("/api/recepciones-compra/1/detalles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "detalleOrdenId": 1,
                                  "cantidadRecibida": 100,
                                  "costoUnitarioReal": 1500.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDetalleRecepcion").value(1))
                .andExpect(jsonPath("$.cantidadRecibida").value(100));
    }

    @Test
    void obtenerRecepcion_inexistente_debeRetornar404() throws Exception {
        when(recepcionCompraService.obtenerRecepcionPorId(99L))
                .thenThrow(new ResourceNotFoundException("No existe recepcion con id 99"));

        mockMvc.perform(get("/api/recepciones-compra/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe recepcion con id 99"));
    }

    @Test
    void listarRecepcionesPendientes_debeRetornar200YLista() throws Exception {
        when(recepcionCompraService.listarRecepcionesPendientes()).thenReturn(List.of(buildRecepcion()));

        mockMvc.perform(get("/api/recepciones-compra/pendientes-pago"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRecepcion").value(1));
    }

    @Test
    void listarTodasRecepciones_debeRetornar200YLista() throws Exception {
        when(recepcionCompraService.listarTodasRecepciones()).thenReturn(List.of());

        mockMvc.perform(get("/api/recepciones-compra"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarEstadoPago_debeRetornar200() throws Exception {
        RecepcionCompra recepcion = buildRecepcion();
        recepcion.setEstadoPago("PAGADO");
        recepcion.setMontoPagado(50000.0);

        when(recepcionCompraService.actualizarEstadoPago(eq(1L), eq("PAGADO"), eq(50000.0)))
                .thenReturn(recepcion);

        mockMvc.perform(patch("/api/recepciones-compra/1/estado-pago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estadoPago": "PAGADO",
                                  "montoPagado": 50000.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago").value("PAGADO"));
    }
}