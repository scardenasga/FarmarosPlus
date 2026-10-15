package co.edu.unbosque.backend.exception;

/**
 * Indica que un recurso requerido no existe en la base de datos.
 *
 * @author Sebastian Cardenas Garcia
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
