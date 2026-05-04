package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/venta-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VentaRepositoryTest {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByEstadoOrderByFechaDesc_debeRetornarVentasFiltradas() {
        Usuario usuario = new Usuario();
        usuario.setUsername("testuser");
        usuario.setPasswordHash("hash");
        usuario.setNombreCompleto("Test User");
        usuario.setRol("ADMIN");
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setEstado("COMPLETADA");
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(1000.0);
        ventaRepository.save(venta);

        List<Venta> resultado = ventaRepository.findByEstadoOrderByFechaDesc("COMPLETADA");

        assertFalse(resultado.isEmpty());
        assertEquals("COMPLETADA", resultado.getFirst().getEstado());
    }

    @Test
    void findByUsuario_IdUsuarioOrderByFechaDesc_debeRetornarVentasDelUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("vendedor1");
        usuario.setPasswordHash("hash");
        usuario.setNombreCompleto("Vendedor Uno");
        usuario.setRol("VENDEDOR");
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setEstado("COMPLETADA");
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(5000.0);
        ventaRepository.save(venta);

        List<Venta> resultado = ventaRepository.findByUsuario_IdUsuarioOrderByFechaDesc(usuario.getIdUsuario());

        assertFalse(resultado.isEmpty());
        assertEquals(usuario.getIdUsuario(), resultado.getFirst().getUsuario().getIdUsuario());
    }
}