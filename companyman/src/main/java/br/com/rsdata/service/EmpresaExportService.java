package br.com.rsdata.service;

import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.export.TabularExporter;
import br.com.rsdata.model.Empresa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Responsável por converter uma lista de {@link Empresa} em um arquivo nos
 * formatos CSV, XLS, ODT ou PDF, delegando a geração propriamente dita
 * para {@link TabularExporter}.
 */
public class EmpresaExportService {

    private static final String[] CABECALHOS = {
        "Nome Fantasia",
        "Razão Social",
        "CNPJ",
        "Data de Fundação",
        "Ramo de Atividade",
        "Tipo de Empresa",
        "Faturamento (R$)"
    };

    private static final String TITULO_RELATORIO = "Relatório de Empresas";

    public byte[] exportar(List<Empresa> empresas, ExportFormat formato) {
        List<String[]> linhas = converterParaLinhas(empresas);

        switch (formato) {
            case CSV:
                return TabularExporter.paraCsv(CABECALHOS, linhas);
            case XLS:
                return TabularExporter.paraXls("Empresas", CABECALHOS, linhas);
            case ODT:
                return TabularExporter.paraOdt(TITULO_RELATORIO, CABECALHOS, linhas);
            case PDF:
                return TabularExporter.paraPdf(TITULO_RELATORIO, CABECALHOS, linhas);
            default:
                throw new IllegalArgumentException("Formato de exportação não suportado: " + formato);
        }
    }

    private List<String[]> converterParaLinhas(List<Empresa> empresas) {
        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
        List<String[]> linhas = new ArrayList<>();

        for (Empresa empresa : empresas) {
            linhas.add(new String[]{
                empresa.getNomeFantasia(),
                empresa.getRazaoSocial(),
                empresa.getCnpj(),
                empresa.getDataFundacao() != null ? formatoData.format(empresa.getDataFundacao()) : "",
                empresa.getRamoAtividade() != null ? empresa.getRamoAtividade().getDescricao() : "",
                empresa.getTipoEmpresa() != null ? empresa.getTipoEmpresa().getDescricao() : "",
                empresa.getFaturamento() != null
                        ? String.format(Locale.forLanguageTag("pt-BR"), "%,.2f", empresa.getFaturamento())
                        : ""
            });
        }
        return linhas;
    }
}
