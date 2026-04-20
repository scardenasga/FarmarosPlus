package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import co.edu.unbosque.backend.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void crearProducto_debeRetornar201() throws Exception {
        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setStockActual(20);
        producto.setCosto(8500.0);
        producto.setPrecioVenta(12000.0);
        producto.setEstado("ACTIVO");

        when(productoService.crearProducto(any())).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Acetaminofen",
                                  "codigoBarras": "7701234567890",
                                  "stockInicial": 20,
                                  "costo": 8500.0,
                                  "precioVenta": 12000.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoBarras").value("7701234567890"))
                .andExpect(jsonPath("$.stockActual").value(20));
    }

    @Test
    void ingresarStock_debeRetornar200() throws Exception {
        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setStockActual(50);
        producto.setCosto(8500.0);
        producto.setPrecioVenta(12000.0);
        producto.setEstado("ACTIVO");

        when(productoService.ingresarStock(eq("7701234567890"), any())).thenReturn(producto);

        mockMvc.perform(post("/api/productos/codigo-barras/7701234567890/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidad": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(50));
    }

    @Test
    void actualizarPrecio_porCodigoBarras_debeRetornar200() throws Exception {
        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setCosto(9000.0);
        producto.setPrecioVenta(13000.0);
        producto.setEstado("ACTIVO");

        when(productoService.actualizarPrecio(eq("7701234567890"), any(CambioPrecioProductoRequest.class)))
                .thenReturn(producto);

        mockMvc.perform(patch("/api/productos/codigo-barras/7701234567890/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nuevoCosto": 9000.0,
                                  "nuevoPrecioVenta": 13000.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioVenta").value(13000.0));
    }

    @Test
    void listarProductosActivos_debeRetornarLista() throws Exception {
        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setEstado("ACTIVO");

        when(productoService.listarProductosActivos()).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Acetaminofen"));
    }

    @Test
    void crearProducto_sinStockInicial_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Acetaminofen",
                                  "codigoBarras": "7701234567890",
                                  "costo": 8500.0,
                                  "precioVenta": 12000.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
