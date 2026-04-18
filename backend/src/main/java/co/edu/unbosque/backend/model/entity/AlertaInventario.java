package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Alerta generada automáticamente por stock bajo o lote próximo a vencer.
 *
 * @author juanjo2748
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alerta_inventario")
public class AlertaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    /** STOCK_MINIMO | PROXIMO_VENCIMIENTO */
    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "id_lote")
    private Long idLote;

    @Column(name = "numero_lote")
    private String numeroLote;

    @Column(name = "cantidad_actual")
    private Integer cantidadActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "leida", nullable = false)
    private boolean leida = false;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;
}
