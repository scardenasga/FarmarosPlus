package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Alerta;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.response.AlertaResponse;
import co.edu.unbosque.backend.repository.AlertaGeneralRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock private AlertaGeneralRepository alertaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private LoteRepository loteRepository;

    @InjectMocks
    private AlertaService alertaService;

    // ── generarAlertas ───────────────────────────────────────────────────────

    @Test
    void generarAlertas_conProductoStockBajo_debeCrearAlertaStockMinimo() {
        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Amoxicilina");
        producto.setStockActual(2);
        producto.setStockMinimo(10);

        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(producto));
        when(loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(any(), any()))
                .thenReturn(List.of());
        when(alertaRepository.existsByReferenciaAndLeidaFalse("producto:1")).thenReturn(false);
        when(alertaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()).thenReturn(List.of());

        alertaService.generarAlertas();

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        Alerta guardada = captor.getValue();
        assertEquals("STOCK_MINIMO", guardada.getTipo());
        assertEquals("producto:1", guardada.getReferencia());
        assertFalse(guardada.isLeida());
    }

    @Test
    void generarAlertas_cuandoAlertaNoLeidaYaExiste_debeOmitirDuplicado() {
        Producto producto = new Producto();
        producto.setUniqueID(1L);
        producto.setNombre("Amoxicilina");
        producto.setStockActual(2);
        producto.setStockMinimo(10);

        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(producto));
        when(loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(any(), any()))
                .thenReturn(List.of());
        when(alertaRepository.existsByReferenciaAndLeidaFalse("producto:1")).thenReturn(true);
        when(alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()).thenReturn(List.of());

        alertaService.generarAlertas();

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void generarAlertas_conLoteProximoVencer_debeCrearAlertaVencimiento() {
        Producto producto = new Producto();
        producto.setUniqueID(2L);
        producto.setNombre("Ibuprofeno");

        Lote lote = new Lote();
        lote.setIdLote(10L);
        lote.setNumeroLote("L001");
        lote.setFechaVencimiento(LocalDate.now().plusDays(15));
        lote.setProducto(producto);

        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of());
        when(loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(any(), any()))
                .thenReturn(List.of(lote));
        when(alertaRepository.existsByReferenciaAndLeidaFalse("lote:10")).thenReturn(false);
        when(alertaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()).thenReturn(List.of());

        alertaService.generarAlertas();

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals("PROXIMO_VENCIMIENTO", captor.getValue().getTipo());
        assertEquals("lote:10", captor.getValue().getReferencia());
    }

    // ── marcarComoLeida ──────────────────────────────────────────────────────

    @Test
    void marcarComoLeida_debeActualizarCampoLeida() {
        Alerta alerta = new Alerta();
        alerta.setId(1L);
        alerta.setLeida(false);

        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        alertaService.marcarComoLeida(1L);

        assertTrue(alerta.isLeida());
        verify(alertaRepository).save(alerta);
    }

    @Test
    void marcarComoLeida_inexistente_debeLanzarResourceNotFoundException() {
        when(alertaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alertaService.marcarComoLeida(99L));
    }

    // ── listarAlertas ────────────────────────────────────────────────────────

    @Test
    void listarAlertas_soloNoLeidas_debeUsarRepositorioFiltrado() {
        when(alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()).thenReturn(List.of());

        alertaService.listarAlertas(true);

        verify(alertaRepository).findByLeidaFalseOrderByFechaGeneracionDesc();
        verify(alertaRepository, never()).findAllByOrderByFechaGeneracionDesc();
    }

    @Test
    void listarAlertas_todas_debeUsarRepositorioCompleto() {
        when(alertaRepository.findAllByOrderByFechaGeneracionDesc()).thenReturn(List.of());

        alertaService.listarAlertas(false);

        verify(alertaRepository).findAllByOrderByFechaGeneracionDesc();
        verify(alertaRepository, never()).findByLeidaFalseOrderByFechaGeneracionDesc();
    }
}
