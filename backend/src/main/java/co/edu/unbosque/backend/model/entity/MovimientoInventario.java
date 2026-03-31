package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Tabla de solo inserción: registra cada movimiento de stock.
 * No se modifica una vez creada, por eso no extiende Auditable.
 * Solo usa @CreatedDate para fecha_movimiento.
 * usuario_responsable se asigna explícitamente desde el servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    // Nullable: ON DELETE SET NULL en el esquema SQL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto",
            foreignKey = @ForeignKey(name = "fk_movimiento_producto"))
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto; // Snapshot por si el producto se elimina

    // Opcional: no todos los movimientos están ligados a un lote
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote",
            foreignKey = @ForeignKey(name = "fk_movimiento_lote"))
    private Lote lote;

    /**
     * Valores válidos: COMPRA | VENTA | AJUSTE | MERMA | ROBO | DEVOLUCION | VENCIMIENTO
     */
    @Column(name = "tipo_movimiento", nullable = false)
    private String tipoMovimiento;

    @Column(name = "cantidad_anterior", nullable = false)
    private Integer cantidadAnterior;

    @Column(name = "cantidad_nueva", nullable = false)
    private Integer cantidadNueva;

    @Column(name = "diferencia", nullable = false)
    private Integer diferencia; // Positivo = entrada, Negativo = salida

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "referencia_documento")
    private String referenciaDocumento; // ID de venta o compra relacionada

    @Column(name = "usuario_responsable", nullable = false)
    private String usuarioResponsable; // Se asigna desde el servicio

    @CreatedDate
    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;
}
