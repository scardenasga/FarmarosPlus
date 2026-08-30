package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permiso")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @Column(name = "clave", nullable = false, unique = true)
    private String clave;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "modulo")
    private String modulo;
}
