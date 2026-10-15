package co.edu.unbosque.backend.model.entity;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Le dice a Spring Data JPA quién es el usuario actual para rellenar
 * @CreatedBy y @LastModifiedBy automáticamente.
 *
 * ─── CUANDO INTEGRES SPRING SECURITY ────────────────────────────────────────
 * Reemplaza el cuerpo del método por:
 *
 *   return Optional.ofNullable(
 *       SecurityContextHolder.getContext().getAuthentication()
 *   )
 *   .filter(auth -> auth.isAuthenticated()
 *       && !(auth instanceof AnonymousAuthenticationToken))
 *   .map(Authentication::getName);
 *
 * Y agrega estos imports:
 *   import org.springframework.security.core.Authentication;
 *   import org.springframework.security.core.context.SecurityContextHolder;
 *   import org.springframework.security.authentication.AnonymousAuthenticationToken;
 * ────────────────────────────────────────────────────────────────────────────
 *
 * @author Sebastian Cardenas Garcia
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // TODO: reemplazar por SecurityContextHolder cuando implementes Spring Security
        return Optional.of("SISTEMA");
    }
}
