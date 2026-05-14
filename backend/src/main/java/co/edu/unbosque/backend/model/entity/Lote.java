package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad que representa un lote físico de inventario.
 * Permite controlar vencimiento y disponibilidad por lote.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "lote")
public class Lote extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long idLote;

    @Column(name = "numero_lote", unique = true)
    private String numeroLote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lote_producto"))
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor",
            foreignKey = @ForeignKey(name = "fk_lote_proveedor"))
    private Proveedor proveedor;
}
