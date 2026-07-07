package br.com.rsdata.exception;

/**
 * Lançada quando se tenta cadastrar uma entidade que já existe
 * (mesma chave de negócio: descrição de RamoAtividade ou CNPJ de Empresa).
 */
public class DuplicateEntityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateEntityException(String message) {
        super(message);
    }
}
