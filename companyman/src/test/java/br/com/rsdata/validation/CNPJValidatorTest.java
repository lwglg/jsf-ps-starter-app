package br.com.rsdata.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CNPJValidator - validação de dígitos verificadores")
class CNPJValidatorTest {

    // O CNPJValidator não invoca nenhum método do ConstraintValidatorContext,
    // portanto null é suficiente aqui e evita a necessidade de uma biblioteca
    // de mocking apenas para este teste.
    private static final ConstraintValidatorContext CONTEXT = null;

    private CNPJValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CNPJValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12.345.678/0001-95",
            "23.456.789/0001-95",
            "34.567.890/0001-30",
            "45.678.901/0001-75",
            "56.789.012/0001-00",
            "12345678000195" // sem formatação também deve ser aceito
    })
    @DisplayName("Deve aceitar CNPJs com dígitos verificadores corretos")
    void deveAceitarCnpjsValidos(String cnpj) {
        assertTrue(validator.isValid(cnpj, CONTEXT));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12.345.678/0001-90", // dígitos verificadores incorretos (correto é 95)
            "11.111.111/1111-11", // sequência de dígitos repetidos
            "00.000.000/0000-00", // sequência de dígitos repetidos
            "12.345.678/0001-9",  // tamanho inválido (13 dígitos)
            "abc.def.ghi/jklm-no" // não numérico
    })
    @DisplayName("Deve rejeitar CNPJs inválidos")
    void deveRejeitarCnpjsInvalidos(String cnpj) {
        assertFalse(validator.isValid(cnpj, CONTEXT));
    }

    @Test
    @DisplayName("Deve considerar válido (delegando para @NotBlank) um valor nulo ou em branco")
    void deveDelegarNuloOuEmBrancoParaOutroValidador() {
        assertTrue(validator.isValid(null, CONTEXT));
        assertTrue(validator.isValid("", CONTEXT));
        assertTrue(validator.isValid("   ", CONTEXT));
    }
}
