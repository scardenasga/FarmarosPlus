package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase base de auditoría. Todas las entidades que necesiten
 * fecha_creacion, fecha_modificacion, usuario_creacion y usuario_modificacion
 * deben extender esta clase.
 *
 * Spring Data JPA rellena estos campos automáticamente gracias a
 * @EnableJpaAuditing (ver JpaAuditingConfig) y AuditorAwareImpl.
 *
 * @author Sebastian Cardenas Garcia
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

     @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @CreatedBy
    @Column(name = "usuario_creacion", updatable = false)
    private String usuarioCreacion;

    @LastModifiedBy
    @Column(name = "usuario_modificacion")
    private String usuarioModificacion;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}