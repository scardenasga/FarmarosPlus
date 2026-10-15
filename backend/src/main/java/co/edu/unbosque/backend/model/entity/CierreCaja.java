package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Snapshot de cierre de caja. Guarda el resumen de ventas de un periodo
 * para auditoría y cuadre. No interfiere con el flujo de ventas existente.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "cierre_caja")
public class CierreCaja extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDateTime fechaCierre;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "monto_inicial", nullable = false)
    private Double montoInicial = 0.0;

    @Column(name = "monto_declarado")
    private Double montoDeclarado;

    @Column(name = "total_efectivo", nullable = false)
    private Double totalEfectivo = 0.0;

    @Column(name = "total_tarjeta", nullable = false)
    private Double totalTarjeta = 0.0;

    @Column(name = "total_transferencia", nullable = false)
    private Double totalTransferencia = 0.0;

    @Column(name = "total_ventas", nullable = false)
    private Double totalVentas = 0.0;

    @Column(name = "total_descuentos", nullable = false)
    private Double totalDescuentos = 0.0;

    @Column(name = "total_iva", nullable = false)
    private Double totalIva = 0.0;

    @Column(name = "cantidad_ventas", nullable = false)
    private Integer cantidadVentas = 0;

    @Column(name = "cantidad_anuladas", nullable = false)
    private Integer cantidadAnuladas = 0;

    @Column(name = "ganancia_estimada", nullable = false)
    private Double gananciaEstimada = 0.0;

    @Column(name = "diferencia")
    private Double diferencia;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "CERRADO";
}
