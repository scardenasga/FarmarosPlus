package co.edu.unbosque.backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activa el mecanismo de auditoría automática de Spring Data JPA.
 *
 * auditorAwareRef apunta al bean AuditorAwareImpl, que provee
 * el nombre del usuario actual para @CreatedBy y @LastModifiedBy.
 *
 * @author Sebastian Cardenas Garcia
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
}
