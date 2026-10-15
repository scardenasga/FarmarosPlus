package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Línea de detalle de una devolución a cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_devolucion_cliente")
public class DetalleDevolucionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_devolucion_cliente")
    private Long idDetalleDevolucionCliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_devolucion_cliente", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_cliente_devolucion"))
    private DevolucionCliente devolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_detalle_venta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_cliente_detalle_venta"))
    private DetalleVenta detalleVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_cliente_producto"))
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote",
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_cliente_lote"))
    private Lote lote;

    @Column(name = "cantidad_devuelta", nullable = false)
    private Integer cantidad;

    @Column(name = "nombre_producto")
    private String nombreProducto;

    @Column(name = "numero_lote")
    private String numeroLote;
}
