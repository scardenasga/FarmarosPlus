package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.entity.*;
import co.edu.unbosque.backend.model.request.CierreCajaRequest;
import co.edu.unbosque.backend.model.response.CierreResumenResponse;
import co.edu.unbosque.backend.repository.CierreCajaRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import co.edu.unbosque.backend.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CierreCajaServiceTest {

    @Mock private CierreCajaRepository cierreCajaRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private CierreCajaService service;

    @Test
    void generarResumen_debeAgregarPorMetodoYCalcularGanancia() {
        LocalDateTime desde = LocalDateTime.now().minusHours(8);
        LocalDateTime hasta = LocalDateTime.now();

        Producto prod = new Producto();
        prod.setCosto(8000.0);
        prod.setPrecioVenta(12000.0);

        DetalleVenta det = new DetalleVenta();
        det.setProducto(prod);
        det.setCantidad(2);
        det.setPrecioUnitarioAplicado(12000.0);

        PagoVenta pagoEfectivo = new PagoVenta();
        pagoEfectivo.setTipo("EFECTIVO");
        pagoEfectivo.setMonto(24000.0);

        Venta venta = new Venta();
        venta.setEstado("COMPLETADA");
        venta.setTotal(24000.0);
        venta.setDescuento(0.0);
        venta.setIva(0.0);
        venta.setDetalles(Set.of(det));
        venta.setPagos(Set.of(pagoEfectivo));

        when(ventaRepository.findHistorico(any(), any(), any(), any())).thenReturn(List.of(venta));

        CierreResumenResponse r = service.generarResumen(desde, hasta, null, 5000.0);

        assertEquals(1, r.cantidadVentas());
        assertEquals(24000.0, r.totalVentas(), 0.01);
        assertEquals(24000.0, r.totalEfectivo(), 0.01);
        assertEquals(0.0, r.totalTarjeta(), 0.01);
        assertEquals(8000.0, r.gananciaEstimada(), 0.01); // (12000-8000)*2
        assertEquals(29000.0, r.efectivoEsperado(), 0.01); // efectivo + montoInicial
    }

    @Test
    void crearCierre_conMontoDeclarado_debeCalcularDiferencia() {
        LocalDateTime desde = LocalDateTime.now().minusHours(4);
        LocalDateTime hasta = LocalDateTime.now();

        Venta venta = new Venta();
        venta.setEstado("COMPLETADA");
        venta.setTotal(10000.0);
        venta.setDescuento(0.0);
        venta.setIva(0.0);
        PagoVenta p = new PagoVenta(); p.setTipo("EFECTIVO"); p.setMonto(10000.0);
        venta.setPagos(Set.of(p));
        Producto prod = new Producto(); prod.setCosto(6000.0); prod.setPrecioVenta(10000.0);
        DetalleVenta d = new DetalleVenta(); d.setProducto(prod); d.setCantidad(1); d.setPrecioUnitarioAplicado(10000.0);
        venta.setDetalles(Set.of(d));

        when(ventaRepository.findHistorico(any(), any(), any(), any())).thenReturn(List.of(venta));
        when(cierreCajaRepository.save(any(CierreCaja.class))).thenAnswer(i -> {
            CierreCaja c = i.getArgument(0); c.setId(1L); return c;
        });

        CierreCajaRequest req = new CierreCajaRequest(desde, hasta, 2000.0, 13000.0, null, "test");
        var cierre = service.crearCierre(req);

        // efectivo 10000 + inicial 2000 = 12000 esperado, declarado 13000 => diferencia 1000
        assertEquals(1000.0, cierre.diferencia(), 0.01);
        assertEquals(1, cierre.cantidadVentas());
    }

    @Test
    void crearCierre_conUsuario_debeResolverUsername() {
        LocalDateTime desde = LocalDateTime.now().minusDays(1);
        LocalDateTime hasta = LocalDateTime.now();
        Usuario u = new Usuario(); u.setUsername("vendedor"); u.setIdUsuario(5L);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(u));
        when(ventaRepository.findHistorico(any(), any(), any(), any())).thenReturn(List.of());
        when(cierreCajaRepository.save(any(CierreCaja.class))).thenAnswer(i -> { CierreCaja c=i.getArgument(0); c.setId(2L); return c; });

        var req = new CierreCajaRequest(desde, hasta, 0.0, null, 5L, null);
        var cierre = service.crearCierre(req);
        assertEquals("vendedor", cierre.username());
    }
}
