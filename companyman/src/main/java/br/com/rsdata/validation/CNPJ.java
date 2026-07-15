package br.com.rsdata.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida se uma String representa um CNPJ estruturalmente válido, isto é,
 * com 14 dígitos numéricos (ignorando pontuação) e dígitos verificadores
 * corretos, calculados pelo algoritmo oficial (módulo 11).
 *
 * Aceita tanto o formato formatado ("12.345.678/0001-95") quanto apenas
 * os 14 dígitos ("12345678000195").
 */
@Documented
@Constraint(validatedBy = CNPJValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CNPJ {

    String message() default "CNPJ inválido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
