package co.edu.unbosque.backend.exception;

/**
 * Se lanza cuando una operación intenta consumir más stock del disponible.
 *
 * @author Sebastian Cardenas Garcia
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
