package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Línea de detalle de una devolución a proveedor.
 * Registra el producto, el lote (opcional) y la cantidad devuelta.
 * Inmutable una vez creada, por eso no extiende Auditable.
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
            foreignKey = @ForeignKey(name = "fk_detalle_dev_devolucion"))
    private DevolucionProveedor devolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_dev_producto"))
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto; // snapshot por si el producto se elimina

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote",
            foreignKey = @ForeignKey(name = "fk_detalle_dev_lote"))
    private Lote lote;

    @Column(name = "numero_lote")
    private String numeroLote; // snapshot

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "motivo")
    private String motivo;
}
