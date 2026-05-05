package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.request.ActualizarCategoriaRequest;
import co.edu.unbosque.backend.model.request.CrearCategoriaRequest;
import co.edu.unbosque.backend.repository.CategoriaRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    // ── crear ────────────────────────────────────────────────────────────────

    @Test
    void crearCategoria_debeNormalizarDescripcionYGuardar() {
        CrearCategoriaRequest request = new CrearCategoriaRequest("Analgesicos", "  Para   el dolor  ");

        when(categoriaRepository.existsByNombreIgnoreCase("Analgesicos")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        Categoria resultado = categoriaService.crearCategoria(request);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertEquals("Analgesicos", captor.getValue().getNombre());
        assertEquals("Para el dolor", resultado.getDescripcion());
    }

    @Test
    void crearCategoria_cuandoNombreExiste_debeLanzarBusinessException() {
        when(categoriaRepository.existsByNombreIgnoreCase("Analgesicos")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> categoriaService.crearCategoria(new CrearCategoriaRequest("Analgesicos", null)));
    }

    @Test
    void crearCategoria_conDescripcionVacia_debeGuardarNull() {
        when(categoriaRepository.existsByNombreIgnoreCase("Vitaminas")).thenReturn(false);
        when(categoriaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Categoria resultado = categoriaService.crearCategoria(new CrearCategoriaRequest("Vitaminas", "   "));

        assertNull(resultado.getDescripcion());
    }

    // ── actualizar ───────────────────────────────────────────────────────────

    @Test
    void actualizarCategoria_debeActualizarNombreYDescripcion() {
        Categoria existente = new Categoria();
        existente.setIdCategoria(1L);
        existente.setNombre("Antibioticos");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findByNombreIgnoreCase("Analgesicos")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Categoria resultado = categoriaService.actualizarCategoria(1L,
                new ActualizarCategoriaRequest("Analgesicos", "Para el dolor"));

        assertEquals("Analgesicos", resultado.getNombre());
        assertEquals("Para el dolor", resultado.getDescripcion());
    }

    @Test
    void actualizarCategoria_cuandoNombrePertenecaAOtra_debeLanzarBusinessException() {
        Categoria categoriaActual = new Categoria();
        categoriaActual.setIdCategoria(1L);
        categoriaActual.setNombre("Antibioticos");

        Categoria otraCategoria = new Categoria();
        otraCategoria.setIdCategoria(2L);
        otraCategoria.setNombre("Analgesicos");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaActual));
        when(categoriaRepository.findByNombreIgnoreCase("Analgesicos")).thenReturn(Optional.of(otraCategoria));

        assertThrows(BusinessException.class,
                () -> categoriaService.actualizarCategoria(1L,
                        new ActualizarCategoriaRequest("Analgesicos", null)));
    }

    // ── eliminar ─────────────────────────────────────────────────────────────

    @Test
    void eliminarCategoria_sinProductosActivos_debeEliminar() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(5L);
        categoria.setNombre("Vitaminas");

        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoria_IdCategoriaAndEstado(5L, "ACTIVO")).thenReturn(false);

        categoriaService.eliminarCategoria(5L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminarCategoria_conProductosActivos_debeLanzarBusinessException() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(5L);
        categoria.setNombre("Vitaminas");

        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoria_IdCategoriaAndEstado(5L, "ACTIVO")).thenReturn(true);

        assertThrows(BusinessException.class, () -> categoriaService.eliminarCategoria(5L));
        verify(categoriaRepository, never()).delete(any());
    }

    // ── obtener ──────────────────────────────────────────────────────────────

    @Test
    void obtenerCategoriaPorId_inexistente_debeLanzarResourceNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoriaService.obtenerCategoriaPorId(99L));
    }
}
