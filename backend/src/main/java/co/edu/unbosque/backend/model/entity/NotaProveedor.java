package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que registra notas o alertas sobre un proveedor.
 * Permite documentar problemas, cambios o seguimiento.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "nota_proveedor")
public class NotaProveedor extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Long idNota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false,
            foreignKey = @ForeignKey(name = "fk_nota_proveedor"))
    private Proveedor proveedor;

    /**
     * Valores válidos: RETRASO | PRODUCTO_DEFECTUOSO | CAMBIO_PRECIO | CAMBIO_CONTACTO | OTRO
     */
    @Column(name = "tipo_nota", nullable = false)
    private String tipoNota;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_relacionada",
            foreignKey = @ForeignKey(name = "fk_nota_orden"))
    private OrdenCompra ordenRelacionada;
}
