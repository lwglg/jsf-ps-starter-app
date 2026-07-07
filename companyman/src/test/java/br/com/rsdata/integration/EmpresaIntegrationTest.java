package br.com.rsdata.integration;

import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.model.Empresa;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.model.TipoEmpresa;
import br.com.rsdata.service.EmpresaService;
import br.com.rsdata.service.RamoAtividadeService;
import br.com.rsdata.util.JPAUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração de ponta a ponta: sobe um PostgreSQL real via Testcontainers,
 * exercitando a camada de persistência (Hibernate/JPA), a camada de serviço e as
 * regras de negócio (prevenção de duplicidade) das entidades Empresa e RamoAtividade.
 */
@Testcontainers
class EmpresaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("empresa_db_test")
            .withUsername("empresa_user")
            .withPassword("empresa_pass");

    private RamoAtividadeService ramoAtividadeService;
    private EmpresaService empresaService;

    @BeforeAll
    static void configurarConexao() {
        System.setProperty("DB_HOST", postgres.getHost());
        System.setProperty("DB_PORT", String.valueOf(postgres.getFirstMappedPort()));
        System.setProperty("DB_NAME", postgres.getDatabaseName());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        JPAUtil.reset();
    }

    @AfterAll
    static void encerrarConexao() {
        JPAUtil.close();
    }

    @BeforeEach
    void setUp() {
        ramoAtividadeService = new RamoAtividadeService();
        empresaService = new EmpresaService();
    }

    @Test
    @DisplayName("Deve criar tabelas e persistir um RamoAtividade")
    void devePersistirRamoAtividade() {
        RamoAtividade ramo = ramoAtividadeService.salvar(new RamoAtividade("Educação"));

        assertNotNull(ramo.getId());

        RamoAtividade recuperado = ramoAtividadeService.buscarPorId(ramo.getId());
        assertEquals("Educação", recuperado.getDescricao());
    }

    @Test
    @DisplayName("Não deve permitir cadastrar RamoAtividade duplicado")
    void naoDevePermitirRamoAtividadeDuplicado() {
        ramoAtividadeService.salvar(new RamoAtividade("Saúde e Bem-Estar"));

        assertThrows(DuplicateEntityException.class,
                () -> ramoAtividadeService.salvar(new RamoAtividade("Saúde e Bem-Estar")));
    }

    @Test
    @DisplayName("Deve persistir uma Empresa associada a um RamoAtividade")
    void devePersistirEmpresa() {
        RamoAtividade ramo = ramoAtividadeService.salvar(new RamoAtividade("Logística"));

        Empresa empresa = new Empresa();
        empresa.setNomeFantasia("Rota Certa Transportes");
        empresa.setRazaoSocial("Rota Certa Transportes e Logística LTDA");
        empresa.setCnpj("11.111.111/0001-11");
        empresa.setDataFundacao(new Date());
        empresa.setRamoAtividade(ramo);
        empresa.setTipoEmpresa(TipoEmpresa.LTDA);
        empresa.setFaturamento(new BigDecimal("999999.99"));

        Empresa salva = empresaService.salvar(empresa);
        assertNotNull(salva.getId());

        Empresa recuperada = empresaService.buscarPorId(salva.getId());
        assertEquals("Rota Certa Transportes", recuperada.getNomeFantasia());
        assertEquals(TipoEmpresa.LTDA, recuperada.getTipoEmpresa());
    }

    @Test
    @DisplayName("Não deve permitir cadastrar Empresa com CNPJ duplicado")
    void naoDevePermitirEmpresaDuplicada() {
        RamoAtividade ramo = ramoAtividadeService.salvar(new RamoAtividade("Turismo"));

        Empresa empresa1 = new Empresa();
        empresa1.setNomeFantasia("Viaje Mais");
        empresa1.setRazaoSocial("Viaje Mais Agência de Turismo LTDA");
        empresa1.setCnpj("22.222.222/0001-22");
        empresa1.setDataFundacao(new Date());
        empresa1.setRamoAtividade(ramo);
        empresa1.setTipoEmpresa(TipoEmpresa.MEI);
        empresa1.setFaturamento(new BigDecimal("50000.00"));
        empresaService.salvar(empresa1);

        Empresa empresa2 = new Empresa();
        empresa2.setNomeFantasia("Viaje Mais Filial");
        empresa2.setRazaoSocial("Viaje Mais Agência de Turismo LTDA");
        empresa2.setCnpj("22.222.222/0001-22");
        empresa2.setDataFundacao(new Date());
        empresa2.setRamoAtividade(ramo);
        empresa2.setTipoEmpresa(TipoEmpresa.MEI);
        empresa2.setFaturamento(new BigDecimal("50000.00"));

        assertThrows(DuplicateEntityException.class, () -> empresaService.salvar(empresa2));
    }

    @Test
    @DisplayName("Deve remover uma Empresa e não mais encontrá-la")
    void deveRemoverEmpresa() {
        RamoAtividade ramo = ramoAtividadeService.salvar(new RamoAtividade("Eventos"));

        Empresa empresa = new Empresa();
        empresa.setNomeFantasia("Festa Boa Eventos");
        empresa.setRazaoSocial("Festa Boa Eventos LTDA");
        empresa.setCnpj("33.333.333/0001-33");
        empresa.setDataFundacao(new Date());
        empresa.setRamoAtividade(ramo);
        empresa.setTipoEmpresa(TipoEmpresa.EIRELI);
        empresa.setFaturamento(new BigDecimal("75000.00"));

        Empresa salva = empresaService.salvar(empresa);
        UUID id = salva.getId();
        String idString = id.toString();

        empresaService.remover(idString);

        assertEquals(null, empresaService.buscarPorId(id));
    }

    @Test
    @DisplayName("Deve listar todas as empresas cadastradas com o ramo de atividade carregado")
    void deveListarEmpresas() {
        RamoAtividade ramo = ramoAtividadeService.salvar(new RamoAtividade("Energia Renovável"));

        Empresa empresa = new Empresa();
        empresa.setNomeFantasia("Sol Nascente Energia");
        empresa.setRazaoSocial("Sol Nascente Energia Solar S.A.");
        empresa.setCnpj("44.444.444/0001-44");
        empresa.setDataFundacao(new Date());
        empresa.setRamoAtividade(ramo);
        empresa.setTipoEmpresa(TipoEmpresa.SA);
        empresa.setFaturamento(new BigDecimal("10000000.00"));
        empresaService.salvar(empresa);

        List<Empresa> todas = empresaService.listarTodos();
        assertTrue(todas.stream().anyMatch(e -> e.getCnpj().equals("44.444.444/0001-44")));
    }
}
