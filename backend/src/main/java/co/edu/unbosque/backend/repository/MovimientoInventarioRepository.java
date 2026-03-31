package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para consultar el histórico de movimientos de inventario.
 * La tabla es de solo inserción y sirve como bitácora operativa.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Recupera los movimientos asociados a un producto.
     *
     * @param idProducto identificador del producto
     * @return movimientos ordenados del más reciente al más antiguo
     */
    List<MovimientoInventario> findByProducto_UniqueIDOrderByFechaMovimientoDesc(Long idProducto);

    /**
     * Recupera los movimientos asociados a un lote.
     *
     * @param idLote identificador del lote
     * @return movimientos ordenados del más reciente al más antiguo
     */
    List<MovimientoInventario> findByLote_IdLoteOrderByFechaMovimientoDesc(Long idLote);

    /**
     * Recupera movimientos por tipo de operación.
     *
     * @param tipoMovimiento tipo de movimiento
     * @return movimientos filtrados
     */
    List<MovimientoInventario> findByTipoMovimientoOrderByFechaMovimientoDesc(String tipoMovimiento);

    /**
     * Recupera movimientos ligados a una referencia documental.
     *
     * @param referenciaDocumento identificador de documento funcional
     * @return movimientos relacionados
     */
    List<MovimientoInventario> findByReferenciaDocumentoOrderByFechaMovimientoDesc(String referenciaDocumento);

    /**
     * Lista movimientos ocurridos en un rango de tiempo.
     *
     * @param fechaInicio inicio inclusivo del rango
     * @param fechaFin fin inclusivo del rango
     * @return movimientos en el rango indicado
     */
    List<MovimientoInventario> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}
