package co.edu.unbosque.backend.model.request;

import java.util.List;


/**
 * Comando de entrada para confirmar una orden de compra.
 * Contiene la información del proveedor, los productos confirmados
 * y observaciones adicionales sobre la orden.
 *
 * @param proveedorId identificador del proveedor asociado a la orden
 * @param items lista de productos confirmados con su cantidad y precio
 * @param observaciones comentarios u observaciones opcionales sobre la orden
 * 
 * @author Angie Tatiana Ortiz
 */
public record OrdenCompraConfirmacionRequest(

    Long proveedorId,

    List<ItemConfirmado> items,

    String observaciones,

    java.time.LocalDateTime fechaEsperada

) {

    /**
     * Representa un item confirmado dentro de la orden de compra.
     * Incluye el producto, la cantidad confirmada y el precio unitario.
     *
     * @param productoId identificador del producto
     * @param cantidad cantidad confirmada del producto
     * @param precioUnitario precio unitario acordado para el producto
     */
    public record ItemConfirmado(

        Long productoId,

        Integer cantidad,

        Double precioUnitario

    ) {}

}