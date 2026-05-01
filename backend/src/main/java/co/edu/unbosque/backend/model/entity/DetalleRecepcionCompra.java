package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Detalle de una recepción de compra.
 * Vincula los productos recibidos con los detalles de la orden original.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_recepcion_compra")
public class DetalleRecepcionCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_recepcion")
    private Long idDetalleRecepcion;

    @JsonBackReference("recepcion-detalles")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_recepcion", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_recepcion_recepcion"))
    private RecepcionCompra recepcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_detalle_orden", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_recepcion_detalle_orden"))
    private DetalleOrdenCompra detalleOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto",
            foreignKey = @ForeignKey(name = "fk_detalle_recepcion_producto"))
    private Producto producto;

    @Column(name = "cantidad_recibida", nullable = false)
    private Integer cantidadRecibida;

    @Column(name = "costo_unitario_real")
    private Double costoUnitarioReal;

    @Column(name = "observaciones")
    private String observaciones;
}
