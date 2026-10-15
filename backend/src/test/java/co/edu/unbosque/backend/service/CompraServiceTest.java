package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.CompraProveedor;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.DetalleCompraRequest;
import co.edu.unbosque.backend.model.request.RegistrarCompraRequest;
import co.edu.unbosque.backend.model.response.CompraResponse;
import co.edu.unbosque.backend.repository.CompraProveedorRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock private CompraProveedorRepository compraRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks
    private CompraService compraService;

    private Proveedor buildProveedor() {
        Proveedor p = new Proveedor();
        p.setIdProveedor(1L);
        p.setNombre("Laboratorio XYZ");
        return p;
    }

    private Producto buildProducto(Long id, String nombre) {
        Producto p = new Producto();
        p.setUniqueID(id);
        p.setNombre(nombre);
        return p;
    }

    // ── registrar ────────────────────────────────────────────────────────────

    @Test
    void registrar_debeCalcularTotalSumandoSubtotales() {
        Proveedor proveedor = buildProveedor();
        Producto producto = buildProducto(10L, "Amoxicilina");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(compraRepository.save(any())).thenAnswer(i -> {
            CompraProveedor c = i.getArgument(0);
            c.setIdCompra(1L);
            return c;
        });

        RegistrarCompraRequest request = new RegistrarCompraRequest(
                1L, "admin", "FAC-001", null,
                List.of(new DetalleCompraRequest(10L, 10, 5000.0)));

        CompraResponse response = compraService.registrar(request);

        assertEquals(50000.0, response.total());
        assertEquals("Laboratorio XYZ", response.nombreProveedor());
        assertEquals("FAC-001", response.numeroFactura());
    }

    @Test
    void registrar_conVariosProductos_debeSumarCorrectamente() {
        Proveedor proveedor = buildProveedor();
        Producto p1 = buildProducto(10L, "Amoxicilina");
        Producto p2 = buildProducto(11L, "Ibuprofeno");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(p1));
        when(productoRepository.findById(11L)).thenReturn(Optional.of(p2));
        when(compraRepository.save(any())).thenAnswer(i -> {
            CompraProveedor c = i.getArgument(0);
            c.setIdCompra(1L);
            return c;
        });

        RegistrarCompraRequest request = new RegistrarCompraRequest(
                1L, "admin", null, null,
                List.of(
                        new DetalleCompraRequest(10L, 5, 2000.0),
                        new DetalleCompraRequest(11L, 3, 1000.0)
                ));

        CompraResponse response = compraService.registrar(request);

        assertEquals(13000.0, response.total());
        assertEquals(2, response.detalles().size());
    }

    @Test
    void registrar_cuandoProveedorNoExiste_debeLanzarResourceNotFoundException() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        RegistrarCompraRequest request = new RegistrarCompraRequest(
                99L, "admin", null, null,
                List.of(new DetalleCompraRequest(1L, 1, 100.0)));

        assertThrows(ResourceNotFoundException.class, () -> compraService.registrar(request));
        verify(compraRepository, never()).save(any());
    }

    // ── listar ───────────────────────────────────────────────────────────────

    @Test
    void listar_sinFiltro_debeUsarRepositorioGeneral() {
        when(compraRepository.findAllByOrderByFechaRecepcionDesc()).thenReturn(List.of());

        compraService.listar(null);

        verify(compraRepository).findAllByOrderByFechaRecepcionDesc();
        verify(compraRepository, never()).findByProveedor_IdProveedorOrderByFechaRecepcionDesc(any());
    }

    @Test
    void listar_conFiltroProveedor_debeUsarRepositorioFiltrado() {
        when(compraRepository.findByProveedor_IdProveedorOrderByFechaRecepcionDesc(1L)).thenReturn(List.of());

        compraService.listar(1L);

        verify(compraRepository).findByProveedor_IdProveedorOrderByFechaRecepcionDesc(1L);
        verify(compraRepository, never()).findAllByOrderByFechaRecepcionDesc();
    }

    // ── obtener ──────────────────────────────────────────────────────────────

    @Test
    void obtener_inexistente_debeLanzarResourceNotFoundException() {
        when(compraRepository.findWithDetallesById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> compraService.obtener(99L));
    }
}
