package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que registra devoluciones de productos realizadas por clientes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "devolucion_cliente")
public class DevolucionCliente extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion_cliente")
    private Long idDevolucionCliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_devolucion_cliente_venta"))
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false,
            foreignKey = @ForeignKey(name = "fk_devolucion_cliente_usuario"))
    private Usuario usuario;

    @Column(name = "usuario_responsable")
    private String usuarioResponsable;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "documento_cliente")
    private String documentoCliente;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "motivo")
    private String motivo;

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleDevolucionCliente> detalles = new ArrayList<>();
}
