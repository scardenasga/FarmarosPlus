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
    @Column(name = "uniqueid")
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
     * Porcentaje de IVA aplicable al producto.
     * Medicamentos: 0.0 — Cosméticos/otros: 19.0
     */
    @Column(name = "porcentaje_iva", nullable = true)
    private Double porcentajeIva = 0.0;

    /**
     * Indica si el producto requiere prescripción médica.
     * true = requiere prescripción | false = no requiere
     */
    @Column(name = "requiere_prescripcion", nullable = true)
    private Boolean requierePrescripcion = false;

    /**
     * Modo de venta del producto: UNIDAD (solo suelto), PRESENTACION (solo blister/caja),
     * AMBAS (permite vender suelto o por presentación). Nullable por compatibilidad con datos existentes.
     */
    @Column(name = "unidad_venta", length = 20)
    private String unidadVenta = "UNIDAD";

    /**
     * Cuántas unidades base contiene una presentación (ej. 10 tabletas por blister, 14, 30).
     * Solo aplica cuando unidadVenta es PRESENTACION o AMBAS.
     */
    @Column(name = "unidades_por_presentacion")
    private Integer unidadesPorPresentacion;

    /**
     * Precio de la presentación completa. Debe usarse con unidadesPorPresentacion.
     */
    @Column(name = "precio_presentacion")
    private Double precioPresentacion;

    /**
     * Valores válidos: ACTIVO | INACTIVO | DESCONTINUADO
     * Validar con @Pattern en el DTO correspondiente.
     */
    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";

    /**
     * Ruta relativa o nombre del archivo de imagen del producto.
     * Null si no tiene imagen. Se almacena en filesystem (uploads/productos).
     * En SQLite3: TEXT nullable, sin FK.
     */
    @Column(name = "imagen_url")
    private String imagenUrl;
}
