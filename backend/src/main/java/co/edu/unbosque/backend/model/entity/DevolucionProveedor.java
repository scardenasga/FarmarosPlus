package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de una devolución de productos a proveedor.
 *
 * @author juanjo2748
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devolucion_proveedor")
public class DevolucionProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Long idDevolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false,
            foreignKey = @ForeignKey(name = "fk_devolucion_proveedor"))
    private Proveedor proveedor;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "usuario_responsable", nullable = false)
    private String usuarioResponsable;

    @Column(name = "motivo")
    private String motivo;

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleDevolucionProveedor> detalles = new ArrayList<>();
}
