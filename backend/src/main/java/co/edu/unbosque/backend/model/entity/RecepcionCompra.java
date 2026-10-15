package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Entidad que registra la recepción de una orden de compra.
 * Incluye estado de recepción y estado de pago.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"detalles"})
@ToString(exclude = {"detalles"})
@Entity
@Table(name = "recepcion_compra")
public class RecepcionCompra extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion")
    private Long idRecepcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recepcion_orden"))
    private OrdenCompra orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recepcion_usuario"))
    private Usuario usuario;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    @Column(name = "observaciones")
    private String observaciones;

    /**
     * Valores válidos: PARCIAL | COMPLETA | RECHAZADA
     */
    @Column(name = "estado", nullable = false)
    private String estado = "PARCIAL";

    @Column(name = "total_recepcion", nullable = false)
    private Double totalRecepcion = 0.0;

    /**
     * Valores válidos: PENDIENTE | PARCIAL | PAGADO
     */
    @Column(name = "estado_pago", nullable = false)
    private String estadoPago = "PENDIENTE";

    @Column(name = "monto_pagado", nullable = false)
    private Double montoPagado = 0.0;

    @Column(name = "fecha_limite_pago")
    private LocalDateTime fechaLimitePago;

    @JsonManagedReference("recepcion-detalles")
    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DetalleRecepcionCompra> detalles = new HashSet<>();
}
