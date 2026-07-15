package br.com.rsdata.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Empresa - regras de domínio")
class EmpresaTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    private Empresa criarEmpresa(String cnpj) {
        Empresa empresa = new Empresa();
        empresa.setNomeFantasia("Empresa Teste");
        empresa.setRazaoSocial("Empresa Teste LTDA");
        empresa.setCnpj(cnpj);
        empresa.setDataFundacao(new Date());
        empresa.setRamoAtividade(new RamoAtividade("Tecnologia da Informação"));
        empresa.setTipoEmpresa(TipoEmpresa.LTDA);
        empresa.setFaturamento(new BigDecimal("1000.00"));

        return empresa;
    }

    @Test
    @DisplayName("Duas empresas com o mesmo CNPJ devem ser iguais")
    void deveSeremIguaisPeloCnpj() {
        Empresa e1 = criarEmpresa("12.345.678/0001-95");
        Empresa e2 = criarEmpresa("12.345.678/0001-95");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    @DisplayName("Empresas com CNPJs diferentes não devem ser iguais")
    void naoDeveSeremIguaisComCnpjsDiferentes() {
        Empresa e1 = criarEmpresa("12.345.678/0001-95");
        Empresa e2 = criarEmpresa("98.765.432/0001-98");

        assertNotEquals(e1, e2);
    }

    @Test
    @DisplayName("Igualdade não deve depender do id gerado")
    void igualdadeIndependeDoId() {
        Empresa e1 = criarEmpresa("11.222.333/0001-81");
        e1.setId(UUID.randomUUID());

        Empresa e2 = criarEmpresa("11.222.333/0001-81");
        e2.setId(UUID.randomUUID());

        assertEquals(e1, e2);
    }

    @Test
    @DisplayName("Getters e setters devem refletir os valores atribuídos")
    void gettersSettersFuncionamCorretamente() {
        Empresa empresa = criarEmpresa("55.666.777/0001-81");
        empresa.setFaturamento(new BigDecimal("2500.75"));
        empresa.setTipoEmpresa(TipoEmpresa.SA);

        assertEquals(new BigDecimal("2500.75"), empresa.getFaturamento());
        assertEquals(TipoEmpresa.SA, empresa.getTipoEmpresa());
        assertEquals("Sociedade Anônima", empresa.getTipoEmpresa().getDescricao());
    }

    @Test
    @DisplayName("Deve ser válida quando todos os campos obrigatórios estão corretos")
    void devidamenteValidaComTodosCamposCorretos() {
        Empresa empresa = criarEmpresa("12.345.678/0001-95");

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        assertTrue(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválida quando campos obrigatórios estão ausentes")
    void invalidaComCamposObrigatoriosAusentes() {
        Empresa empresa = new Empresa();

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        // nomeFantasia, razaoSocial, cnpj, dataFundacao, ramoAtividade, tipoEmpresa, faturamento
        assertTrue(violacoes.size() >= 7);
    }

    @Test
    @DisplayName("Deve ser inválida quando o CNPJ possui dígito verificador incorreto")
    void invalidaComCnpjComDigitoVerificadorIncorreto() {
        Empresa empresa = criarEmpresa("12.345.678/0001-00"); // dígitos verificadores incorretos

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválida quando o CNPJ possui todos os dígitos iguais")
    void invalidaComCnpjDeDigitosRepetidos() {
        Empresa empresa = criarEmpresa("11.111.111/1111-11");

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválida quando o faturamento é negativo")
    void invalidaComFaturamentoNegativo() {
        Empresa empresa = criarEmpresa("23.456.789/0001-95");
        empresa.setFaturamento(new BigDecimal("-1.00"));

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválida quando o faturamento excede 8 dígitos inteiros (precision=10, scale=2)")
    void invalidaComFaturamentoAcimaDaPrecisaoSuportada() {
        Empresa empresa = criarEmpresa("34.567.890/0001-30");
        empresa.setFaturamento(new BigDecimal("125000000.00")); // 9 dígitos inteiros

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválida quando a data de fundação está no futuro")
    void invalidaComDataDeFundacaoFutura() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);

        Empresa empresa = criarEmpresa("45.678.901/0001-75");
        empresa.setDataFundacao(calendar.getTime());

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    @DisplayName("Deve ser inválida quando o ramo de atividade não é informado")
    void invalidaSemRamoDeAtividade() {
        Empresa empresa = criarEmpresa("56.789.012/0001-00");
        empresa.setRamoAtividade(null);

        Set<ConstraintViolation<Empresa>> violacoes = VALIDATOR.validate(empresa);

        assertFalse(violacoes.isEmpty());
    }
}
