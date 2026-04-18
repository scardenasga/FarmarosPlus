package co.edu.unbosque.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuración global de alertas de inventario (singleton — siempre id=1).
 *
 * @author juanjo2748
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configuracion_alerta")
public class ConfiguracionAlerta {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "dias_proximo_vencimiento", nullable = false)
    private int diasProximoVencimiento = 30;
}
