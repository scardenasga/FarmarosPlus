package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Alerta;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.response.AlertaResponse;
import co.edu.unbosque.backend.repository.AlertaGeneralRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para generación y gestión de alertas de inventario.
 *
 * @author juanjo2748
 */
@Service
public class AlertaService {

    private static final String TIPO_STOCK = "STOCK_MINIMO";
    private static final String TIPO_VENCIMIENTO = "PROXIMO_VENCIMIENTO";
    private static final int DIAS_PROXIMO_VENCIMIENTO = 30;

    private final AlertaGeneralRepository alertaRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    public AlertaService(AlertaGeneralRepository alertaRepository,
                         ProductoRepository productoRepository,
                         LoteRepository loteRepository) {
        this.alertaRepository = alertaRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    /**
     * Escanea productos y lotes, genera alertas nuevas evitando duplicados
     * y retorna todas las alertas no leídas.
     */
    @Transactional
    public List<AlertaResponse> generarAlertas() {
        LocalDateTime ahora = LocalDateTime.now();

        for (Producto p : productoRepository.findProductosConStockBajo()) {
            String ref = "producto:" + p.getUniqueID();
            if (!alertaRepository.existsByReferenciaAndLeidaFalse(ref)) {
                Alerta a = new Alerta();
                a.setTipo(TIPO_STOCK);
                a.setTitulo("Stock bajo: " + p.getNombre());
                a.setMensaje("Stock actual: " + p.getStockActual()
                        + " unidades. Mínimo requerido: " + p.getStockMinimo() + ".");
                a.setReferencia(ref);
                a.setLeida(false);
                a.setFechaGeneracion(ahora);
                alertaRepository.save(a);
            }
        }

        LocalDate fechaCorte = LocalDate.now().plusDays(DIAS_PROXIMO_VENCIMIENTO);
        for (Lote l : loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(
                LocalDate.now(), fechaCorte)) {
            String ref = "lote:" + l.getIdLote();
            if (!alertaRepository.existsByReferenciaAndLeidaFalse(ref)) {
                Alerta a = new Alerta();
                a.setTipo(TIPO_VENCIMIENTO);
                a.setTitulo("Vencimiento próximo: " + l.getProducto().getNombre());
                a.setMensaje("Lote " + l.getNumeroLote() + " vence el " + l.getFechaVencimiento() + ".");
                a.setReferencia(ref);
                a.setLeida(false);
                a.setFechaGeneracion(ahora);
                alertaRepository.save(a);
            }
        }

        return alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarAlertas(boolean soloNoLeidas) {
        List<Alerta> alertas = soloNoLeidas
                ? alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()
                : alertaRepository.findAllByOrderByFechaGeneracionDesc();
        return alertas.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void marcarComoLeida(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la alerta con id " + id));
        alerta.setLeida(true);
        alertaRepository.save(alerta);
    }

    @Transactional
    public void marcarTodasComoLeidas() {
        alertaRepository.marcarTodasComoLeidas();
    }

    private AlertaResponse toResponse(Alerta a) {
        return new AlertaResponse(a.getId(), a.getTipo(), a.getTitulo(),
                a.getMensaje(), a.isLeida(), a.getFechaGeneracion());
    }
}
