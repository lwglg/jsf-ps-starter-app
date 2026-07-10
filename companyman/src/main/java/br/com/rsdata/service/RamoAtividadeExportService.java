package br.com.rsdata.service;

import br.com.rsdata.export.ExportFormat;
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

    public byte[] exportar(List<RamoAtividade> ramosAtividade, ExportFormat formato) {
        List<String[]> linhas = converterParaLinhas(ramosAtividade);

        switch (formato) {
            case CSV:
                return TabularExporter.paraCsv(CABECALHOS, linhas);
            case XLS:
                return TabularExporter.paraXls("Ramos de Atividade", CABECALHOS, linhas);
            case ODT:
                return TabularExporter.paraOdt(TITULO_RELATORIO, CABECALHOS, linhas);
            case PDF:
                return TabularExporter.paraPdf(TITULO_RELATORIO, CABECALHOS, linhas);
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
