package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.CompraProveedor;
import co.edu.unbosque.backend.model.entity.DetalleCompraProveedor;
import co.edu.unbosque.backend.model.entity.Producto;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.request.DetalleCompraRequest;
import co.edu.unbosque.backend.model.request.RegistrarCompraRequest;
import co.edu.unbosque.backend.model.response.CompraResponse;
import co.edu.unbosque.backend.model.response.DetalleCompraResponse;
import co.edu.unbosque.backend.repository.CompraProveedorRepository;
import co.edu.unbosque.backend.repository.ProductoRepository;
import co.edu.unbosque.backend.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para registro y consulta de compras a proveedores.
 *
 * @author juanjo2748
 */
@Service
public class CompraService {

    private final CompraProveedorRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;

    public CompraService(CompraProveedorRepository compraRepository,
                         ProveedorRepository proveedorRepository,
                         ProductoRepository productoRepository) {
        this.compraRepository = compraRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public CompraResponse registrar(RegistrarCompraRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.idProveedor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el proveedor con id " + request.idProveedor()));

        CompraProveedor compra = new CompraProveedor();
        compra.setProveedor(proveedor);
        compra.setFechaRecepcion(LocalDateTime.now());
        compra.setUsuarioResponsable(request.usuarioResponsable());
        compra.setNumeroFactura(request.numeroFactura());
        compra.setNotas(request.notas());

        List<DetalleCompraProveedor> detalles = new ArrayList<>();
        double total = 0.0;

        for (DetalleCompraRequest detalleReq : request.detalles()) {
            Producto producto = productoRepository.findById(detalleReq.idProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el producto con id " + detalleReq.idProducto()));

            double subtotal = detalleReq.precioUnitario() * detalleReq.cantidad();
            total += subtotal;

            DetalleCompraProveedor detalle = new DetalleCompraProveedor();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(detalleReq.cantidad());
            detalle.setPrecioUnitario(detalleReq.precioUnitario());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
        }

        compra.setDetalles(detalles);
        compra.setTotal(total);
        return toResponse(compraRepository.save(compra));
    }

    @Transactional(readOnly = true)
    public List<CompraResponse> listar(Long idProveedor) {
        List<CompraProveedor> compras = (idProveedor != null)
                ? compraRepository.findByProveedor_IdProveedorOrderByFechaRecepcionDesc(idProveedor)
                : compraRepository.findAllByOrderByFechaRecepcionDesc();
        return compras.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CompraResponse obtener(Long id) {
        CompraProveedor compra = compraRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la compra con id " + id));
        return toResponse(compra);
    }

    private CompraResponse toResponse(CompraProveedor c) {
        List<DetalleCompraResponse> detallesResp = c.getDetalles().stream()
                .map(d -> new DetalleCompraResponse(
                        d.getProducto().getUniqueID(),
                        d.getNombreProducto(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()))
                .toList();
        return new CompraResponse(
                c.getIdCompra(),
                c.getProveedor().getIdProveedor(),
                c.getProveedor().getNombre(),
                c.getUsuarioResponsable(),
                c.getNumeroFactura(),
                c.getNotas(),
                c.getFechaRecepcion(),
                c.getTotal(),
                detallesResp);
    }
}
