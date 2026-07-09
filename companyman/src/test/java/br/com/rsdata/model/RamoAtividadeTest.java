package br.com.rsdata.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RamoAtividade - regras de domínio")
class RamoAtividadeTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Test
    @DisplayName("Duas instâncias com a mesma descrição (case-insensitive) devem ser iguais")
    void deveSeremIguaisPelaDescricao() {
        RamoAtividade r1 = new RamoAtividade("Tecnologia da Informação");
        RamoAtividade r2 = new RamoAtividade("tecnologia da informação");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("Instâncias com descrições diferentes não devem ser iguais")
    void naoDeveSeremIguaisComDescricoesDiferentes() {
        RamoAtividade r1 = new RamoAtividade("Tecnologia da Informação");
        RamoAtividade r2 = new RamoAtividade("Comércio Varejista");

        assertNotEquals(r1, r2);
    }

    @Test
    @DisplayName("Igualdade não deve depender do id gerado")
    void igualdadeIndependeDoId() {
        RamoAtividade r1 = new RamoAtividade("Construção Civil");
        r1.setId(UUID.randomUUID());

        RamoAtividade r2 = new RamoAtividade("Construção Civil");
        r2.setId(UUID.randomUUID());

        assertEquals(r1, r2);
    }

    @Test
    @DisplayName("Getters e setters devem funcionar corretamente")
    void gettersSettersFuncionaisCorretamente() {
        RamoAtividade ramo = new RamoAtividade();
        UUID id = UUID.randomUUID();
        ramo.setId(id);
        ramo.setDescricao("Serviços de Consultoria");

        assertEquals(id, ramo.getId());
        assertEquals("Serviços de Consultoria", ramo.getDescricao());
    }

    @Test
    @DisplayName("Deve ser válido quando a descrição atende às restrições (3 a 150 caracteres)")
    void devidamenteValidoComDescricaoAdequada() {
        RamoAtividade ramo = new RamoAtividade("Tecnologia da Informação");

        Set<ConstraintViolation<RamoAtividade>> violacoes = VALIDATOR.validate(ramo);

        assertTrue(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválido quando a descrição está em branco")
    void invalidoComDescricaoEmBranco() {
        RamoAtividade ramo = new RamoAtividade("   ");

        Set<ConstraintViolation<RamoAtividade>> violacoes = VALIDATOR.validate(ramo);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválido quando a descrição é nula")
    void invalidoComDescricaoNula() {
        RamoAtividade ramo = new RamoAtividade(null);

        Set<ConstraintViolation<RamoAtividade>> violacoes = VALIDATOR.validate(ramo);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválido quando a descrição possui menos de 3 caracteres")
    void invalidoComDescricaoMuitoCurta() {
        RamoAtividade ramo = new RamoAtividade("TI");

        Set<ConstraintViolation<RamoAtividade>> violacoes = VALIDATOR.validate(ramo);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválido quando a descrição excede 150 caracteres")
    void invalidoComDescricaoMuitoLonga() {
        RamoAtividade ramo = new RamoAtividade("a".repeat(151));

        Set<ConstraintViolation<RamoAtividade>> violacoes = VALIDATOR.validate(ramo);

        assertFalse(violacoes.isEmpty());
    }
}
