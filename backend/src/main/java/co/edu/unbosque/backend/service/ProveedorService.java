package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación para gestión de proveedores.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional
    public Proveedor crearProveedor(CrearProveedorRequest request) {
        validarCreacionProveedor(request);

        String nombre = normalizarTexto(request.nombre());
        if (proveedorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un proveedor con el nombre: " + nombre);
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(nombre);
        proveedor.setNit(normalizarTexto(request.nit()));
        proveedor.setTelefono(normalizarTexto(request.telefono()));
        proveedor.setEmail(normalizarTexto(request.email()));
        proveedor.setContacto(normalizarTexto(request.contacto()));
        proveedor.setCondicionPago(normalizarTexto(request.condicionPago()));
        proveedor.setEstado("ACTIVO");

        return proveedorRepository.save(proveedor);
    }

    @Transactional(readOnly = true)
    public Proveedor obtenerProveedorPorId(Long idProveedor) {
        return proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe proveedor con id " + idProveedor
                ));
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedoresActivos() {
        return proveedorRepository.findByEstadoOrderByNombreAsc("ACTIVO");
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarTodosProveedores() {
        return proveedorRepository.findAll();
    }

    @Transactional
    public Proveedor actualizarEstadoProveedor(Long idProveedor, ActualizarEstadoProveedorRequest request) {
        validarEstado(request.estado());

        Proveedor proveedor = obtenerProveedorPorId(idProveedor);
        proveedor.setEstado(request.estado().toUpperCase());

        return proveedorRepository.save(proveedor);
    }

    private void validarCreacionProveedor(CrearProveedorRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de creación del proveedor es obligatoria");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new BusinessException("El nombre del proveedor es obligatorio");
        }
    }

    private void validarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new BusinessException("El estado del proveedor es obligatorio");
        }
        String estadoUpper = estado.trim().toUpperCase();
        if (!estadoUpper.equals("ACTIVO") && !estadoUpper.equals("INACTIVO")) {
            throw new BusinessException("Estado inválido. Valores válidos: ACTIVO, INACTIVO");
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) return null;
        return texto.trim().replaceAll("\\s+", " ");
    }
}
