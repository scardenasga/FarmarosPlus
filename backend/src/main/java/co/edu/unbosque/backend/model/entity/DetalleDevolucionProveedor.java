package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Línea de detalle de una devolución a proveedor.
 * Guarda snapshot de nombre y número de lote para trazabilidad.
 *
 * @author juanjo2748
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_devolucion_proveedor")
public class DetalleDevolucionProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_devolucion", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion"))
    private DevolucionProveedor devolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote",
            foreignKey = @ForeignKey(name = "fk_detalle_lote"))
    private Lote lote;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "numero_lote")
    private String numeroLote;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
