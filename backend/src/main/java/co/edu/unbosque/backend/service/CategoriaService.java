package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.request.ActualizarCategoriaRequest;
import co.edu.unbosque.backend.model.request.CrearCategoriaRequest;
import co.edu.unbosque.backend.repository.CategoriaRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicacion para categorias de producto.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Crea una categoria nueva validando unicidad por nombre.
     */
    @Transactional
    public Categoria crearCategoria(CrearCategoriaRequest request) {
        String nombreNormalizado = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new BusinessException("Ya existe una categoria con nombre " + request.nombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreNormalizado);
        categoria.setDescripcion(normalizarTexto(request.descripcion()));
        return categoriaRepository.save(categoria);
    }

    /**
     * Actualiza el nombre y descripción de una categoria existente.
     */
    @Transactional
    public Categoria actualizarCategoria(Long id, ActualizarCategoriaRequest request) {
        Categoria categoria = obtenerCategoriaPorId(id);
        String nombreNormalizado = request.nombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombreNormalizado)
                .filter(c -> !c.getIdCategoria().equals(id))
                .ifPresent(c -> {
                    throw new BusinessException("Ya existe una categoria con nombre " + nombreNormalizado);
                });

        categoria.setNombre(nombreNormalizado);
        categoria.setDescripcion(normalizarTexto(request.descripcion()));
        return categoriaRepository.save(categoria);
    }

    /**
     * Elimina una categoria solo si no tiene productos activos asociados.
     */
    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = obtenerCategoriaPorId(id);
        if (productoRepository.existsByCategoria_IdCategoriaAndEstado(id, "ACTIVO")) {
            throw new BusinessException(
                    "No se puede eliminar la categoría \"" + categoria.getNombre()
                    + "\" porque tiene productos activos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    /**
     * Recupera una categoria por id.
     */
    @Transactional(readOnly = true)
    public Categoria obtenerCategoriaPorId(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoria con id " + categoriaId));
    }

    /**
     * Lista categorias existentes ordenadas por nombre.
     */
    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"));
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim().replaceAll("\\s+", " ");
    }
}
