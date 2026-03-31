package co.edu.unbosque.backend.model.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false,
            foreignKey = @ForeignKey(name = "fk_producto_categoria"))
    @NotNull(message = "La categoria es obligatoria")
    private Categoria categoria;

    @Column(name = "nombre", nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "codigo_barras", unique = true)
    private String codigoBarras;

    @Column(name = "stock_minimo")
    @PositiveOrZero(message = "El stock minimo no puede ser negativo")
    private Integer stockMinimo = 0;

    @Column(name = "stock_actual")
    @PositiveOrZero(message = "El stock actual no puede ser negativo")
    private Integer stockActual = 0;

    @Column(name = "costo", nullable = false)
    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
    private Double costo = 0.0;

    @Column(name = "precio_venta", nullable = false)
    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de venta no puede ser negativo")
    private Double precioVenta = 0.0;

    @Column(name = "margen_ganancia")
    private Double margenGanancia;

    /**
     * Valores válidos: ACTIVO | INACTIVO | DESCONTINUADO
     * Validar con @Pattern en el DTO correspondiente.
     */
    @Column(name = "estado", nullable = false)
    @NotBlank(message = "El estado es obligatorio")
    private String estado = "ACTIVO";
}
