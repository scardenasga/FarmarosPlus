package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Lote}.
 * Gestiona consultas de disponibilidad y vencimiento por lote.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    /**
     * Lista los lotes de un producto ordenados por fecha de vencimiento.
     *
     * @param idProducto identificador del producto
     * @return lotes asociados al producto
     */
    List<Lote> findByProducto_UniqueIDOrderByFechaVencimientoAsc(Long idProducto);

    /**
     * Lista los lotes cuya fecha de vencimiento cae dentro de un rango dado.
     *
     * @param fechaInicio fecha inicial inclusiva
     * @param fechaFin fecha final inclusiva
     * @return lotes en el rango solicitado
     */
    List<Lote> findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Recupera el primer lote con stock disponible para un producto.
     *
     * @param idProducto identificador del producto
     * @param cantidad stock mínimo requerido
     * @return lote disponible más cercano a vencer
     */
    Optional<Lote> findFirstByProducto_UniqueIDAndCantidadGreaterThanOrderByFechaVencimientoAsc(Long idProducto, Integer cantidad);

    /**
     * Obtiene y bloquea un lote para actualización dentro de una transacción.
     *
     * @param id identificador del lote
     * @return lote bloqueado si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lote l JOIN FETCH l.producto WHERE l.idLote = :id")
    Optional<Lote> findByIdForUpdate(@Param("id") Long id);

    /**
     * Lista todos los lotes que todavía tienen stock disponible.
     *
     * @return lotes con cantidad positiva ordenados por vencimiento
     */
    @Query("""
            SELECT l
            FROM Lote l
            WHERE l.cantidad > 0
            ORDER BY l.fechaVencimiento ASC, l.numeroLote ASC
            """)
    List<Lote> findLotesDisponiblesOrdenadosPorVencimiento();

    /**
     * Lista los lotes disponibles de un producto ordenados por vencimiento.
     *
     * @param idProducto identificador del producto
     * @return lotes del producto con stock disponible
     */
    @Query("""
            SELECT l
            FROM Lote l
            WHERE l.producto.uniqueID = :idProducto
              AND l.cantidad > 0
            ORDER BY l.fechaVencimiento ASC, l.numeroLote ASC
            """)
    List<Lote> findLotesDisponiblesPorProducto(@Param("idProducto") Long idProducto);
}
