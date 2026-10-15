package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.ByteArrayResource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductoController.class)
class ProductoImagenControllerTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void subirImagen_debeRetornar200ConImagenUrl() throws Exception {
        Producto p = new Producto();
        p.setUniqueID(1L);
        p.setNombre("Acetaminofen");
        p.setCodigoBarras("7701234567890");
        p.setStockActual(20);
        p.setCosto(8500.0);
        p.setPrecioVenta(12000.0);
        p.setEstado("ACTIVO");
        p.setImagenUrl("producto-1.jpg");

        when(productoService.guardarImagen(eq(1L), any())).thenReturn(p);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1,2,3});

        mockMvc.perform(multipart("/api/productos/1/imagen").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenUrl").value("producto-1.jpg"));
    }

    @Test
    void obtenerImagen_debeRetornar200() throws Exception {
        Resource res = new ByteArrayResource(new byte[]{1,2,3}) {
            @Override public String getFilename() { return "producto-1.jpg"; }
        };
        when(productoService.obtenerImagenResource(1L)).thenReturn(res);
        when(productoService.obtenerImagenContentType(1L)).thenReturn("image/jpeg");

        mockMvc.perform(get("/api/productos/1/imagen"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void eliminarImagen_debeRetornar204() throws Exception {
        mockMvc.perform(delete("/api/productos/1/imagen"))
                .andExpect(status().isNoContent());
    }
}
