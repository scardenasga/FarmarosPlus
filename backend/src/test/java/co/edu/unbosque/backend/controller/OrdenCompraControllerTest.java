package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.DetalleOrdenCompra;
import co.edu.unbosque.backend.model.entity.OrdenCompra;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.request.AgregarDetalleOrdenRequest;
import co.edu.unbosque.backend.service.OrdenCompraService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrdenCompraController.class)
class OrdenCompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenCompraService ordenCompraService;

    private OrdenCompra buildOrden() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("admin1");
        usuario.setNombreCompleto("Admin Uno");

        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrden(1L);
        orden.setProveedor(proveedor);
        orden.setUsuario(usuario);
        orden.setFechaPedido(LocalDateTime.now());
        orden.setFechaEsperada(LocalDateTime.now().plusDays(7));
        orden.setEstado("PENDIENTE");
        orden.setTotalEsperado(50000.0);
        return orden;
    }

    @Test
    void crearOrdenCompra_debeRetornar201() throws Exception {
        OrdenCompra orden = buildOrden();

        when(ordenCompraService.crearOrdenCompra(any())).thenReturn(orden);

        mockMvc.perform(post("/api/ordenes-compra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "proveedorId": 1,
                                  "totalEsperado": 50000.0,
                                  "observaciones": "Envio urgente"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrden").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void agregarDetalleOrden_debeRetornar201() throws Exception {
        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setIdDetalle(1L);
        detalle.setNombreProducto("Amoxicilina 500mg");
        detalle.setCantidadPedida(100);
        detalle.setPrecioUnitarioPactado(1500.0);

        when(ordenCompraService.agregarDetalleOrden(eq(1L), any(AgregarDetalleOrdenRequest.class)))
                .thenReturn(detalle);

        mockMvc.perform(post("/api/ordenes-compra/1/detalles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": 5,
                                  "nombreProducto": "Amoxicilina 500mg",
                                  "cantidadPedida": 100,
                                  "precioUnitarioPactado": 1500.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDetalle").value(1))
                .andExpect(jsonPath("$.nombreProducto").value("Amoxicilina 500mg"));
    }

    @Test
    void obtenerOrden_inexistente_debeRetornar404() throws Exception {
        when(ordenCompraService.obtenerOrdenPorId(99L))
                .thenThrow(new ResourceNotFoundException("No existe orden con id 99"));

        mockMvc.perform(get("/api/ordenes-compra/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe orden con id 99"));
    }

    @Test
    void listarOrdenesPendientes_debeRetornar200YLista() throws Exception {
        when(ordenCompraService.listarOrdenesPendientes()).thenReturn(List.of(buildOrden()));

        mockMvc.perform(get("/api/ordenes-compra/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idOrden").value(1));
    }

    @Test
    void listarTodasOrdenes_debeRetornar200YLista() throws Exception {
        when(ordenCompraService.listarTodasOrdenes()).thenReturn(List.of());

        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isOk());
    }
}