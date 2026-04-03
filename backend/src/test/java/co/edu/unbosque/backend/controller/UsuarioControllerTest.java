package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_debeRetornar201() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("sebas");
        usuario.setNombreCompleto("Sebastian Cardenas Garcia");
        usuario.setRol("ADMIN");
        usuario.setEstado("ACTIVO");

        when(usuarioService.crearUsuario(org.mockito.ArgumentMatchers.any())).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "sebas",
                                  "passwordHash": "clave123",
                                  "nombreCompleto": "Sebastian Cardenas Garcia",
                                  "rol": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("sebas"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void listarUsuarios_debeRetornar200YLista() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(2L);
        usuario.setUsername("karen");
        usuario.setNombreCompleto("Karen Lopez");
        usuario.setRol("VENDEDOR");
        usuario.setEstado("ACTIVO");

        when(usuarioService.listarUsuarios("karen", null, null)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios").param("q", "karen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("karen"));
    }

    @Test
    void obtenerUsuario_inexistente_debeRetornar404() throws Exception {
        when(usuarioService.obtenerUsuarioPorId(99L))
                .thenThrow(new ResourceNotFoundException("No existe el usuario con id 99"));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el usuario con id 99"));
    }

    @Test
    void actualizarEstado_requestInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(patch("/api/usuarios/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "PAUSADO"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
