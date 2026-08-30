package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rol_permiso", uniqueConstraints = @UniqueConstraint(columnNames = {"rol", "id_permiso"}))
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rol", nullable = false)
    private String rol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}
