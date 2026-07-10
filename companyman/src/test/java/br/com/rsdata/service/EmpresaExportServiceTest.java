package br.com.rsdata.service;

import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.model.Empresa;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.model.TipoEmpresa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EmpresaExportService - exportação de empresas")
class EmpresaExportServiceTest {

    private final EmpresaExportService service = new EmpresaExportService();

    private Empresa criarEmpresaExemplo() {
        RamoAtividade ramo = new RamoAtividade("Tecnologia da Informação");

        Empresa empresa = new Empresa();
        
        empresa.setNomeFantasia("TechNova Soluções");
        empresa.setRazaoSocial("TechNova Soluções em TI EIRELI");
        empresa.setCnpj("23.456.789/0001-95");
        empresa.setDataFundacao(new Date());
        empresa.setRamoAtividade(ramo);
        empresa.setTipoEmpresa(TipoEmpresa.EIRELI);
        empresa.setFaturamento(new BigDecimal("360000.50"));
        
        return empresa;
    }

    @ParameterizedTest
    @EnumSource(ExportFormat.class)
    @DisplayName("Deve gerar um arquivo não vazio para cada formato suportado")
    void deveGerarArquivoNaoVazioParaCadaFormato(ExportFormat formato) {
        byte[] conteudo = service.exportar(List.of(criarEmpresaExemplo()), formato);

        assertTrue(conteudo.length > 0, "o arquivo gerado para " + formato + " não deveria estar vazio");
    }

    @Test
    @DisplayName("CSV exportado deve conter os dados da empresa")
    void csvDeveConterDadosDaEmpresa() {
        byte[] csv = service.exportar(List.of(criarEmpresaExemplo()), ExportFormat.CSV);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertTrue(conteudo.contains("TechNova Soluções"));
        assertTrue(conteudo.contains("23.456.789/0001-95"));
        assertTrue(conteudo.contains("Tecnologia da Informação"));
        assertTrue(conteudo.contains("Empresa Individual de Responsabilidade Limitada"));
    }

    @Test
    @DisplayName("Deve gerar arquivo vazio (apenas cabeçalho) quando não há empresas cadastradas")
    void deveGerarArquivoComApenasCabecalhoQuandoListaVazia() {
        byte[] csv = service.exportar(List.of(), ExportFormat.CSV);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertTrue(conteudo.contains("Nome Fantasia"));
        assertTrue(conteudo.contains("Faturamento"));
    }
}
