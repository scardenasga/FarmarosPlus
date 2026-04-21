package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.ProveedorRequest;
import co.edu.unbosque.backend.model.response.ProveedorResponse;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de gestión de proveedores.
 *
 * @author juanjo2748
 */
@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional
    public ProveedorResponse crearProveedor(ProveedorRequest request) {
        if (proveedorRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("Ya existe un proveedor con el nombre: " + request.nombre());
        }
        if (request.nit() != null && !request.nit().isBlank()
                && proveedorRepository.existsByNit(request.nit())) {
            throw new BusinessException("Ya existe un proveedor con el NIT: " + request.nit());
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.nombre());
        proveedor.setNit(request.nit());
        proveedor.setContacto(request.contacto());
        proveedor.setTelefono(request.telefono());
        proveedor.setEmail(request.email());
        proveedor.setDireccion(request.direccion());
        proveedor.setEstado("ACTIVO");

        return toResponse(proveedorRepository.save(proveedor));
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponse> listarProveedores() {
        return proveedorRepository.findAllByOrderByNombreAsc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponse> listarProveedoresActivos() {
        return proveedorRepository.findByEstadoOrderByNombreAsc("ACTIVO")
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Long id) {
        return toResponse(buscarOLanzar(id));
    }

    @Transactional
    public ProveedorResponse actualizarProveedor(Long id, ProveedorRequest request) {
        Proveedor proveedor = buscarOLanzar(id);

        if (!proveedor.getNombre().equalsIgnoreCase(request.nombre())
                && proveedorRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("Ya existe un proveedor con el nombre: " + request.nombre());
        }
        if (request.nit() != null && !request.nit().isBlank()
                && !request.nit().equals(proveedor.getNit())
                && proveedorRepository.existsByNit(request.nit())) {
            throw new BusinessException("Ya existe un proveedor con el NIT: " + request.nit());
        }

        proveedor.setNombre(request.nombre());
        proveedor.setNit(request.nit());
        proveedor.setContacto(request.contacto());
        proveedor.setTelefono(request.telefono());
        proveedor.setEmail(request.email());
        proveedor.setDireccion(request.direccion());

        return toResponse(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void desactivarProveedor(Long id) {
        Proveedor proveedor = buscarOLanzar(id);
        proveedor.setEstado("INACTIVO");
        proveedorRepository.save(proveedor);
    }

    private Proveedor buscarOLanzar(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el proveedor con id " + id));
    }

    private ProveedorResponse toResponse(Proveedor p) {
        return new ProveedorResponse(
                p.getIdProveedor(),
                p.getNombre(),
                p.getNit(),
                p.getContacto(),
                p.getTelefono(),
                p.getEmail(),
                p.getDireccion(),
                p.getEstado(),
                p.getFechaCreacion()
        );
    }
}
