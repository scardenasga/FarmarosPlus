package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.response.AnaliticaDashboardResponse;
import co.edu.unbosque.backend.model.response.DashboardResponse;
import co.edu.unbosque.backend.repository.DashboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(dashboardRepository);
    }

    @Test
    void obtenerDashboard_debeCalcularResumenYRetornarResponse() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fin = LocalDateTime.now();

        List<Object[]> ventasDia = new ArrayList<>();
        ventasDia.add(new Object[]{150000.0, 5L});
        when(dashboardRepository.resumenVentas(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(ventasDia);
        when(dashboardRepository.totalComprasProveedor(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(50000.0);
        when(dashboardRepository.contarProductosStockBajo()).thenReturn(4L);
        when(dashboardRepository.contarProductosPorVencer(any(LocalDate.class), any(LocalDate.class))).thenReturn(2L);
        when(dashboardRepository.productosConStockBajo()).thenReturn(List.<Object[]>of());
        when(dashboardRepository.ventasAgrupadasPorDia(anyString(), anyString())).thenReturn(List.<Object[]>of());
        when(dashboardRepository.ventasCompletadasPorPeriodo(any(), any())).thenReturn(List.<Object[]>of());
        when(dashboardRepository.inventarioPorCategoria()).thenReturn(List.<Object[]>of());
        when(dashboardRepository.productosMasVendidos(any(), any(), any())).thenReturn(List.<Object[]>of());

        DashboardResponse response = dashboardService.obtenerDashboard(inicio, fin);

        assertNotNull(response);
        assertEquals(150000.0, response.resumen().ventasDelDia());
        assertEquals(50000.0, response.resumen().comprasDelMes());
        assertEquals(100000.0, response.resumen().gananciaDelMes());
        assertNotNull(response.comparativaMensual());
    }

    @Test
    void obtenerAnalitica_debeRetornarAnaliticaCompleta() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fin = LocalDateTime.now();

        when(dashboardRepository.ventasAgrupadasPorDia(anyString(), anyString())).thenReturn(List.<Object[]>of());
        List<Object[]> prods = new ArrayList<>();
        prods.add(new Object[]{1L, "Amoxicilina 500mg", "Antibióticos", 10L, 50000.0, 3000.0, 5000.0, 5, 10, 1L});
        when(dashboardRepository.productosAnaliticaPeriodo(any(), any())).thenReturn(prods);

        when(dashboardRepository.calcularValorLotesPorVencer(any(), any())).thenReturn(12000.0);
        when(dashboardRepository.contarProductosPorVencer(any(), any())).thenReturn(2L);

        List<Object[]> cats = new ArrayList<>();
        cats.add(new Object[]{"Antibióticos", 50000.0, 10L});
        when(dashboardRepository.ventasPorCategoriaPeriodo(any(), any())).thenReturn(cats);

        AnaliticaDashboardResponse response = dashboardService.obtenerAnalitica(inicio, fin, null, null, false);

        assertNotNull(response);
        assertEquals(50000.0, response.resumen().ventasFiltradas());
        assertEquals(10L, response.resumen().unidadesVendidas());
        assertEquals("Antibióticos", response.resumen().categoriaLider());
        assertEquals("Amoxicilina 500mg", response.resumen().topProducto());
        assertEquals(1, response.productos().size());
        assertEquals("critical", response.productos().get(0).estadoStock());
    }
}
