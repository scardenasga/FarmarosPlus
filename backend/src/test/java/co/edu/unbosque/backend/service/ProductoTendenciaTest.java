package co.edu.unbosque.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas del calculo puro de variacion porcentual usado por la
 * tendencia de ventas de productos.
 */
class ProductoTendenciaTest {

    @Test
    @DisplayName("Sin ventas previas y con ventas recientes equivale a +100%")
    void sinPreviasConRecientes() {
        assertEquals(100.0, ProductoService.calcularPorcentajeCambio(10, 0), 0.001);
    }

    @Test
    @DisplayName("Sin ventas en ambos periodos es 0%")
    void sinVentasEnAmbos() {
        assertEquals(0.0, ProductoService.calcularPorcentajeCambio(0, 0), 0.001);
    }

    @Test
    @DisplayName("Mismas unidades en ambos periodos es 0%")
    void mismasUnidades() {
        assertEquals(0.0, ProductoService.calcularPorcentajeCambio(50, 50), 0.001);
    }

    @Test
    @DisplayName("El doble de unidades recientes es +100%")
    void duplicaUnidades() {
        assertEquals(100.0, ProductoService.calcularPorcentajeCambio(20, 10), 0.001);
    }

    @Test
    @DisplayName("La mitad de unidades recientes es -50%")
    void reduceMitad() {
        assertEquals(-50.0, ProductoService.calcularPorcentajeCambio(5, 10), 0.001);
    }

    @Test
    @DisplayName("Crecimiento parcial se calcula proporcionalmente")
    void crecimientoParcial() {
        // De 4 a 5 unidades: +25%
        assertEquals(25.0, ProductoService.calcularPorcentajeCambio(5, 4), 0.001);
    }
}
