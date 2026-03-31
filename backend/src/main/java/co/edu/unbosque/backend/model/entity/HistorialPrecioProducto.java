package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Tabla de solo inserción: registra cada cambio de precio de un producto.
 * No se modifica una vez creada, por eso no extiende Auditable.
 * Solo usa @CreatedDate para fecha_cambio.
 * usuario_responsable se asigna explícitamente desde el servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "historial_precio_producto")
public class HistorialPrecioProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    // Nullable: ON DELETE SET NULL en el esquema SQL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto",
            foreignKey = @ForeignKey(name = "fk_historial_precio_producto"))
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto; // Snapshot del nombre al momento del cambio

    @Column(name = "precio_anterior", nullable = false)
    private Double precioAnterior;

    @Column(name = "precio_nuevo", nullable = false)
    private Double precioNuevo;

    @Column(name = "costo_anterior")
    private Double costoAnterior;

    @Column(name = "costo_nuevo")
    private Double costoNuevo;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "usuario_responsable", nullable = false)
    private String usuarioResponsable; // Se asigna desde el servicio

    @CreatedDate
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;
}
