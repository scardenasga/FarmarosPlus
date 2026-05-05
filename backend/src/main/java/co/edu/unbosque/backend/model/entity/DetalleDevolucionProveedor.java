package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Línea de detalle de una devolución a proveedor.
 * Guarda snapshot de nombre y número de lote para trazabilidad.
 *
 * Detalle de una devolución al proveedor.
 * Especifica qué productos y cantidades se devuelven de un recibo.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_devolucion_proveedor")
public class DetalleDevolucionProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_devolucion")
    private Long idDetalleDevolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_devolucion", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_devolucion"))
    private DevolucionProveedor devolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_detalle_recepcion", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_detalle_recepcion"))
    private DetalleRecepcionCompra detalleRecepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto",
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_producto"))
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote",
            foreignKey = @ForeignKey(name = "fk_detalle_devolucion_lote"))
    private Lote lote;

    @Column(name = "cantidad_devuelta", nullable = false)
    private Integer cantidadDevuelta;

    @Column(name = "costo_unitario_referencia")
    private Double costoUnitarioReferencia;

    @Column(name = "observaciones")
    private String observaciones;
}
