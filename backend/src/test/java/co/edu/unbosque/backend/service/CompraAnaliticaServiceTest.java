package co.edu.unbosque.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas del calculo puro de porcentaje de cumplimiento de entregas.
 */
class CompraAnaliticaServiceTest {

    @Test
    @DisplayName("Todas a tiempo equivale a 100%")
    void todasATiempo() {
        assertEquals(100.0, CompraAnaliticaService.porcentaje(6, 6), 0.001);
    }

    @Test
    @DisplayName("Ninguna a tiempo equivale a 0%")
    void ningunaATiempo() {
        assertEquals(0.0, CompraAnaliticaService.porcentaje(0, 4), 0.001);
    }

    @Test
    @DisplayName("Cumplimiento parcial se calcula proporcionalmente")
    void cumplimientoParcial() {
        // 19 de 20 => 95%
        assertEquals(95.0, CompraAnaliticaService.porcentaje(19, 20), 0.001);
    }

    @Test
    @DisplayName("Sin recepciones el porcentaje es 0 y no division por cero")
    void sinRecepciones() {
        assertEquals(0.0, CompraAnaliticaService.porcentaje(0, 0), 0.001);
    }
}