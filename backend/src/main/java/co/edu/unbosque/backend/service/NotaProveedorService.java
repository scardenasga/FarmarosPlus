package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.NotaProveedor;
import co.edu.unbosque.backend.model.entity.OrdenCompra;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.CrearNotaProveedorRequest;
import co.edu.unbosque.backend.repository.NotaProveedorRepository;
import co.edu.unbosque.backend.repository.OrdenCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación para reclamos y observaciones de proveedor.
 */
@Service
public class NotaProveedorService {

    private final NotaProveedorRepository notaProveedorRepository;
    private final ProveedorService proveedorService;
    private final OrdenCompraRepository ordenCompraRepository;

    public NotaProveedorService(
            NotaProveedorRepository notaProveedorRepository,
            ProveedorService proveedorService,
            OrdenCompraRepository ordenCompraRepository
    ) {
        this.notaProveedorRepository = notaProveedorRepository;
        this.proveedorService = proveedorService;
        this.ordenCompraRepository = ordenCompraRepository;
    }

    @Transactional
    public NotaProveedor crearNota(Long idProveedor, CrearNotaProveedorRequest request) {
        validarCreacionNota(request);

        Proveedor proveedor = proveedorService.obtenerProveedorPorId(idProveedor);

        NotaProveedor nota = new NotaProveedor();
        nota.setProveedor(proveedor);
        nota.setTipoNota(request.tipoNota().trim().toUpperCase());
        nota.setTitulo(request.titulo().trim());
        nota.setDescripcion(request.descripcion().trim());

        if (request.idOrdenRelacionada() != null) {
            OrdenCompra orden = ordenCompraRepository.findById(request.idOrdenRelacionada())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe la orden de compra con id " + request.idOrdenRelacionada()
                    ));
            nota.setOrdenRelacionada(orden);
        }

        return notaProveedorRepository.save(nota);
    }

    @Transactional(readOnly = true)
    public List<NotaProveedor> listarPorProveedor(Long idProveedor) {
        proveedorService.obtenerProveedorPorId(idProveedor);
        return notaProveedorRepository.findByProveedor_IdProveedorOrderByFechaCreacionDesc(idProveedor);
    }

    private void validarCreacionNota(CrearNotaProveedorRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud para registrar la nota es obligatoria");
        }
        if (request.tipoNota() == null || request.tipoNota().isBlank()) {
            throw new BusinessException("El tipo de nota es obligatorio");
        }
        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new BusinessException("El título de la nota es obligatorio");
        }
        if (request.descripcion() == null || request.descripcion().isBlank()) {
            throw new BusinessException("La descripción de la nota es obligatoria");
        }
    }
}
