package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.service.ProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProveedorController.class)
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProveedorService proveedorService;

    @Test
    void crearProveedor_debeRetornar201() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");
        proveedor.setNit("860123456-7");
        proveedor.setEstado("ACTIVO");

        when(proveedorService.crearProveedor(any())).thenReturn(proveedor);

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Farmaceutica XYZ",
                                  "nit": "860123456-7",
                                  "telefono": "+57 1 1234567",
                                  "email": "contacto@xyz.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProveedor").value(1))
                .andExpect(jsonPath("$.nombre").value("Farmaceutica XYZ"));
    }

    @Test
    void obtenerProveedor_inexistente_debeRetornar404() throws Exception {
        when(proveedorService.obtenerProveedorPorId(99L))
                .thenThrow(new ResourceNotFoundException("No existe proveedor con id 99"));

        mockMvc.perform(get("/api/proveedores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe proveedor con id 99"));
    }

    @Test
    void listarProveedoresActivos_debeRetornar200YLista() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica ABC");
        proveedor.setEstado("ACTIVO");

        when(proveedorService.listarProveedoresActivos()).thenReturn(List.of(proveedor));

        mockMvc.perform(get("/api/proveedores/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Farmaceutica ABC"));
    }

    @Test
    void listarTodosProveedores_debeRetornar200YLista() throws Exception {
        when(proveedorService.listarTodosProveedores()).thenReturn(List.of());

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarEstadoProveedor_debeRetornar200() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");
        proveedor.setEstado("INACTIVO");

        when(proveedorService.actualizarEstadoProveedor(any(), any())).thenReturn(proveedor);

        mockMvc.perform(patch("/api/proveedores/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "INACTIVO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void crearProveedor_sinNombre_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nit": "860123456-7"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}