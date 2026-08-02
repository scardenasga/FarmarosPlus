package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.ActualizarProductoRequest;
import co.edu.unbosque.backend.model.request.CambioPrecioProductoRequest;
import co.edu.unbosque.backend.model.response.ProductoDetalleResponse;
import co.edu.unbosque.backend.model.response.ProductoResponse;
import co.edu.unbosque.backend.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
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

        ProductoResponse respuesta = new ProductoResponse(
                1L, null, "Acetaminofen", null, "7701234567890",
                0, 20, 8500.0, 12000.0, 41.18, 0.0, false, "ACTIVO", Collections.emptyList()
        );

        when(productoService.crearProducto(any())).thenReturn(producto);
        when(productoService.toProductoResponse(producto)).thenReturn(respuesta);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Acetaminofen",
                                  "codigoBarras": "7701234567890",
                                  "stockInicial": 20,
                                  "costo": 8500.0,
                                  "precioVenta": 12000.0,
                                  "requierePrescripcion": false,
                                  "fechaVencimiento": "2027-12-31",
                                  "numeroLote": "ACE-2026-01"
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

        ProductoResponse respuesta = new ProductoResponse(
                1L, null, "Acetaminofen", null, "7701234567890",
                0, 50, 8500.0, 12000.0, 41.18, 0.0, false, "ACTIVO", Collections.emptyList()
        );

        when(productoService.ingresarStock(eq("7701234567890"), any())).thenReturn(producto);
        when(productoService.toProductoResponse(producto)).thenReturn(respuesta);

        mockMvc.perform(post("/api/productos/codigo-barras/7701234567890/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidad": 30,
                                  "numeroLote": "ACE-2026-02",
                                  "fechaVencimiento": "2027-12-31"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(50));
    }

    @Test
    void ingresarStock_sinFechaVencimiento_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/productos/codigo-barras/7701234567890/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidad": 30,
                                  "numeroLote": "ACE-2026-02"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ingresarStock_sinNumeroLote_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/productos/codigo-barras/7701234567890/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidad": 30,
                                  "fechaVencimiento": "2027-12-31"
                                }
                                """))
                .andExpect(status().isBadRequest());
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

        ProductoResponse respuesta = new ProductoResponse(
                1L, null, "Acetaminofen", null, "7701234567890",
                0, 0, 9000.0, 13000.0, 44.44, 0.0, false, "ACTIVO", Collections.emptyList()
        );

        when(productoService.actualizarPrecio(eq("7701234567890"), any(CambioPrecioProductoRequest.class)))
                .thenReturn(producto);
        when(productoService.toProductoResponse(producto)).thenReturn(respuesta);

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

        ProductoResponse respuesta = new ProductoResponse(
                1L, null, "Acetaminofen", null, "7701234567890",
                0, 0, 0.0, 0.0, 0.0, 0.0, false, "ACTIVO", Collections.emptyList()
        );

        when(productoService.listarProductosActivos()).thenReturn(List.of(producto));
        when(productoService.toProductoResponse(producto)).thenReturn(respuesta);

        mockMvc.perform(get("/api/productos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Acetaminofen"));
    }

    @Test
    void buscarProductos_debeRetornarResultadosSinErroresDeParseo() throws Exception {
        ProductoResponse respuesta = new ProductoResponse(
                1L,
                null,
                "Acetaminofen",
                null,
                "7701234567890",
                0,
                20,
                8500.0,
                12000.0,
                41.18,
                0.0,
                false,
                "ACTIVO",
                Collections.emptyList()
        );

        when(productoService.buscarActivosPorNombreOCodigo("acetaminofen")).thenReturn(List.of(respuesta));

        mockMvc.perform(get("/api/productos/buscar").param("nombre", "acetaminofen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoBarras").value("7701234567890"));
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

    @Test
    void crearProducto_sinNumeroLote_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Acetaminofen",
                                  "codigoBarras": "7701234567890",
                                  "stockInicial": 20,
                                  "costo": 8500.0,
                                  "precioVenta": 12000.0,
                                  "requierePrescripcion": false,
                                  "fechaVencimiento": "2027-12-31"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarProducto_debeRetornar200() throws Exception {
        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Acetaminofen 650mg");
        producto.setCodigoBarras("7701234567890");
        producto.setEstado("ACTIVO");

        ProductoResponse respuesta = new ProductoResponse(
                1L, null, "Acetaminofen 650mg", "Caja por 20 tabletas", "7701234567890",
                12, 120, 9000.0, 13500.0, 50.0, 0.0, false, "ACTIVO", Collections.emptyList()
        );

        when(productoService.actualizarProducto(eq(1L), any(ActualizarProductoRequest.class))).thenReturn(producto);
        when(productoService.toProductoResponse(producto)).thenReturn(respuesta);

        mockMvc.perform(patch("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": 2,
                                  "nombre": "Acetaminofen 650mg",
                                  "descripcion": "Caja por 20 tabletas",
                                  "stockMinimo": 12,
                                  "stockActual": 120,
                                  "costo": 9000.0,
                                  "precioVenta": 13500.0,
                                  "porcentajeIva": 0.0,
                                  "requierePrescripcion": false,
                                  "estado": "ACTIVO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Acetaminofen 650mg"));
    }

    @Test
    void obtenerProductoDetalle_porId_debeRetornar200ConLotes() throws Exception {
        ProductoDetalleResponse respuesta = new ProductoDetalleResponse(
                1L,
                null,
                "Acetaminofen",
                null,
                "7701234567890",
                0,
                20,
                8500.0,
                12000.0,
                null,
                0.0,
                false,
                "ACTIVO",
                List.of()
        );

        when(productoService.obtenerProductoDetallePorId(1L)).thenReturn(respuesta);

        mockMvc.perform(get("/api/productos/1/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoBarras").value("7701234567890"));
    }

    @Test
    void obtenerProductoDetalle_porId_sinLotes_debeRetornarListaVacia() throws Exception {
        ProductoDetalleResponse respuesta = new ProductoDetalleResponse(
                2L,
                null,
                "Producto sin lotes",
                null,
                "7702222222222",
                0,
                5,
                1000.0,
                2000.0,
                null,
                0.0,
                false,
                "ACTIVO",
                List.of()
        );

        when(productoService.obtenerProductoDetallePorId(2L)).thenReturn(respuesta);

        mockMvc.perform(get("/api/productos/2/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoBarras").value("7702222222222"))
                .andExpect(jsonPath("$.lotes").isEmpty());
    }
}