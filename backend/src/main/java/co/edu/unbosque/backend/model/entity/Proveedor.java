package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un proveedor de productos.
 * Mantiene datos de contacto, identificación y condiciones comerciales.
 *
 * @author Sebastian Cardenas Garcia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "proveedor")
public class Proveedor extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long idProveedor;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "nit", unique = true)
    private String nit;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    @Column(name = "contacto")
    private String contacto;

    /**
     * Valores válidos: ACTIVO | INACTIVO
     */
    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";

    @Column(name = "condicion_pago")
    private String condicionPago;
}
