package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Categoria;
import co.edu.unbosque.backend.model.request.CrearCategoriaRequest;
import co.edu.unbosque.backend.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void crearCategoria_debeNormalizarDescripcionYGuardar() {
        CrearCategoriaRequest request = new CrearCategoriaRequest("Analgesicos", "  Para   el dolor  ");

        when(categoriaRepository.existsByNombreIgnoreCase("Analgesicos")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resultado = categoriaService.crearCategoria(request);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        Categoria guardada = captor.getValue();

        assertEquals("Analgesicos", guardada.getNombre());
        assertEquals("Para el dolor", guardada.getDescripcion());
        assertEquals("Para el dolor", resultado.getDescripcion());
    }

    @Test
    void crearCategoria_cuandoNombreExiste_debeLanzarBusinessException() {
        when(categoriaRepository.existsByNombreIgnoreCase("Analgesicos")).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> categoriaService.crearCategoria(new CrearCategoriaRequest("Analgesicos", null))
        );
    }

    @Test
    void obtenerCategoriaPorId_inexistente_debeLanzarResourceNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoriaService.obtenerCategoriaPorId(99L));
    }

    @Test
    void crearCategoria_conDescripcionVacia_debeGuardarNull() {
        CrearCategoriaRequest request = new CrearCategoriaRequest("Vitaminas", "   ");

        when(categoriaRepository.existsByNombreIgnoreCase("Vitaminas")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resultado = categoriaService.crearCategoria(request);

        assertNull(resultado.getDescripcion());
    }
}
