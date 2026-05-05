package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que registra devoluciones de productos al proveedor.
 * Vincula una recepción y especifica motivos y tipo de devolución.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "devolucion_proveedor")
public class DevolucionProveedor extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Long idDevolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false,
            foreignKey = @ForeignKey(name = "fk_devolucion_proveedor"))
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_recepcion", nullable = false,
            foreignKey = @ForeignKey(name = "fk_devolucion_recepcion"))
    private RecepcionCompra recepcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false,
            foreignKey = @ForeignKey(name = "fk_devolucion_usuario"))
    private Usuario usuario;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion;

    /**
     * Valores válidos: INMEDIATA_RECEPCION | POSTERIOR
     */
    @Column(name = "tipo_devolucion", nullable = false)
    private String tipoDevolucion;

    /**
     * Valores válidos: VENCIMIENTO | DANADO | DEFECTO | ERROR_DESPACHO | RETIRO_SANITARIO | OTRO
     */
    @Column(name = "motivo_principal", nullable = false)
    private String motivoPrincipal;

    /**
     * Valores válidos: PENDIENTE | ENVIADA | ACEPTADA | RECHAZADA | CERRADA
     */
    @Column(name = "estado", nullable = false)
    private String estado = "PENDIENTE";

    @Column(name = "observaciones")
    private String observaciones;
}
