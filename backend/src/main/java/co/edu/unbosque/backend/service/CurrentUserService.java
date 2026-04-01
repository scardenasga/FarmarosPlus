package co.edu.unbosque.backend.service;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

/**
 * Servicio utilitario para obtener el usuario actual desde la misma fuente
 * usada por la auditoria JPA.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class CurrentUserService {

    private final AuditorAware<String> auditorAware;

    public CurrentUserService(AuditorAware<String> auditorAware) {
        this.auditorAware = auditorAware;
    }

    /**
     * Retorna el usuario actual o SISTEMA si no existe uno disponible.
     *
     * @return nombre del usuario actual
     */
    public String getCurrentUsername() {
        return auditorAware.getCurrentAuditor().orElse("SISTEMA");
    }
}
