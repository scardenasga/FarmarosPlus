package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad de catálogo para productos farmacéuticos o comerciales.
 * Mantiene datos maestros y stock agregado a nivel de producto.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "producto")
public class Producto extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UniqueID")
    private Long uniqueID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria",
            foreignKey = @ForeignKey(name = "fk_producto_categoria"))
    private Categoria categoria;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "codigo_barras", unique = true)
    private String codigoBarras;

    @Column(name = "stock_minimo")
    private Integer stockMinimo = 0;

    @Column(name = "stock_actual")
    private Integer stockActual = 0;

    @Column(name = "costo", nullable = false)
    private Double costo = 0.0;

    @Column(name = "precio_venta", nullable = false)
    private Double precioVenta = 0.0;

    @Column(name = "margen_ganancia")
    private Double margenGanancia;

    /**
     * Valores válidos: ACTIVO | INACTIVO | DESCONTINUADO
     * Validar con @Pattern en el DTO correspondiente.
     */
    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";
}
