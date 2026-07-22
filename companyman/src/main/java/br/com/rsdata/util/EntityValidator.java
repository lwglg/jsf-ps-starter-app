package br.com.rsdata.util;

import br.com.rsdata.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilitário responsável por validar as entidades de domínio (Empresa,
 * RamoAtividade) usando Jakarta Bean Validation (Hibernate Validator),
 * antes de serem persistidas ou atualizadas pela camada de serviço.
 *
 * Centraliza a criação do ValidatorFactory/Validator (que são thread-safe
 * e caros de instanciar) em uma única instância reaproveitada por toda a
 * aplicação.
 */
public final class EntityValidator {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    private EntityValidator() {
    }

    /**
     * Valida a entidade informada de acordo com as anotações de Bean
     * Validation presentes em seus atributos (@NotBlank, @Size, @NotNull,
     * @CNPJ, etc.).
     *
     * @throws ValidationException se houver uma ou mais violações, agregando
     *                              todas as mensagens encontradas.
     */
    public static <T> void validar(T entidade) {
        Set<ConstraintViolation<T>> violacoes = VALIDATOR.validate(entidade);

        if (!violacoes.isEmpty()) {
            throw new ValidationException(
                    violacoes.stream()
                            .map(EntityValidator::formatarViolacao)
                            .collect(Collectors.toList())
            );
        }
    }

    private static <T> String formatarViolacao(ConstraintViolation<T> violacao) {
        return violacao.getPropertyPath() + ": " + violacao.getMessage();
    }
}
