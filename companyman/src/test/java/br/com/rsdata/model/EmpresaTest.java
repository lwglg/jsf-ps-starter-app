package br.com.rsdata.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Empresa - regras de domínio")
class EmpresaTest {

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
        Empresa e1 = criarEmpresa("12.345.678/0001-90");
        Empresa e2 = criarEmpresa("12.345.678/0001-90");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    @DisplayName("Empresas com CNPJs diferentes não devem ser iguais")
    void naoDeveSeremIguaisComCnpjsDiferentes() {
        Empresa e1 = criarEmpresa("12.345.678/0001-90");
        Empresa e2 = criarEmpresa("98.765.432/0001-10");

        assertNotEquals(e1, e2);
    }

    @Test
    @DisplayName("Igualdade não deve depender do id gerado")
    void igualdadeIndependeDoId() {
        Empresa e1 = criarEmpresa("11.222.333/0001-44");
        e1.setId(UUID.randomUUID());

        Empresa e2 = criarEmpresa("11.222.333/0001-44");
        e2.setId(UUID.randomUUID());

        assertEquals(e1, e2);
    }

    @Test
    @DisplayName("Getters e setters devem refletir os valores atribuídos")
    void gettersSettersFuncionamCorretamente() {
        Empresa empresa = criarEmpresa("55.666.777/0001-88");
        empresa.setFaturamento(new BigDecimal("2500.75"));
        empresa.setTipoEmpresa(TipoEmpresa.SA);

        assertEquals(new BigDecimal("2500.75"), empresa.getFaturamento());
        assertEquals(TipoEmpresa.SA, empresa.getTipoEmpresa());
        assertEquals("Sociedade Anônima", empresa.getTipoEmpresa().getDescricao());
    }
}
