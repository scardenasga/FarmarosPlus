package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Tabla de solo inserción: registra eventos técnicos del sistema (login, logout, etc.).
 * No se modifica una vez creada, por eso no extiende Auditable.
 * Solo usa @CreatedDate para fecha_hora.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "auditoria_sistema")
public class AuditoriaSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    // Nullable: ON DELETE SET NULL en el esquema SQL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario",
            foreignKey = @ForeignKey(name = "fk_auditoria_usuario"))
    private Usuario usuario;

    /**
     * Valores esperados: LOGIN | LOGOUT | EXPORTAR_REPORTE | CAMBIO_CLAVE
     */
    @Column(name = "accion", nullable = false)
    private String accion;

    @Column(name = "tabla_afectada")
    private String tablaAfectada;

    @Column(name = "registro_id")
    private Long registroId;

    @Column(name = "detalles")
    private String detalles;

    @Column(name = "ip_origen")
    private String ipOrigen;

    @CreatedDate
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
}
