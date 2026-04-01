package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.AuditoriaSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la bitácora técnica del sistema.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface AuditoriaSistemaRepository extends JpaRepository<AuditoriaSistema, Long> {

    /**
     * Recupera eventos técnicos asociados a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return eventos ordenados por fecha descendente
     */
    List<AuditoriaSistema> findByUsuario_IdUsuarioOrderByFechaHoraDesc(Long idUsuario);

    /**
     * Recupera eventos por tipo de acción.
     *
     * @param accion acción auditada
     * @return eventos filtrados
     */
    List<AuditoriaSistema> findByAccionOrderByFechaHoraDesc(String accion);

    /**
     * Recupera eventos técnicos dentro de un rango temporal.
     *
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return eventos del rango
     */
    List<AuditoriaSistema> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
