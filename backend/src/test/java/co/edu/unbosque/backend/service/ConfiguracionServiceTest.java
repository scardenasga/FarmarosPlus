package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.ConfiguracionSistema;
import co.edu.unbosque.backend.model.request.ConfiguracionGananciaRequest;
import co.edu.unbosque.backend.repository.ConfiguracionSistemaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracionServiceTest {

    @Mock
    private ConfiguracionSistemaRepository repository;

    @Test
    void obtenerGananciaMinima_sinConfig_debeRetornarDefault() {
        ConfiguracionService service = new ConfiguracionService(repository, 30.0);
        when(repository.findByClave("GANANCIA_MIN_PORCENTAJE")).thenReturn(Optional.empty());
        assertEquals(30.0, service.obtenerGananciaMinima());
    }

    @Test
    void obtenerGananciaMinima_conConfig_debeRetornarValorGuardado() {
        ConfiguracionService service = new ConfiguracionService(repository, 30.0);
        ConfiguracionSistema c = new ConfiguracionSistema();
        c.setClave("GANANCIA_MIN_PORCENTAJE");
        c.setValor("45.5");
        when(repository.findByClave("GANANCIA_MIN_PORCENTAJE")).thenReturn(Optional.of(c));
        assertEquals(45.5, service.obtenerGananciaMinima());
    }

    @Test
    void actualizarGananciaMinima_valorValido_debeGuardar() {
        ConfiguracionService service = new ConfiguracionService(repository, 30.0);
        when(repository.findByClave("GANANCIA_MIN_PORCENTAJE")).thenReturn(Optional.empty());
        when(repository.save(any(ConfiguracionSistema.class))).thenAnswer(i -> i.getArgument(0));

        var resp = service.actualizarGananciaMinima(new ConfiguracionGananciaRequest(25.0));
        assertEquals(25.0, resp.porcentajeMinimo());
        verify(repository).save(any(ConfiguracionSistema.class));
    }

    @Test
    void actualizarGananciaMinima_valorNegativo_debeLanzarExcepcion() {
        ConfiguracionService service = new ConfiguracionService(repository, 30.0);
        assertThrows(BusinessException.class, () -> service.actualizarGananciaMinima(new ConfiguracionGananciaRequest(-5.0)));
    }

    @Test
    void calcularPrecioSugerido_debeAplicarGananciaSobreCosto() {
        ConfiguracionService service = new ConfiguracionService(repository, 30.0);
        ConfiguracionSistema c = new ConfiguracionSistema();
        c.setClave("GANANCIA_MIN_PORCENTAJE");
        c.setValor("30.0");
        when(repository.findByClave("GANANCIA_MIN_PORCENTAJE")).thenReturn(Optional.of(c));

        double sugerido = service.calcularPrecioSugerido(10000.0, 0.0);
        assertEquals(13000.0, sugerido, 0.01);
    }

    @Test
    void calcularPrecioSugerido_conIva_debeCubrirIva() {
        ConfiguracionService service = new ConfiguracionService(repository, 10.0);
        ConfiguracionSistema c = new ConfiguracionSistema();
        c.setClave("GANANCIA_MIN_PORCENTAJE");
        c.setValor("10.0");
        when(repository.findByClave("GANANCIA_MIN_PORCENTAJE")).thenReturn(Optional.of(c));

        // costo 10000, iva 19% => minimo 11900, sugerido 11000 quedaría por debajo, debe ajustar
        double sugerido = service.calcularPrecioSugerido(10000.0, 19.0);
        assertTrue(sugerido > 11900);
    }
}
