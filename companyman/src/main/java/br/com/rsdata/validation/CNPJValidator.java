package br.com.rsdata.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementa o algoritmo oficial de validação de dígitos verificadores do
 * CNPJ (módulo 11), além de recusar sequências conhecidas de dígitos
 * repetidos (ex.: "00.000.000/0000-00", "11.111.111/1111-11"), que são
 * numericamente "válidas" pelo algoritmo mas nunca correspondem a um CNPJ
 * real emitido pela Receita Federal.
 * 
 * Baseado em: https://www.macoratti.net/alg_cnpj.htm
 */
public class CNPJValidator implements ConstraintValidator<CNPJ, String> {

    private static final int[] PESOS_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Campos nulos/vazios são responsabilidade de @NotBlank, não deste validador.
        if (value == null || value.isBlank()) {
            return true;
        }

        String digitos = value.replaceAll("\\D", "");

        if (digitos.length() != 14) {
            return false;
        }

        if (todosDigitosIguais(digitos)) {
            return false;
        }

        int primeiroDigitoVerificador = calcularDigitoVerificador(digitos.substring(0, 12), PESOS_PRIMEIRO_DIGITO);
        int segundoDigitoVerificador = calcularDigitoVerificador(digitos.substring(0, 12) + primeiroDigitoVerificador, PESOS_SEGUNDO_DIGITO);

        String digitosVerificadoresCalculados = "" + primeiroDigitoVerificador + segundoDigitoVerificador;

        return digitos.endsWith(digitosVerificadoresCalculados);
    }

    private boolean todosDigitosIguais(String digitos) {
        char primeiro = digitos.charAt(0);
        
        for (int i = 1; i < digitos.length(); i++) {
            if (digitos.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }

    private int calcularDigitoVerificador(String base, int[] pesos) {
        int soma = 0;
        
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[i];
        }
        
        int resto = soma % 11;
        
        return (resto < 2) ? 0 : 11 - resto;
    }
}
