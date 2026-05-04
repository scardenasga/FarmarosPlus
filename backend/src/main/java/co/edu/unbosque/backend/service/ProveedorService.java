package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación para proveedores.
 *
 * @author juanjo2748
 */
@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByEstadoOrderByNombreAsc("ACTIVO");
    }

    @Transactional(readOnly = true)
    public Proveedor obtenerPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el proveedor con id " + id));
    }

    @Transactional
    public Proveedor crear(CrearProveedorRequest request) {
        String nombre = request.nombre().trim();
        if (proveedorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un proveedor con nombre " + nombre);
        }
        Proveedor p = new Proveedor();
        p.setNombre(nombre);
        p.setNit(request.nit());
        p.setContacto(request.contacto());
        p.setTelefono(request.telefono());
        p.setEmail(request.email());
        p.setEstado("ACTIVO");
        return proveedorRepository.save(p);
    }
}
