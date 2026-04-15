package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/usuario-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByUsernameIgnoreCase_debeEncontrarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("Sebas");
        usuario.setPasswordHash("hash");
        usuario.setNombreCompleto("Sebastian Cardenas Garcia");
        usuario.setRol("ADMIN");
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);

        var resultado = usuarioRepository.findByUsernameIgnoreCase("sebas");

        assertTrue(resultado.isPresent());
        assertEquals("Sebas", resultado.get().getUsername());
    }

    @Test
    void buscarPorUsernameONombre_debeRetornarCoincidencias() {
        Usuario usuario = new Usuario();
        usuario.setUsername("karen");
        usuario.setPasswordHash("hash");
        usuario.setNombreCompleto("Karen Lopez");
        usuario.setRol("VENDEDOR");
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);

        List<Usuario> resultado = usuarioRepository.buscarPorUsernameONombre("Karen");

        assertEquals(1, resultado.size());
        assertEquals("karen", resultado.getFirst().getUsername());
    }
}
