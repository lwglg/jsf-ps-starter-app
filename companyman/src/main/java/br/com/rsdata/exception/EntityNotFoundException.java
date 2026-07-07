package br.com.rsdata.exception;

/**
 * Lançada quando uma entidade buscada por id não é encontrada.
 */
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}
