package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.CierreCaja;
import co.edu.unbosque.backend.model.entity.DetalleVenta;
import co.edu.unbosque.backend.model.entity.PagoVenta;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.Venta;
import co.edu.unbosque.backend.model.request.CierreCajaRequest;
import co.edu.unbosque.backend.model.response.CierreCajaResponse;
import co.edu.unbosque.backend.model.response.CierreResumenResponse;
import co.edu.unbosque.backend.repository.CierreCajaRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import co.edu.unbosque.backend.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio de cierre de caja. Calcula agregados a partir de ventas existentes
 * sin modificar ninguna otra tabla. Solo lectura + persistencia del snapshot.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class CierreCajaService {

    private final CierreCajaRepository cierreCajaRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;

    public CierreCajaService(CierreCajaRepository cierreCajaRepository,
                             VentaRepository ventaRepository,
                             UsuarioRepository usuarioRepository) {
        this.cierreCajaRepository = cierreCajaRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public CierreResumenResponse generarResumen(LocalDateTime desde, LocalDateTime hasta, Long idUsuario, Double montoInicial) {
        LocalDateTime[] rango = normalizarRango(desde, hasta);
        LocalDateTime d = rango[0];
        LocalDateTime h = rango[1];

        List<Venta> ventas = ventaRepository.findHistorico(d, h, idUsuario, null);
        // Separar completadas y anuladas
        List<Venta> completadas = ventas.stream().filter(v -> "COMPLETADA".equalsIgnoreCase(v.getEstado())).toList();
        List<Venta> anuladas = ventas.stream().filter(v -> "ANULADA".equalsIgnoreCase(v.getEstado())).toList();

        double totalVentas = completadas.stream().mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0).sum();
        double totalDescuentos = completadas.stream().mapToDouble(v -> v.getDescuento() != null ? v.getDescuento() : 0.0).sum();
        double totalIva = completadas.stream().mapToDouble(v -> v.getIva() != null ? v.getIva() : 0.0).sum();

        double totalEfectivo = 0, totalTarjeta = 0, totalTransferencia = 0;
        double gananciaEstimada = 0;

        for (Venta v : completadas) {
            if (v.getPagos() != null) {
                for (PagoVenta p : v.getPagos()) {
                    if ("EFECTIVO".equalsIgnoreCase(p.getTipo())) totalEfectivo += p.getMonto();
                    else if ("TARJETA".equalsIgnoreCase(p.getTipo())) totalTarjeta += p.getMonto();
                    else if ("TRANSFERENCIA".equalsIgnoreCase(p.getTipo())) totalTransferencia += p.getMonto();
                }
            }
            if (v.getDetalles() != null) {
                for (DetalleVenta d2 : v.getDetalles()) {
                    Double costo = d2.getProducto() != null ? d2.getProducto().getCosto() : null;
                    double costoSeguro = costo != null ? costo : 0.0;
                    double precio = d2.getPrecioUnitarioAplicado() != null ? d2.getPrecioUnitarioAplicado() : 0.0;
                    int cant = d2.getCantidad() != null ? d2.getCantidad() : 0;
                    gananciaEstimada += (precio - costoSeguro) * cant;
                }
            }
        }

        double montoInicialSeguro = montoInicial != null ? montoInicial : 0.0;
        double efectivoEsperado = totalEfectivo + montoInicialSeguro;

        return new CierreResumenResponse(d, h, montoInicialSeguro, totalEfectivo, totalTarjeta, totalTransferencia,
                totalVentas, totalDescuentos, totalIva, completadas.size(), anuladas.size(), gananciaEstimada, efectivoEsperado);
    }

    @Transactional
    public CierreCajaResponse crearCierre(CierreCajaRequest request) {
        LocalDateTime[] rango = normalizarRango(request != null ? request.fechaApertura() : null,
                request != null ? request.fechaCierre() : null);
        LocalDateTime d = rango[0];
        LocalDateTime h = rango[1];

        if (d.isAfter(h)) {
            throw new BusinessException("La fecha de apertura no puede ser posterior a la fecha de cierre");
        }

        Double montoInicial = request != null && request.montoInicial() != null ? request.montoInicial() : 0.0;
        if (montoInicial < 0) throw new BusinessException("El monto inicial no puede ser negativo");
        Double montoDeclarado = request != null ? request.montoDeclarado() : null;
        if (montoDeclarado != null && montoDeclarado < 0) throw new BusinessException("El monto declarado no puede ser negativo");

        Long idUsuario = request != null ? request.idUsuario() : null;
        String username = null;
        if (idUsuario != null) {
            Usuario u = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + idUsuario));
            username = u.getUsername();
        }

        CierreResumenResponse resumen = generarResumen(d, h, idUsuario, montoInicial);

        Double diferencia = null;
        if (montoDeclarado != null) {
            diferencia = montoDeclarado - resumen.efectivoEsperado();
        }

        CierreCaja cierre = new CierreCaja();
        cierre.setFechaApertura(d);
        cierre.setFechaCierre(h);
        cierre.setIdUsuario(idUsuario);
        cierre.setUsername(username);
        cierre.setMontoInicial(montoInicial);
        cierre.setMontoDeclarado(montoDeclarado);
        cierre.setTotalEfectivo(resumen.totalEfectivo());
        cierre.setTotalTarjeta(resumen.totalTarjeta());
        cierre.setTotalTransferencia(resumen.totalTransferencia());
        cierre.setTotalVentas(resumen.totalVentas());
        cierre.setTotalDescuentos(resumen.totalDescuentos());
        cierre.setTotalIva(resumen.totalIva());
        cierre.setCantidadVentas(resumen.cantidadVentas());
        cierre.setCantidadAnuladas(resumen.cantidadAnuladas());
        cierre.setGananciaEstimada(resumen.gananciaEstimada());
        cierre.setDiferencia(diferencia);
        cierre.setObservaciones(request != null ? request.observaciones() : null);
        cierre.setEstado("CERRADO");

        CierreCaja guardado = cierreCajaRepository.save(cierre);
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<CierreCajaResponse> listarTodos() {
        return cierreCajaRepository.findAllByOrderByFechaCierreDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CierreCajaResponse obtenerPorId(Long id) {
        CierreCaja c = cierreCajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el cierre con id " + id));
        return toResponse(c);
    }

    private LocalDateTime[] normalizarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null && hasta == null) {
            LocalDate hoy = LocalDate.now();
            return new LocalDateTime[]{ hoy.atStartOfDay(), hoy.atTime(LocalTime.of(23, 59, 59)) };
        }
        if (desde == null) desde = hasta.toLocalDate().atStartOfDay();
        if (hasta == null) hasta = desde.toLocalDate().atTime(LocalTime.of(23, 59, 59));
        return new LocalDateTime[]{ desde, hasta };
    }

    private CierreCajaResponse toResponse(CierreCaja c) {
        return new CierreCajaResponse(
                c.getId(), c.getFechaApertura(), c.getFechaCierre(), c.getIdUsuario(), c.getUsername(),
                c.getMontoInicial(), c.getMontoDeclarado(), c.getTotalEfectivo(), c.getTotalTarjeta(),
                c.getTotalTransferencia(), c.getTotalVentas(), c.getTotalDescuentos(), c.getTotalIva(),
                c.getCantidadVentas(), c.getCantidadAnuladas(), c.getGananciaEstimada(), c.getDiferencia(),
                c.getObservaciones(), c.getEstado(), c.getFechaCreacion()
        );
    }
}
