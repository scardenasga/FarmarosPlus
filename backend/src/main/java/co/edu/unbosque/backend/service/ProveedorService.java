package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.ProveedorProducto;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProveedorRequest;
import co.edu.unbosque.backend.model.request.ActualizarEstadoProductoProveedorRequest;
import co.edu.unbosque.backend.model.request.ActualizarProveedorRequest;
import co.edu.unbosque.backend.model.request.AsociarProductoProveedorRequest;
import co.edu.unbosque.backend.model.request.CrearProveedorRequest;
import co.edu.unbosque.backend.model.response.ProductoProveedorResponse;
import co.edu.unbosque.backend.model.response.ProveedorDetalleResponse;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorProductoRepository;
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
    private final ProductoRepository productoRepository;
    private final ProveedorProductoRepository proveedorProductoRepository;

    public ProveedorService(
            ProveedorRepository proveedorRepository,
            ProductoRepository productoRepository,
            ProveedorProductoRepository proveedorProductoRepository
    ) {
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.proveedorProductoRepository = proveedorProductoRepository;
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
    public ProveedorDetalleResponse obtenerProveedorDetallePorId(Long idProveedor) {
        Proveedor proveedor = obtenerProveedorPorId(idProveedor);
        List<ProductoProveedorResponse> productos = proveedorProductoRepository
                .findByProveedorIdWithProductoOrderByProductoNombreAsc(idProveedor)
                .stream()
                .map(this::toProductoProveedorResponse)
                .toList();

        return new ProveedorDetalleResponse(
                proveedor.getIdProveedor(),
                proveedor.getNombre(),
                proveedor.getNit(),
                proveedor.getTelefono(),
                proveedor.getEmail(),
                proveedor.getContacto(),
                proveedor.getEstado(),
                proveedor.getCondicionPago(),
                productos
        );
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

    @Transactional
    public Proveedor actualizarProveedor(Long idProveedor, ActualizarProveedorRequest request) {
        validarActualizacionProveedor(request);

        Proveedor proveedor = obtenerProveedorPorId(idProveedor);

        if (request.nombre() != null) {
            String nombre = normalizarTexto(request.nombre());
            if (nombre == null) {
                throw new BusinessException("El nombre del proveedor no puede estar vacío");
            }
            if (!nombre.equalsIgnoreCase(proveedor.getNombre())
                    && proveedorRepository.existsByNombreIgnoreCaseAndIdProveedorNot(nombre, idProveedor)) {
                throw new BusinessException("Ya existe un proveedor con el nombre: " + nombre);
            }
            proveedor.setNombre(nombre);
        }

        if (request.nit() != null) {
            proveedor.setNit(normalizarTexto(request.nit()));
        }
        if (request.telefono() != null) {
            proveedor.setTelefono(normalizarTexto(request.telefono()));
        }
        if (request.email() != null) {
            proveedor.setEmail(normalizarTexto(request.email()));
        }
        if (request.contacto() != null) {
            proveedor.setContacto(normalizarTexto(request.contacto()));
        }
        if (request.condicionPago() != null) {
            proveedor.setCondicionPago(normalizarTexto(request.condicionPago()));
        }

        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public ProveedorProducto asociarProducto(Long idProveedor, AsociarProductoProveedorRequest request) {
        validarAsociacionProducto(request);

        Proveedor proveedor = obtenerProveedorPorId(idProveedor);
        Producto producto = obtenerProductoPorId(request.productoId());

        ProveedorProducto relacion = proveedorProductoRepository
                .findByProveedorIdAndProductoId(idProveedor, request.productoId())
                .orElse(null);

        if (relacion != null && "ACTIVO".equals(relacion.getEstado())) {
            throw new BusinessException("El proveedor ya tiene asociado ese producto");
        }

        if (relacion == null) {
            relacion = new ProveedorProducto();
            relacion.setProveedor(proveedor);
            relacion.setProducto(producto);
        }

        relacion.setCodigoProductoProveedor(normalizarTexto(request.codigoProductoProveedor()));
        relacion.setPrecioReferencia(request.precioReferencia());
        relacion.setEstado("ACTIVO");

        return proveedorProductoRepository.save(relacion);
    }

    @Transactional
    public ProveedorProducto actualizarEstadoProductoProveedor(
            Long idProveedor,
            Long productoId,
            ActualizarEstadoProductoProveedorRequest request
    ) {
        validarEstadoRelacion(request.estado());

        ProveedorProducto relacion = obtenerRelacionProveedorProducto(idProveedor, productoId);
        relacion.setEstado(request.estado().trim().toUpperCase());
        return proveedorProductoRepository.save(relacion);
    }

    @Transactional
    public void eliminarProductoProveedor(Long idProveedor, Long productoId) {
        ProveedorProducto relacion = obtenerRelacionProveedorProducto(idProveedor, productoId);
        proveedorProductoRepository.delete(relacion);
    }

    private void validarCreacionProveedor(CrearProveedorRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de creación del proveedor es obligatoria");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new BusinessException("El nombre del proveedor es obligatorio");
        }
    }

    private void validarActualizacionProveedor(ActualizarProveedorRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de actualización del proveedor es obligatoria");
        }
        if (request.nombre() == null
                && request.nit() == null
                && request.telefono() == null
                && request.email() == null
                && request.contacto() == null
                && request.condicionPago() == null) {
            throw new BusinessException("La solicitud de actualización debe incluir al menos un campo modificable");
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

    private void validarAsociacionProducto(AsociarProductoProveedorRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud para asociar producto al proveedor es obligatoria");
        }
        if (request.productoId() == null) {
            throw new BusinessException("El id del producto es obligatorio");
        }
        if (request.precioReferencia() != null && request.precioReferencia() < 0) {
            throw new BusinessException("El precio de referencia no puede ser negativo");
        }
    }

    private void validarEstadoRelacion(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new BusinessException("El estado de la relación es obligatorio");
        }
        String estadoUpper = estado.trim().toUpperCase();
        if (!estadoUpper.equals("ACTIVO") && !estadoUpper.equals("INACTIVO")) {
            throw new BusinessException("Estado inválido para la relación. Valores válidos: ACTIVO, INACTIVO");
        }
    }

    private Producto obtenerProductoPorId(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe producto con id " + productoId
                ));
    }

    private ProveedorProducto obtenerRelacionProveedorProducto(Long idProveedor, Long productoId) {
        obtenerProveedorPorId(idProveedor);
        return proveedorProductoRepository.findByProveedorIdAndProductoId(idProveedor, productoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe relación entre proveedor " + idProveedor + " y producto " + productoId
                ));
    }

    private ProductoProveedorResponse toProductoProveedorResponse(ProveedorProducto relacion) {
        Producto producto = relacion.getProducto();
        return new ProductoProveedorResponse(
                producto.getUniqueID(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCodigoBarras(),
                producto.getEstado(),
                relacion.getCodigoProductoProveedor(),
                relacion.getPrecioReferencia(),
                relacion.getEstado()
        );
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) return null;
        return texto.trim().replaceAll("\\s+", " ");
    }
}
