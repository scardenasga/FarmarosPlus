package co.edu.unbosque.backend.exception;

/**
 * Excepción base para reglas de negocio del dominio.
 * Al ser unchecked, permite que Spring revierta la transacción automáticamente.
 *
 * @author Sebastian Cardenas Garcia
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
