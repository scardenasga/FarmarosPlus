package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.ProveedorProducto;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProductoProveedorRequest;
import co.edu.unbosque.backend.model.request.ActualizarProveedorRequest;
import co.edu.unbosque.backend.model.request.AsociarProductoProveedorRequest;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
import co.edu.unbosque.backend.model.response.ProveedorDetalleResponse;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedorProductoRepository proveedorProductoRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void crearProveedor_exitoso_normalizaTextoYGuarda() {
        CrearProveedorRequest request = new CrearProveedorRequest(
                "  Farmaceutica   XYZ  ", "860123456-7", "+57 1 1234567",
                "contacto@xyz.com", "Juan Perez", "Neto 30"
        );

        when(proveedorRepository.existsByNombreIgnoreCase("Farmaceutica XYZ")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> {
            Proveedor p = inv.getArgument(0);
            p.setIdProveedor(1L);
            return p;
        });

        Proveedor resultado = proveedorService.crearProveedor(request);

        ArgumentCaptor<Proveedor> captor = ArgumentCaptor.forClass(Proveedor.class);
        verify(proveedorRepository).save(captor.capture());
        Proveedor guardado = captor.getValue();

        assertEquals("Farmaceutica XYZ", guardado.getNombre());
        assertEquals("860123456-7", guardado.getNit());
        assertEquals("ACTIVO", guardado.getEstado());
        assertEquals("Neto 30", guardado.getCondicionPago());
        assertEquals("Farmaceutica XYZ", resultado.getNombre());
    }

    @Test
    void crearProveedor_nombreDuplicado_lanzaBusinessException() {
        CrearProveedorRequest request = new CrearProveedorRequest(
                "Farmaceutica XYZ", "860123456-7", null, null, null, null
        );

        when(proveedorRepository.existsByNombreIgnoreCase("Farmaceutica XYZ")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> proveedorService.crearProveedor(request));
    }

    @Test
    void crearProveedor_sinNombre_lanzaBusinessException() {
        CrearProveedorRequest request = new CrearProveedorRequest(
                null, "860123456-7", null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> proveedorService.crearProveedor(request));
    }

    @Test
    void crearProveedor_requestNulo_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> proveedorService.crearProveedor(null));
    }

    @Test
    void obtenerProveedorPorId_inexistente_lanzaResourceNotFoundException() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(co.edu.unbosque.backend.exception.ResourceNotFoundException.class,
                () -> proveedorService.obtenerProveedorPorId(99L));
    }

    @Test
    void listarProveedoresActivos_debeRetornarSoloActivos() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica ABC");
        proveedor.setEstado("ACTIVO");

        when(proveedorRepository.findByEstadoOrderByNombreAsc("ACTIVO"))
                .thenReturn(List.of(proveedor));

        List<Proveedor> resultado = proveedorService.listarProveedoresActivos();

        assertEquals(1, resultado.size());
        assertEquals("ACTIVO", resultado.getFirst().getEstado());
    }

    @Test
    void listarTodosProveedores_debeRetornarTodos() {
        when(proveedorRepository.findAll()).thenReturn(List.of());

        List<Proveedor> resultado = proveedorService.listarTodosProveedores();

        assertEquals(0, resultado.size());
    }

    @Test
    void actualizarEstadoProveedor_exitoso_cambiaEstado() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setEstado("ACTIVO");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedor);

        Proveedor resultado = proveedorService.actualizarEstadoProveedor(1L,
                new ActualizarEstadoProveedorRequest("INACTIVO"));

        assertEquals("INACTIVO", resultado.getEstado());
    }

    @Test
    void actualizarEstadoProveedor_estadoInvalido_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> proveedorService.actualizarEstadoProveedor(1L,
                        new ActualizarEstadoProveedorRequest("PAUSADO")));
    }

    @Test
    void actualizarEstadoProveedor_estadoVacio_lanzaBusinessException() {
        assertThrows(BusinessException.class,
                () -> proveedorService.actualizarEstadoProveedor(1L,
                        new ActualizarEstadoProveedorRequest("")));
    }

    @Test
    void actualizarProveedor_exitoso_actualizaCamposYNormaliza() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        Proveedor resultado = proveedorService.actualizarProveedor(1L,
                new ActualizarProveedorRequest("  Farmaceutica   XYZ Plus  ", null, "  123 456  ",
                        null, null, "  Neto 45  "));

        assertEquals("Farmaceutica XYZ Plus", resultado.getNombre());
        assertEquals("123 456", resultado.getTelefono());
        assertEquals("Neto 45", resultado.getCondicionPago());
    }

    @Test
    void actualizarProveedor_nombreDuplicado_lanzaBusinessException() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.existsByNombreIgnoreCaseAndIdProveedorNot("Farmaceutica ABC", 1L)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> proveedorService.actualizarProveedor(1L,
                        new ActualizarProveedorRequest("Farmaceutica ABC", null, null, null, null, null)));
    }

    @Test
    void obtenerProveedorDetallePorId_debeRetornarProductosAsociados() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Farmaceutica XYZ");
        proveedor.setEstado("ACTIVO");

        Producto producto = new Producto();
        producto.setUniqueID(10L);
        producto.setNombre("Acetaminofen");
        producto.setCodigoBarras("7701234567890");
        producto.setEstado("ACTIVO");

        ProveedorProducto relacion = new ProveedorProducto();
        relacion.setProveedor(proveedor);
        relacion.setProducto(producto);
        relacion.setCodigoProductoProveedor("PR-ACET-01");
        relacion.setPrecioReferencia(8500.0);
        relacion.setEstado("ACTIVO");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorProductoRepository.findByProveedorIdWithProductoOrderByProductoNombreAsc(1L))
                .thenReturn(List.of(relacion));

        ProveedorDetalleResponse resultado = proveedorService.obtenerProveedorDetallePorId(1L);

        assertEquals(1, resultado.productos().size());
        assertEquals("Acetaminofen", resultado.productos().getFirst().nombre());
        assertEquals("ACTIVO", resultado.productos().getFirst().estadoRelacion());
    }

    @Test
    void asociarProducto_relacionNueva_debeGuardarActiva() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);

        Producto producto = new Producto();
        producto.setUniqueID(10L);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(proveedorProductoRepository.findByProveedorIdAndProductoId(1L, 10L)).thenReturn(Optional.empty());
        when(proveedorProductoRepository.save(any(ProveedorProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProveedorProducto resultado = proveedorService.asociarProducto(1L,
                new AsociarProductoProveedorRequest(10L, " COD-1 ", 1234.0));

        assertEquals("ACTIVO", resultado.getEstado());
        assertEquals("COD-1", resultado.getCodigoProductoProveedor());
        assertEquals(1234.0, resultado.getPrecioReferencia());
    }

    @Test
    void asociarProducto_relacionActivaDuplicada_lanzaBusinessException() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);

        Producto producto = new Producto();
        producto.setUniqueID(10L);

        ProveedorProducto relacion = new ProveedorProducto();
        relacion.setProveedor(proveedor);
        relacion.setProducto(producto);
        relacion.setEstado("ACTIVO");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(proveedorProductoRepository.findByProveedorIdAndProductoId(1L, 10L)).thenReturn(Optional.of(relacion));

        assertThrows(BusinessException.class,
                () -> proveedorService.asociarProducto(1L,
                        new AsociarProductoProveedorRequest(10L, "COD-1", 1234.0)));
    }

    @Test
    void asociarProducto_relacionInactiva_debeReactivar() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);

        Producto producto = new Producto();
        producto.setUniqueID(10L);

        ProveedorProducto relacion = new ProveedorProducto();
        relacion.setProveedor(proveedor);
        relacion.setProducto(producto);
        relacion.setEstado("INACTIVO");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(proveedorProductoRepository.findByProveedorIdAndProductoId(1L, 10L)).thenReturn(Optional.of(relacion));
        when(proveedorProductoRepository.save(any(ProveedorProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProveedorProducto resultado = proveedorService.asociarProducto(1L,
                new AsociarProductoProveedorRequest(10L, "COD-2", 2000.0));

        assertEquals("ACTIVO", resultado.getEstado());
        assertEquals("COD-2", resultado.getCodigoProductoProveedor());
    }

    @Test
    void actualizarEstadoProductoProveedor_debeCambiarEstado() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);

        Producto producto = new Producto();
        producto.setUniqueID(10L);

        ProveedorProducto relacion = new ProveedorProducto();
        relacion.setProveedor(proveedor);
        relacion.setProducto(producto);
        relacion.setEstado("ACTIVO");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorProductoRepository.findByProveedorIdAndProductoId(1L, 10L)).thenReturn(Optional.of(relacion));
        when(proveedorProductoRepository.save(any(ProveedorProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProveedorProducto resultado = proveedorService.actualizarEstadoProductoProveedor(1L, 10L,
                new ActualizarEstadoProductoProveedorRequest("INACTIVO"));

        assertEquals("INACTIVO", resultado.getEstado());
    }
}
