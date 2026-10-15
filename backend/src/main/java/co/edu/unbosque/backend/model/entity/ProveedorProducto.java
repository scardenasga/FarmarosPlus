package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla de relación muchos-a-muchos entre Proveedor y Producto.
 * Permite asociar múltiples proveedores a un producto con referencias específicas.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proveedor_producto", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_proveedor", "id_producto"})
})
public class ProveedorProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor_producto")
    private Long idProveedorProducto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false,
            foreignKey = @ForeignKey(name = "fk_proveedor_producto_proveedor"))
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false,
            foreignKey = @ForeignKey(name = "fk_proveedor_producto_producto"))
    private Producto producto;

    @Column(name = "codigo_producto_proveedor")
    private String codigoProductoProveedor;

    @Column(name = "precio_referencia")
    private Double precioReferencia;

    /**
     * Valores válidos: ACTIVO | INACTIVO
     */
    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";
}
