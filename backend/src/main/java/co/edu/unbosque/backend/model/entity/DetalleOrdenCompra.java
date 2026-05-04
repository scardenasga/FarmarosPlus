package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Detalle de una línea de orden de compra.
 * Especifica cantidad y precio pactado por cada producto en la orden.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_orden_compra")
public class DetalleOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @JsonBackReference("orden-detalles")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_orden_orden"))
    private OrdenCompra orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto",
            foreignKey = @ForeignKey(name = "fk_detalle_orden_producto"))
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "descripcion_producto")
    private String descripcionProducto;

    @Column(name = "cantidad_pedida", nullable = false)
    private Integer cantidadPedida;

    @Column(name = "precio_unitario_pactado", nullable = false)
    private Double precioUnitarioPactado;
}
