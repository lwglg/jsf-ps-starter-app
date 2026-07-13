package br.com.rsdata.service;

import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.export.MetadadosExportacao;
import br.com.rsdata.export.TabularExporter;
import br.com.rsdata.model.RamoAtividade;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por converter uma lista de {@link RamoAtividade} em um
 * arquivo nos formatos CSV, XLS, ODT ou PDF, delegando a geração
 * propriamente dita para {@link TabularExporter}.
 */
public class RamoAtividadeExportService {

    private static final String[] CABECALHOS = {"Descrição"};

    private static final String TITULO_RELATORIO = "Relatório de Ramos de Atividade";
    public static final String NOME_RELATORIO_FALLBACK = "relatorio-ramos-atividade";

    public byte[] exportar(List<RamoAtividade> ramosAtividade, ExportFormat formato) {
        List<String[]> linhas = converterParaLinhas(ramosAtividade);
        MetadadosExportacao metadados = MetadadosExportacao.criar(TITULO_RELATORIO, linhas.size());

        switch (formato) {
            case CSV:
                return TabularExporter.paraCsv(metadados, CABECALHOS, linhas);
            case XLS:
                return TabularExporter.paraXls(metadados, "Ramos de Atividade", CABECALHOS, linhas);
            case ODT:
                return TabularExporter.paraOdt(metadados, CABECALHOS, linhas);
            case PDF:
                return TabularExporter.paraPdf(metadados, CABECALHOS, linhas);
            default:
                throw new IllegalArgumentException("Formato de exportação não suportado: " + formato);
        }
    }

    private List<String[]> converterParaLinhas(List<RamoAtividade> ramosAtividade) {
        List<String[]> linhas = new ArrayList<>();
        for (RamoAtividade ramo : ramosAtividade) {
            linhas.add(new String[]{ramo.getDescricao()});
        }
        return linhas;
    }
}
