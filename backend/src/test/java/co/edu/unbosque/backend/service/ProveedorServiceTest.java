package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
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
}