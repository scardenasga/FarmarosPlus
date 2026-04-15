package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.request.CrearUsuarioRequest;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_debeHashearClaveYAsignarEstadoActivoPorDefecto() {
        CrearUsuarioRequest request = new CrearUsuarioRequest(
                "sebas",
                "clave123",
                "  Sebastian   Cardenas Garcia  ",
                "VENDEDOR",
                null
        );

        when(usuarioRepository.existsByUsernameIgnoreCase("sebas")).thenReturn(false);
        when(passwordEncoder.encode("clave123")).thenReturn("HASHED");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.crearUsuario(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertEquals("sebas", guardado.getUsername());
        assertEquals("HASHED", guardado.getPasswordHash());
        assertEquals("Sebastian Cardenas Garcia", guardado.getNombreCompleto());
        assertEquals("ACTIVO", guardado.getEstado());
        assertEquals("HASHED", resultado.getPasswordHash());
    }

    @Test
    void crearUsuario_cuandoUsernameExiste_debeLanzarBusinessException() {
        CrearUsuarioRequest request = new CrearUsuarioRequest(
                "sebas",
                "clave123",
                "Sebastian Cardenas Garcia",
                "VENDEDOR",
                "ACTIVO"
        );

        when(usuarioRepository.existsByUsernameIgnoreCase("sebas")).thenReturn(true);

        assertThrows(BusinessException.class, () -> usuarioService.crearUsuario(request));
    }

    @Test
    void listarUsuarios_conBusquedaNumerica_debeRetornarUsuarioPorId() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setUsername("karen");

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

        var resultado = usuarioService.listarUsuarios("7", null, null);

        assertEquals(1, resultado.size());
        assertEquals("karen", resultado.getFirst().getUsername());
    }

    @Test
    void actualizarEstado_debeNormalizarElNuevoEstado() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(3L);
        usuario.setEstado("ACTIVO");

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.actualizarEstado(3L, "inactivo");

        assertEquals("INACTIVO", resultado.getEstado());
    }
}
