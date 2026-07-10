package br.com.rsdata.exception;

/**
 * Lançada quando ocorre uma falha inesperada durante a geração de um
 * arquivo de exportação (CSV, XLS, ODT ou PDF).
 */
public class ExportException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
