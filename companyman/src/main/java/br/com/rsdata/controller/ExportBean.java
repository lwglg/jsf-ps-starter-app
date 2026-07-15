package br.com.rsdata.controller;

import br.com.rsdata.export.EscopoExportacao;
import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.export.OrigemExportacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Managed bean por trás do modal "Exportar dados", compartilhado pelas
 * telas de Empresas e de Ramos de Atividade (ver
 * {@code dialogs/exportar-dados/index.xhtml}).
 *
 * A geração do arquivo e o disparo do diálogo nativo "Salvar Como" do
 * navegador (via File System Access API, em Chrome/Brave/Edge, ou via
 * download padrão em navegadores sem suporte, como o Firefox) acontecem
 * inteiramente no JavaScript da própria tela, que consome
 * {@code br.com.rsdata.servlet.ExportDownloadServlet}. Este bean é
 * {@code @ApplicationScoped} porque só expõe as opções (enums) usadas
 * para popular os `f:selectItems` do formato, da origem e do escopo dos
 * dados — não guarda nenhum estado de requisição.
 */
@Named
@ApplicationScoped
public class ExportBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public OrigemExportacao[] getOrigens() {
        return OrigemExportacao.values();
    }

    public ExportFormat[] getFormatos() {
        return ExportFormat.values();
    }

    public EscopoExportacao[] getEscopos() {
        return EscopoExportacao.values();
    }
}
