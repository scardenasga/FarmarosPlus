package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de la agregación de métricas del reporte de ventas.
 * No requiere contexto de Spring: la lógica es pura sobre las entidades.
 */
class ReporteVentasServiceTest {

    private final ReporteVentasService service = new ReporteVentasService();

    // ── Helpers para construir ventas sintéticas ────────────────────────────

    private Producto producto(String nombre) {
        Producto p = new Producto();
        p.setNombre(nombre);
        return p;
    }

    private DetalleVenta detalle(Producto producto, int cantidad, double subtotalLinea, double ivaLinea) {
        DetalleVenta d = new DetalleVenta();
        d.setProducto(producto);
        d.setCantidad(cantidad);
        d.setSubtotalLinea(subtotalLinea);
        d.setIvaLinea(ivaLinea);
        return d;
    }

    private Venta venta(Long id, String estado, LocalDateTime fecha, Double total,
                        Double descuento, DetalleVenta... detalles) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Vendedor Test");

        Venta v = new Venta();
        v.setIdVenta(id);
        v.setUsuario(usuario);
        v.setEstado(estado);
        v.setFecha(fecha);
        v.setTotal(total);
        v.setDescuento(descuento);

        Set<DetalleVenta> setDetalles = new HashSet<>();
        for (DetalleVenta d : detalles) {
            d.setVenta(v);
            setDetalles.add(d);
        }
        v.setDetalles(setDetalles);
        return v;
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Lista vacía produce un resumen en ceros y sin productos")
    void resumenConListaVacia() {
        ReporteVentasService.ResumenVentas r = service.calcularResumen(List.of());

        assertEquals(0, r.totalVentas());
        assertEquals(0, r.completadas());
        assertEquals(0, r.anuladas());
        assertEquals(0.0, r.totalIngresos());
        assertEquals(0.0, r.ticketPromedio());
        assertEquals(0, r.unidadesVendidas());
        assertTrue(r.topProductos().isEmpty());
        assertTrue(r.menosVendidos().isEmpty());
        assertTrue(r.ingresosPorDia().isEmpty());
    }

    @Test
    @DisplayName("Las ventas anuladas no suman ingresos pero sí al conteo")
    void anuladasNoSumanIngresos() {
        ReporteVentasService.ResumenVentas r = service.calcularResumen(List.of(
                venta(1L, "COMPLETADA", LocalDateTime.of(2026, 8, 1, 10, 0), 10000.0, 0.0,
                        detalle(producto("A"), 2, 10000.0, 500.0)),
                venta(2L, "ANULADA", LocalDateTime.of(2026, 8, 2, 11, 0), 50000.0, 0.0,
                        detalle(producto("B"), 5, 50000.0, 0.0))
        ));

        assertEquals(2, r.totalVentas());
        assertEquals(1, r.completadas());
        assertEquals(1, r.anuladas());
        assertEquals(10000.0, r.totalIngresos(), 0.001);
        assertEquals(10000.0, r.ticketPromedio(), 0.001);
        // Las unidades del detalle anulado no cuentan.
        assertEquals(2, r.unidadesVendidas());
        assertEquals(500.0, r.ivaRecaudado(), 0.001);
    }

    @Test
    @DisplayName("Top productos ordenado por unidades descendente")
    void topProductosOrdenados() {
        ReporteVentasService.ResumenVentas r = service.calcularResumen(List.of(
                venta(1L, "COMPLETADA", LocalDateTime.of(2026, 8, 1, 10, 0), 30000.0, 0.0,
                        detalle(producto("Paracetamol"), 3, 9000.0, 0.0),
                        detalle(producto("Ibuprofeno"), 1, 21000.0, 0.0)),
                venta(2L, "COMPLETADA", LocalDateTime.of(2026, 8, 2, 12, 0), 18000.0, 0.0,
                        detalle(producto("Paracetamol"), 6, 18000.0, 0.0))
        ));

        List<ReporteVentasService.ProductoVendido> top = r.topProductos();
        assertEquals(2, top.size());
        assertEquals("Paracetamol", top.get(0).nombre());
        assertEquals(9, top.get(0).unidades());
        assertEquals("Ibuprofeno", top.get(1).nombre());

        // Menos vendidos: ascendente por unidades.
        assertEquals("Ibuprofeno", r.menosVendidos().get(0).nombre());
        assertEquals(27000.0, top.get(0).ingresos(), 0.001);
    }

    @Test
    @DisplayName("Rendimiento por día: mejor día con mayores ingresos")
    void mejorDiaCorrecto() {
        ReporteVentasService.ResumenVentas r = service.calcularResumen(List.of(
                venta(1L, "COMPLETADA", LocalDateTime.of(2026, 8, 1, 9, 0), 20000.0, 0.0,
                        detalle(producto("A"), 1, 20000.0, 0.0)),
                venta(2L, "COMPLETADA", LocalDateTime.of(2026, 8, 2, 15, 0), 80000.0, 0.0,
                        detalle(producto("A"), 4, 80000.0, 0.0)),
                venta(3L, "COMPLETADA", LocalDateTime.of(2026, 8, 2, 16, 0), 10000.0, 0.0,
                        detalle(producto("A"), 1, 10000.0, 0.0))
        ));

        LocalDate dia1 = LocalDate.of(2026, 8, 1);
        LocalDate dia2 = LocalDate.of(2026, 8, 2);

        assertEquals(2, r.ingresosPorDia().size());
        assertEquals(dia2, r.mejorDia());
        assertEquals(90000.0, r.mejorDiaIngresos(), 0.001);
        assertEquals(dia1, r.peorDia());
        assertEquals(20000.0, r.peorDiaIngresos(), 0.001);
        assertEquals(1, r.transaccionesPorDia().get(dia1));
        assertEquals(2, r.transaccionesPorDia().get(dia2));
        assertEquals(55000.0, r.promedioDiario(), 0.001);
    }

    @Test
    @DisplayName("Tendencia positiva cuando la segunda mitad vende más")
    void tendenciaPositiva() {
        ReporteVentasService.ResumenVentas r = service.calcularResumen(List.of(
                venta(1L, "COMPLETADA", LocalDateTime.of(2026, 8, 1, 9, 0), 10000.0, 0.0,
                        detalle(producto("A"), 1, 10000.0, 0.0)),
                venta(2L, "COMPLETADA", LocalDateTime.of(2026, 8, 10, 9, 0), 30000.0, 0.0,
                        detalle(producto("A"), 3, 30000.0, 0.0))
        ));

        // Período del 1 al 10: corte en el día 6. La 2ª mitad duplica a la 1ª.
        assertEquals(10000.0, r.ingresoPrimeraMitad(), 0.001);
        assertEquals(30000.0, r.ingresoSegundaMitad(), 0.001);
        assertEquals(200.0, r.tendenciaPorcentaje(), 0.001);
    }

    @Test
    @DisplayName("Descuentos otorgados solo de ventas completadas")
    void descuentosAcumulados() {
        ReporteVentasService.ResumenVentas r = service.calcularResumen(List.of(
                venta(1L, "COMPLETADA", LocalDateTime.of(2026, 8, 1, 9, 0), 9000.0, 1000.0,
                        detalle(producto("A"), 1, 9000.0, 0.0)),
                venta(2L, "ANULADA", LocalDateTime.of(2026, 8, 1, 10, 0), 8000.0, 2000.0,
                        detalle(producto("A"), 1, 8000.0, 0.0))
        ));

        assertEquals(1000.0, r.descuentosOtorgados(), 0.001);
    }
}
