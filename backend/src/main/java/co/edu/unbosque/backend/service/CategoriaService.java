package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.request.CrearCategoriaRequest;
import co.edu.unbosque.backend.repository.CategoriaRepository;
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

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Crea una categoria nueva validando unicidad por nombre.
     *
     * @param request datos de la categoria
     * @return categoria creada
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
     * Recupera una categoria por id.
     *
     * @param categoriaId id de la categoria
     * @return categoria encontrada
     */
    @Transactional(readOnly = true)
    public Categoria obtenerCategoriaPorId(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoria con id " + categoriaId));
    }

    /**
     * Lista categorias existentes.
     *
     * @return categorias ordenadas por nombre
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
