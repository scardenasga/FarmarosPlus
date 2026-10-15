package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de usuario interno del sistema.
 * Modela credenciales, rol operativo y estado de acceso.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "usuario")
public class Usuario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    /**
     * Valores válidos: ADMIN | REGENTE | VENDEDOR | ALMACENISTA
     * Validar con @Pattern o @NotNull en el DTO correspondiente.
     */
    @Column(name = "rol", nullable = false)
    private String rol = "VENDEDOR";

    /**
     * Valores válidos: ACTIVO | INACTIVO
     */
    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;
}
