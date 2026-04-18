package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.AlertaInventario;
import co.edu.unbosque.backend.model.entity.ConfiguracionAlerta;
import co.edu.unbosque.backend.model.entity.Lote;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.request.ActualizarConfiguracionAlertaRequest;
import co.edu.unbosque.backend.model.response.AlertaResponse;
import co.edu.unbosque.backend.model.response.ConfiguracionAlertaResponse;
import co.edu.unbosque.backend.repository.AlertaRepository;
import co.edu.unbosque.backend.repository.ConfiguracionAlertaRepository;
import co.edu.unbosque.backend.repository.LoteRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para la generación y gestión de alertas de inventario.
 *
 * @author juanjo2748
 */
@Service
public class AlertaService {

    private static final Long CONFIG_ID = 1L;
    private static final String TIPO_STOCK = "STOCK_MINIMO";
    private static final String TIPO_VENCIMIENTO = "PROXIMO_VENCIMIENTO";

    private final AlertaRepository alertaRepository;
    private final ConfiguracionAlertaRepository configuracionRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    public AlertaService(
            AlertaRepository alertaRepository,
            ConfiguracionAlertaRepository configuracionRepository,
            ProductoRepository productoRepository,
            LoteRepository loteRepository
    ) {
        this.alertaRepository = alertaRepository;
        this.configuracionRepository = configuracionRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    /**
     * Genera alertas nuevas por stock bajo y vencimiento próximo, sin duplicar
     * alertas ya existentes no leídas. Retorna todas las alertas no leídas.
     *
     * @return lista de alertas no leídas tras la generación
     */
    @Transactional
    public List<AlertaResponse> generarAlertas() {
        int dias = obtenerConfiguracionEntidad().getDiasProximoVencimiento();
        LocalDateTime ahora = LocalDateTime.now();

        for (Producto producto : productoRepository.findProductosConStockBajo()) {
            if (!alertaRepository.existsByIdProductoAndTipoAndLeidaFalse(producto.getUniqueID(), TIPO_STOCK)) {
                AlertaInventario alerta = new AlertaInventario();
                alerta.setTipo(TIPO_STOCK);
                alerta.setIdProducto(producto.getUniqueID());
                alerta.setNombreProducto(producto.getNombre());
                alerta.setCantidadActual(producto.getStockActual());
                alerta.setStockMinimo(producto.getStockMinimo());
                alerta.setLeida(false);
                alerta.setFechaGeneracion(ahora);
                alertaRepository.save(alerta);
            }
        }

        LocalDate fechaCorte = LocalDate.now().plusDays(dias);
        for (Lote lote : loteRepository.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(LocalDate.now(), fechaCorte)) {
            if (!alertaRepository.existsByIdLoteAndTipoAndLeidaFalse(lote.getIdLote(), TIPO_VENCIMIENTO)) {
                AlertaInventario alerta = new AlertaInventario();
                alerta.setTipo(TIPO_VENCIMIENTO);
                alerta.setIdProducto(lote.getProducto().getUniqueID());
                alerta.setNombreProducto(lote.getProducto().getNombre());
                alerta.setIdLote(lote.getIdLote());
                alerta.setNumeroLote(lote.getNumeroLote());
                alerta.setFechaVencimiento(lote.getFechaVencimiento());
                alerta.setLeida(false);
                alerta.setFechaGeneracion(ahora);
                alertaRepository.save(alerta);
            }
        }

        return alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lista todas las alertas o solo las no leídas.
     *
     * @param soloNoLeidas si es true, filtra solo las no leídas
     * @return lista de alertas
     */
    @Transactional(readOnly = true)
    public List<AlertaResponse> listarAlertas(boolean soloNoLeidas) {
        List<AlertaInventario> alertas = soloNoLeidas
                ? alertaRepository.findByLeidaFalseOrderByFechaGeneracionDesc()
                : alertaRepository.findAllByOrderByFechaGeneracionDesc();
        return alertas.stream().map(this::toResponse).toList();
    }

    /**
     * Marca una alerta específica como leída.
     *
     * @param idAlerta identificador de la alerta
     */
    @Transactional
    public void marcarComoLeida(Long idAlerta) {
        AlertaInventario alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la alerta con id " + idAlerta));
        alerta.setLeida(true);
        alertaRepository.save(alerta);
    }

    /**
     * Marca todas las alertas no leídas como leídas.
     */
    @Transactional
    public void marcarTodasComoLeidas() {
        alertaRepository.marcarTodasComoLeidas();
    }

    /**
     * Retorna la configuración actual de alertas.
     *
     * @return configuración de alertas
     */
    @Transactional(readOnly = true)
    public ConfiguracionAlertaResponse obtenerConfiguracion() {
        return new ConfiguracionAlertaResponse(obtenerConfiguracionEntidad().getDiasProximoVencimiento());
    }

    /**
     * Actualiza el umbral de días para alertas de vencimiento próximo.
     *
     * @param request nueva configuración
     * @return configuración actualizada
     */
    @Transactional
    public ConfiguracionAlertaResponse actualizarConfiguracion(ActualizarConfiguracionAlertaRequest request) {
        ConfiguracionAlerta config = configuracionRepository.findById(CONFIG_ID)
                .orElse(new ConfiguracionAlerta(CONFIG_ID, 30));
        config.setDiasProximoVencimiento(request.diasProximoVencimiento());
        return new ConfiguracionAlertaResponse(configuracionRepository.save(config).getDiasProximoVencimiento());
    }

    private ConfiguracionAlerta obtenerConfiguracionEntidad() {
        return configuracionRepository.findById(CONFIG_ID)
                .orElseGet(() -> configuracionRepository.save(new ConfiguracionAlerta(CONFIG_ID, 30)));
    }

    private AlertaResponse toResponse(AlertaInventario a) {
        return new AlertaResponse(
                a.getIdAlerta(),
                a.getTipo(),
                a.getIdProducto(),
                a.getNombreProducto(),
                a.getIdLote(),
                a.getNumeroLote(),
                a.getCantidadActual(),
                a.getStockMinimo(),
                a.getFechaVencimiento(),
                a.isLeida(),
                a.getFechaGeneracion()
        );
    }
}
