package co.edu.unbosque.backend.recomendacion.domain;

/**
 * Puerto de lectura de canastas.
 */
public interface CanastaReader {

    /**
     * Lee una fotografia de canastas desde la fuente de datos.
     *
     * @return snapshot con todas las canastas y metricas
     */
    CanastaSnapshot leerCanastas();
}
