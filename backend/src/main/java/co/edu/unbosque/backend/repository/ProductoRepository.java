package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Producto;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Producto}.
 * Centraliza consultas de catálogo e inventario a nivel de producto.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por código de barras.
     *
     * @param codigoBarras código de barras del producto
     * @return producto coincidente si existe
     */
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    /**
     * Busca un producto por código de barras ignorando mayúsculas.
     *
     * @param codigoBarras código de barras del producto
     * @return producto coincidente si existe
     */
    Optional<Producto> findByCodigoBarrasIgnoreCase(String codigoBarras);

    /**
     * Verifica si ya existe un producto con el mismo código de barras.
     *
     * @param codigoBarras código de barras a validar
     * @return {@code true} si ya existe un producto con ese código
     */
    boolean existsByCodigoBarras(String codigoBarras);

    /**
     * Verifica si ya existe un producto con el mismo código de barras ignorando mayúsculas.
     *
     * @param codigoBarras código de barras a validar
     * @return {@code true} si ya existe un producto con ese código
     */
    boolean existsByCodigoBarrasIgnoreCase(String codigoBarras);

    /**
     * Lista productos por estado ordenados alfabéticamente.
     *
     * @param estado estado funcional del producto
     * @return lista de productos filtrados
     */
    List<Producto> findByEstadoOrderByNombreAsc(String estado);

    /**
     * Lista productos pertenecientes a una categoría específica.
     *
     * @param idCategoria identificador de la categoría
     * @return productos asociados a la categoría
     */
    List<Producto> findByCategoria_IdCategoriaOrderByNombreAsc(Long idCategoria);

    /**
     * Busca productos por coincidencia parcial del nombre.
     *
     * @param nombre fragmento del nombre
     * @return productos cuyo nombre coincide
     */
    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    /**
     * Obtiene y bloquea un producto para actualización dentro de una transacción.
     *
     * @param id identificador del producto
     * @return producto bloqueado si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.uniqueID = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    /**
     * Obtiene un producto junto con su categoria para evitar inicializacion perezosa fuera de transaccion.
     *
     * @param id identificador del producto
     * @return producto con categoria cargada si existe
     */
    @EntityGraph(attributePaths = {"categoria"})
    @Query("SELECT p FROM Producto p WHERE p.uniqueID = :id")
    Optional<Producto> findByIdWithCategoria(@Param("id") Long id);

    /**
     * Obtiene solo los campos necesarios para el detalle del producto.
     * Evita hidratar columnas de auditoria que no se exponen en la respuesta.
     *
     * @param id identificador del producto
     * @return fila plana con datos de producto y categoria
     */
    @Query("""
            SELECT p.uniqueID AS productoId,
                   c.idCategoria AS categoriaId,
                   c.nombre AS categoriaNombre,
                   c.descripcion AS categoriaDescripcion,
                   p.nombre AS nombre,
                   p.descripcion AS descripcion,
                   p.codigoBarras AS codigoBarras,
                   p.stockMinimo AS stockMinimo,
                   p.stockActual AS stockActual,
                   p.costo AS costo,
                   p.precioVenta AS precioVenta,
                   p.margenGanancia AS margenGanancia,
                   p.porcentajeIva AS porcentajeIva,
                   p.requierePrescripcion AS requierePrescripcion,
                   p.estado AS estado
            FROM Producto p
            LEFT JOIN p.categoria c
            WHERE p.uniqueID = :id
            """)
    Optional<Tuple> findDetalleProductoRowById(@Param("id") Long id);

    /**
     * Obtiene y bloquea un producto por código de barras para actualización.
     *
     * @param codigoBarras código de barras del producto
     * @return producto bloqueado si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE LOWER(p.codigoBarras) = LOWER(:codigoBarras)")
    Optional<Producto> findByCodigoBarrasForUpdate(@Param("codigoBarras") String codigoBarras);

    /**
     * Verifica si una categoría tiene al menos un producto en el estado indicado.
     *
     * @param idCategoria identificador de la categoría
     * @param estado      estado del producto (ej: "ACTIVO")
     * @return {@code true} si existe al menos un producto con ese estado en la categoría
     */
    boolean existsByCategoria_IdCategoriaAndEstado(Long idCategoria, String estado);

    /**
     * Recupera productos que ya alcanzaron o cruzaron su stock mínimo.
     *
     * @return productos con stock bajo
     */
    @Query(value = """
            SELECT p.*
            FROM producto p
            WHERE p.stock_actual <= p.stock_minimo
            ORDER BY p.stock_actual ASC, p.nombre ASC
            """, nativeQuery = true)
    List<Producto> findProductosConStockBajo();

    /**
     * Busca productos activos por nombre o código de barras.
     *
     * @param termino criterio de búsqueda libre
     * @return productos activos coincidentes
     */
    @Query("""
            SELECT DISTINCT p
            FROM Producto p
            LEFT JOIN FETCH p.categoria
            WHERE p.estado = 'ACTIVO'
              AND (
                    LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))
                    OR LOWER(COALESCE(p.codigoBarras, '')) LIKE LOWER(CONCAT('%', :termino, '%'))
                  )
            ORDER BY p.nombre ASC
            """)
    List<Producto> buscarActivosPorNombreOCodigo(@Param("termino") String termino);
}
