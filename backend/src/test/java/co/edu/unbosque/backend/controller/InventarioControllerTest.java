package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.AjusteInventarioRequest;
import co.edu.unbosque.backend.model.request.IngresoLoteRequest;
import co.edu.unbosque.backend.service.InventarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventarioController.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    private Producto buildProducto() {
        Producto p = new Producto();
        p.setUniqueID(1L);
        p.setNombre("Amoxicilina");
        p.setStockActual(100);
        p.setCosto(5000.0);
        p.setPrecioVenta(8000.0);
        p.setEstado("ACTIVO");
        return p;
    }

    private Lote buildLote(Producto producto) {
        Lote l = new Lote();
        l.setIdLote(1L);
        l.setNumeroLote("LOT-001");
        l.setCantidad(50);
        l.setProducto(producto);
        l.setFechaVencimiento(LocalDate.now().plusYears(1));
        return l;
    }

    @Test
    void registrarIngresoLote_debeRetornar201() throws Exception {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        when(inventarioService.registrarIngresoLote(any(IngresoLoteRequest.class))).thenReturn(lote);

        mockMvc.perform(post("/api/inventario/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": 1,
                                  "numeroLote": "LOT-NEW",
                                  "fechaVencimiento": "2027-12-31",
                                  "cantidad": 25,
                                  "motivo": "Entrada inicial"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroLote").value("LOT-001"))
                .andExpect(jsonPath("$.cantidad").value(50));
    }

    @Test
    void ajustarInventario_debeRetornar200() throws Exception {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);
        lote.setCantidad(60);

        when(inventarioService.ajustarInventario(any(AjusteInventarioRequest.class))).thenReturn(lote);

        mockMvc.perform(patch("/api/inventario/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loteId": 1,
                                  "diferencia": 10,
                                  "tipoMovimiento": "AJUSTE",
                                  "motivo": "Conteo fisico"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(60));
    }

    @Test
    void listarLotesDisponibles_debeRetornar200YLista() throws Exception {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        when(inventarioService.listarLotesDisponiblesPorProducto(1L)).thenReturn(List.of(lote));

        mockMvc.perform(get("/api/inventario/productos/1/lotes-disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroLote").value("LOT-001"));
    }

    @Test
    void listarLotesProximosAVencer_debeRetornar200YLista() throws Exception {
        Producto producto = buildProducto();
        Lote lote = buildLote(producto);

        when(inventarioService.listarLotesProximosAVencer(any(LocalDate.class))).thenReturn(List.of(lote));

        mockMvc.perform(get("/api/inventario/lotes/proximos-a-vencer")
                        .param("fechaCorte", "2027-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroLote").value("LOT-001"));
    }

    @Test
    void listarMovimientosPorProducto_debeRetornar200YLista() throws Exception {
        Producto producto = buildProducto();
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setIdMovimiento(1L);
        movimiento.setProducto(producto);
        movimiento.setNombreProducto("Amoxicilina");
        movimiento.setTipoMovimiento("COMPRA");
        movimiento.setCantidadAnterior(90);
        movimiento.setCantidadNueva(100);
        movimiento.setDiferencia(10);

        when(inventarioService.listarMovimientosPorProducto(1L)).thenReturn(List.of(movimiento));

        mockMvc.perform(get("/api/inventario/productos/1/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoMovimiento").value("COMPRA"));
    }
}