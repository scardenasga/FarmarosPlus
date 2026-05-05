package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.Alerta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/alerta-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AlertaRepositoryTest {

    @Autowired
    private AlertaGeneralRepository alertaRepository;

    private Alerta buildAlerta(String referencia, boolean leida) {
        Alerta a = new Alerta();
        a.setTipo("STOCK_MINIMO");
        a.setTitulo("Titulo");
        a.setMensaje("Mensaje");
        a.setReferencia(referencia);
        a.setLeida(leida);
        a.setFechaGeneracion(LocalDateTime.now());
        return a;
    }

    @Test
    void existsByReferenciaAndLeidaFalse_cuandoExisteNoLeida_debeRetornarTrue() {
        alertaRepository.save(buildAlerta("producto:1", false));

        assertTrue(alertaRepository.existsByReferenciaAndLeidaFalse("producto:1"));
    }

    @Test
    void existsByReferenciaAndLeidaFalse_cuandoEstaLeida_debeRetornarFalse() {
        alertaRepository.save(buildAlerta("producto:2", true));

        assertFalse(alertaRepository.existsByReferenciaAndLeidaFalse("producto:2"));
    }

    @Test
    void findByLeidaFalseOrderByFechaGeneracionDesc_debeRetornarSoloNoLeidas() {
        alertaRepository.save(buildAlerta("producto:3", false));
        alertaRepository.save(buildAlerta("producto:4", true));

        List<Alerta> resultado = alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc();

        assertEquals(1, resultado.size());
        assertFalse(resultado.getFirst().isLeida());
    }

    @Test
    void marcarTodasComoLeidas_debeActualizarTodasLasNoLeidas() {
        alertaRepository.save(buildAlerta("producto:5", false));
        alertaRepository.save(buildAlerta("producto:6", false));

        alertaRepository.marcarTodasComoLeidas();

        List<Alerta> noLeidas = alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc();
        assertTrue(noLeidas.isEmpty());
    }
}
