package br.com.rsdata.exception;

import java.util.Collections;
import java.util.List;

/**
 * Lançada quando uma entidade não atende às restrições de validação
 * (Jakarta Bean Validation) antes de ser persistida ou atualizada.
 * Agrega todas as mensagens de violação encontradas, para que a camada
 * de apresentação possa exibi-las de uma só vez ao usuário.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> violacoes;

    public ValidationException(List<String> violacoes) {
        super(String.join(" | ", violacoes));
        this.violacoes = Collections.unmodifiableList(violacoes);
    }

    public List<String> getViolacoes() {
        return violacoes;
    }
}
