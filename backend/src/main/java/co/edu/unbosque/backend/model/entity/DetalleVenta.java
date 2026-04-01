package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla de detalle inmutable. Una vez creada la venta, las líneas no se modifican.
 * Si la venta se anula, el estado cambia en Venta y el servicio revierte el stock,
 * pero las líneas quedan como registro histórico intacto.
 * Por eso no necesita auditoría JPA.
 * Cada fila vincula un producto y un lote específico dentro de una venta.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_venta"))
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lote", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_lote"))
    private Lote lote;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad; // CHECK (cantidad > 0) — validar en DTO

    @Column(name = "precio_unitario_aplicado", nullable = false)
    private Double precioUnitarioAplicado;

    @Column(name = "subtotal_linea", nullable = false)
    private Double subtotalLinea;
}
