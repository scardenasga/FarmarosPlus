package co.edu.unbosque.backend.recomendacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Parametros operativos del modulo de recomendaciones.
 */
@Component
@ConfigurationProperties(prefix = "farmaros.recomendaciones")
public class RecomendacionProperties {

    /**
     * Soporte relativo minimo (0..1). Se convierte a soporte absoluto con max(2, ceil(x * totalVentas)).
     */
    private double minSupportRelativo = 0.10;

    /**
     * Confianza minima (0..1).
     */
    private double minConfidence = 0.50;

    /**
     * Lift minimo exclusivo (> lift).
     */
    private double minLift = 1.0;

    /**
     * TTL de cache en minutos.
     */
    private long cacheTtlMinutes = 60;

    /**
     * Si true, excluye productos que requieren prescripcion del POS.
     */
    private boolean excluirPrescripcion = true;

    /**
     * Limite maximo de recomendaciones por respuesta.
     */
    private int limiteMaximo = 5;

    public double getMinSupportRelativo() {
        return minSupportRelativo;
    }

    public void setMinSupportRelativo(double minSupportRelativo) {
        this.minSupportRelativo = minSupportRelativo;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    public double getMinLift() {
        return minLift;
    }

    public void setMinLift(double minLift) {
        this.minLift = minLift;
    }

    public long getCacheTtlMinutes() {
        return cacheTtlMinutes;
    }

    public void setCacheTtlMinutes(long cacheTtlMinutes) {
        this.cacheTtlMinutes = cacheTtlMinutes;
    }

    public boolean isExcluirPrescripcion() {
        return excluirPrescripcion;
    }

    public void setExcluirPrescripcion(boolean excluirPrescripcion) {
        this.excluirPrescripcion = excluirPrescripcion;
    }

    public int getLimiteMaximo() {
        return limiteMaximo;
    }

    public void setLimiteMaximo(int limiteMaximo) {
        this.limiteMaximo = limiteMaximo;
    }
}
