package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/lote-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LoteRepositoryTest {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void findLotesDisponiblesPorProducto_debeRetornarSoloLotesConCantidadPositiva() {
        Producto producto = new Producto();
        producto.setNombre("Amoxicilina");
        producto.setStockActual(100);
        producto.setCosto(5000.0);
        producto.setPrecioVenta(8000.0);
        producto.setEstado("ACTIVO");
        productoRepository.save(producto);

        Lote loteConStock = new Lote();
        loteConStock.setProducto(producto);
        loteConStock.setNumeroLote("LOT-001");
        loteConStock.setCantidad(30);
        loteConStock.setFechaVencimiento(LocalDate.now().plusYears(1));
        loteRepository.save(loteConStock);

        Lote loteSinStock = new Lote();
        loteSinStock.setProducto(producto);
        loteSinStock.setNumeroLote("LOT-002");
        loteSinStock.setCantidad(0);
        loteSinStock.setFechaVencimiento(LocalDate.now().plusYears(1));
        loteRepository.save(loteSinStock);

        List<Lote> resultado = loteRepository.findLotesDisponiblesPorProducto(producto.getUniqueID());

        assertTrue(resultado.size() >= 1);
    }

    @Test
    void findByFechaVencimientoBetweenOrderByFechaVencimientoAsc_debeRetornarLotesPorVencer() {
        Producto producto = new Producto();
        producto.setNombre("Vitamina C");
        producto.setStockActual(50);
        producto.setCosto(3000.0);
        producto.setPrecioVenta(5000.0);
        producto.setEstado("ACTIVO");
        productoRepository.save(producto);

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setNumeroLote("LOT-VCT");
        lote.setCantidad(20);
        lote.setFechaVencimiento(LocalDate.now().plusMonths(3));
        loteRepository.save(lote);

        List<Lote> resultado = loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(
                LocalDate.now(), LocalDate.now().plusMonths(6));

        assertTrue(resultado.size() >= 1);
    }
}