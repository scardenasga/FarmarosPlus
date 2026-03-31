package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pago_venta")
public class PagoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pago_venta"))
    private Venta venta;

    /**
     * Valores válidos: EFECTIVO | TARJETA — validar en DTO
     */
    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "monto", nullable = false)
    private Double monto; // CHECK (monto > 0) — validar en DTO
}
