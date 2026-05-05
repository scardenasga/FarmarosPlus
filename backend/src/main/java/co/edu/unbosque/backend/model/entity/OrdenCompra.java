package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Entidad cabecera de una orden de compra a proveedor.
 * Agrupa detalles pedidos, total esperado y estado.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"detalles"})
@ToString(exclude = {"detalles"})
@Entity
@Table(name = "orden_compra")
public class OrdenCompra extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orden_proveedor"))
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orden_usuario"))
    private Usuario usuario;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "fecha_esperada")
    private LocalDateTime fechaEsperada;

    /**
     * Valores válidos: PENDIENTE | CERRADA | NO_RECIBIDA | CANCELADA
     */
    @Column(name = "estado", nullable = false)
    private String estado = "PENDIENTE";

    @Column(name = "total_esperado", nullable = false)
    private Double totalEsperado = 0.0;

    @Column(name = "observaciones")
    private String observaciones;

    @JsonManagedReference("orden-detalles")
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DetalleOrdenCompra> detalles = new HashSet<>();
}
