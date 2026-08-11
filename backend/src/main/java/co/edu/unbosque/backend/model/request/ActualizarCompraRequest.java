package co.edu.unbosque.backend.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Campos editables de una compra recibida. */
public record ActualizarCompraRequest(
        @NotNull(message = "El proveedor es obligatorio") Long idProveedor,
        String numeroFactura,
        String notas,
        @NotEmpty(message = "La compra debe tener al menos un producto")
        @Valid List<DetalleCompraRequest> detalles
) {}
