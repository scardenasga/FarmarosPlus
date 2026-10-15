package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.ConfiguracionSistema;
import co.edu.unbosque.backend.model.request.ConfiguracionGananciaRequest;
import co.edu.unbosque.backend.model.response.ConfiguracionGananciaResponse;
import co.edu.unbosque.backend.repository.ConfiguracionSistemaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para parámetros configurables del sistema.
 * Actualmente gestiona la ganancia mínima por defecto aplicada
 * al crear productos cuando no se envía precioVenta.
 *
 * Mantiene compatibilidad con cálculo sobre costo (margen = (precio-costo)/costo*100).
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class ConfiguracionService {

    public static final String CLAVE_GANANCIA_MIN = "GANANCIA_MIN_PORCENTAJE";

    private final ConfiguracionSistemaRepository repository;

    private final double gananciaMinDefault;

    public ConfiguracionService(ConfiguracionSistemaRepository repository,
                                @Value("${farmaros.ganancia.min-porcentaje:30.0}") double gananciaMinDefault) {
        this.repository = repository;
        this.gananciaMinDefault = gananciaMinDefault;
    }

    @Transactional(readOnly = true)
    public double obtenerGananciaMinima() {
        return repository.findByClave(CLAVE_GANANCIA_MIN)
                .map(c -> {
                    try {
                        return Double.parseDouble(c.getValor());
                    } catch (NumberFormatException e) {
                        return gananciaMinDefault;
                    }
                })
                .orElse(gananciaMinDefault);
    }

    @Transactional(readOnly = true)
    public ConfiguracionGananciaResponse obtenerConfiguracionGanancia() {
        double porcentaje = obtenerGananciaMinima();
        return new ConfiguracionGananciaResponse(porcentaje);
    }

    @Transactional
    public ConfiguracionGananciaResponse actualizarGananciaMinima(ConfiguracionGananciaRequest request) {
        if (request == null || request.porcentajeMinimo() == null) {
            throw new BusinessException("El porcentaje mínimo es obligatorio");
        }
        double valor = request.porcentajeMinimo();
        if (valor < 0 || valor > 500) {
            throw new BusinessException("El porcentaje mínimo debe estar entre 0 y 500");
        }
        ConfiguracionSistema config = repository.findByClave(CLAVE_GANANCIA_MIN)
                .orElseGet(() -> {
                    ConfiguracionSistema c = new ConfiguracionSistema();
                    c.setClave(CLAVE_GANANCIA_MIN);
                    c.setDescripcion("Porcentaje mínimo de ganancia sobre costo aplicado por defecto al crear productos sin precioVenta");
                    return c;
                });
        config.setValor(String.valueOf(valor));
        repository.save(config);
        return new ConfiguracionGananciaResponse(valor);
    }

    /**
     * Calcula precio sugerido aplicando ganancia mínima sobre costo,
     * asegurando que cubra costo + IVA.
     */
    public double calcularPrecioSugerido(Double costo, Double porcentajeIva) {
        double costoSeguro = costo != null ? costo : 0.0;
        double ivaSeguro = porcentajeIva != null ? porcentajeIva : 0.0;
        double gananciaMin = obtenerGananciaMinima();
        // Precio base con ganancia sobre costo
        double precioBase = costoSeguro * (1 + gananciaMin / 100.0);
        // Precio mínimo para cubrir IVA
        double precioMinimoIva = costoSeguro * (1 + ivaSeguro / 100.0);
        // Debe ser mayor estricto, agregamos 0.01 si queda igual
        if (precioBase <= precioMinimoIva) {
            precioBase = precioMinimoIva + 1.0;
        }
        // Redondeo a 2 decimales
        return Math.round(precioBase * 100.0) / 100.0;
    }
}
