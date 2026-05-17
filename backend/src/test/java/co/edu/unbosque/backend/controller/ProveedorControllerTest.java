package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.ProveedorProducto;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.model.response.ProductoProveedorResponse;
import co.edu.unbosque.backend.model.response.ProveedorDetalleResponse;
import co.edu.unbosque.backend.service.ProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
        proveedor.setCondicionPago("Neto 30");
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
                .andExpect(jsonPath("$.nombre").value("Farmaceutica XYZ"))
                .andExpect(jsonPath("$.condicionPago").value("Neto 30"));
    }

    @Test
    void crearProveedor_conCondicionDePagoAlias_debeRetornar201() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(2L);
        proveedor.setNombre("Farmaceutica Alias");
        proveedor.setCondicionPago("Contra Entrega");
        proveedor.setEstado("ACTIVO");

        when(proveedorService.crearProveedor(any())).thenReturn(proveedor);

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Farmaceutica Alias",
                                  "condicionDePago": "Contra Entrega"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.condicionPago").value("Contra Entrega"));
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
                .andExpect(jsonPath("$[0].nombre").value("Farmaceutica ABC"))
                .andExpect(jsonPath("$[0].fechaCreacion").doesNotExist())
                .andExpect(jsonPath("$[0].fechaModificacion").doesNotExist());
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

    @Test
    void obtenerProveedorDetalle_porId_debeRetornar200ConProductos() throws Exception {
        ProveedorDetalleResponse response = new ProveedorDetalleResponse(
                1L,
                "Farmaceutica XYZ",
                "860123456-7",
                "+57 1 1234567",
                "contacto@xyz.com",
                "Juan Perez",
                "ACTIVO",
                "Neto 30",
                List.of(new ProductoProveedorResponse(
                        10L,
                        "Acetaminofen",
                        "Caja x 20",
                        "7701234567890",
                        "ACTIVO",
                        "PR-ACET-01",
                        8500.0,
                        "ACTIVO"
                ))
        );

        when(proveedorService.obtenerProveedorDetallePorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/proveedores/1/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProveedor").value(1))
                .andExpect(jsonPath("$.productos[0].nombre").value("Acetaminofen"))
                .andExpect(jsonPath("$.productos[0].estadoRelacion").value("ACTIVO"))
                .andExpect(jsonPath("$.fechaCreacion").doesNotExist())
                .andExpect(jsonPath("$.fechaModificacion").doesNotExist());
    }

    @Test
    void obtenerProveedorDetalle_porId_sinProductos_debeRetornarListaVacia() throws Exception {
        ProveedorDetalleResponse response = new ProveedorDetalleResponse(
                2L,
                "Proveedor sin productos",
                null,
                null,
                null,
                null,
                "ACTIVO",
                null,
                List.of()
        );

        when(proveedorService.obtenerProveedorDetallePorId(2L)).thenReturn(response);

        mockMvc.perform(get("/api/proveedores/2/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos").isEmpty());
    }

    @Test
    void actualizarProveedor_debeRetornar200() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ Actualizada");
        proveedor.setTelefono("+57 601 7654321");
        proveedor.setEstado("ACTIVO");

        when(proveedorService.actualizarProveedor(eq(1L), any())).thenReturn(proveedor);

        mockMvc.perform(patch("/api/proveedores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Farmaceutica XYZ Actualizada",
                                  "telefono": "+57 601 7654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Farmaceutica XYZ Actualizada"));
    }

    @Test
    void asociarProducto_debeRetornar201() throws Exception {
        Producto producto = new Producto();
        producto.setUniqueID(10L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setEstado("ACTIVO");

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);

        ProveedorProducto relacion = new ProveedorProducto();
        relacion.setProveedor(proveedor);
        relacion.setProducto(producto);
        relacion.setCodigoProductoProveedor("PR-ACET-01");
        relacion.setPrecioReferencia(8500.0);
        relacion.setEstado("ACTIVO");

        when(proveedorService.asociarProducto(eq(1L), any())).thenReturn(relacion);

        mockMvc.perform(post("/api/proveedores/1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": 10,
                                  "codigoProductoProveedor": "PR-ACET-01",
                                  "precioReferencia": 8500.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.codigoProductoProveedor").value("PR-ACET-01"));
    }

    @Test
    void asociarProducto_sinProductoId_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/proveedores/1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoProductoProveedor": "PR-ACET-01"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarEstadoProductoProveedor_debeRetornar200() throws Exception {
        Producto producto = new Producto();
        producto.setUniqueID(10L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setEstado("ACTIVO");

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);

        ProveedorProducto relacion = new ProveedorProducto();
        relacion.setProveedor(proveedor);
        relacion.setProducto(producto);
        relacion.setEstado("INACTIVO");

        when(proveedorService.actualizarEstadoProductoProveedor(eq(1L), eq(10L), any()))
                .thenReturn(relacion);

        mockMvc.perform(patch("/api/proveedores/1/productos/10/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "INACTIVO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoRelacion").value("INACTIVO"));
    }

    @Test
    void eliminarProductoProveedor_debeRetornar204() throws Exception {
        doNothing().when(proveedorService).eliminarProductoProveedor(1L, 10L);

        mockMvc.perform(delete("/api/proveedores/1/productos/10"))
                .andExpect(status().isNoContent());
    }
}
