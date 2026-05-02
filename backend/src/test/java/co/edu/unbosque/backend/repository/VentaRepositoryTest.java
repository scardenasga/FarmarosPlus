/*package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VentaRepositoryTest {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByEstadoOrderByFechaDesc() {
        Usuario u = new Usuario();
        u.setUsername("test");
        u.setNombreCompleto("Test");
        u.setRol("ADMIN");
        u.setEstado("ACTIVO");
        usuarioRepository.save(u);

        Venta v = new Venta();
        v.setUsuario(u);
        v.setEstado("COMPLETADA");
        v.setFecha(LocalDateTime.now());
        v.setTotal(1000.0);

        ventaRepository.save(v);

        List<Venta> resultado = ventaRepository.findByEstadoOrderByFechaDesc("COMPLETADA");

        assertFalse(resultado.isEmpty());
    }
}*/