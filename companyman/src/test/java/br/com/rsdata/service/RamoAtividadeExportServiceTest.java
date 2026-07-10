package br.com.rsdata.service;

import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.model.RamoAtividade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RamoAtividadeExportService - exportação de ramos de atividade")
class RamoAtividadeExportServiceTest {

    private final RamoAtividadeExportService service = new RamoAtividadeExportService();

    @ParameterizedTest
    @EnumSource(ExportFormat.class)
    @DisplayName("Deve gerar um arquivo não vazio para cada formato suportado")
    void deveGerarArquivoNaoVazioParaCadaFormato(ExportFormat formato) {
        List<RamoAtividade> ramos = List.of(new RamoAtividade("Comércio Varejista"));

        byte[] conteudo = service.exportar(ramos, formato);

        assertTrue(conteudo.length > 0, "o arquivo gerado para " + formato + " não deveria estar vazio");
    }

    @Test
    @DisplayName("CSV exportado deve conter a descrição do ramo de atividade")
    void csvDeveConterDescricaoDoRamo() {
        List<RamoAtividade> ramos = List.of(new RamoAtividade("Construção Civil"));

        byte[] csv = service.exportar(ramos, ExportFormat.CSV);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertTrue(conteudo.contains("Descrição"));
        assertTrue(conteudo.contains("Construção Civil"));
    }
}
