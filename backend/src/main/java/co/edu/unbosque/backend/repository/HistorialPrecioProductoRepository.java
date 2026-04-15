package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.HistorialPrecioProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio del historial de cambios de precio y costo por producto.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface HistorialPrecioProductoRepository extends JpaRepository<HistorialPrecioProducto, Long> {

    /**
     * Recupera el historial de cambios de precio de un producto.
     *
     * @param idProducto identificador del producto
     * @return historial ordenado del más reciente al más antiguo
     */
    List<HistorialPrecioProducto> findByProducto_UniqueIDOrderByFechaCambioDesc(Long idProducto);

    /**
     * Recupera cambios de precio ocurridos en un rango de tiempo.
     *
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return registros del rango
     */
    List<HistorialPrecioProducto> findByFechaCambioBetweenOrderByFechaCambioDesc(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );

    /**
     * Recupera el último cambio de precio conocido para un producto.
     *
     * @param idProducto identificador del producto
     * @return último historial si existe
     */
    Optional<HistorialPrecioProducto> findTopByProducto_UniqueIDOrderByFechaCambioDesc(Long idProducto);
}
