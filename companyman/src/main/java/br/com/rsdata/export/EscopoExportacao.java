package br.com.rsdata.export;

/**
 * Escopo de registros a exportar no modal "Exportar dados": todos os
 * registros, apenas os selecionados na tabela (checkboxes), ou apenas os
 * da página atualmente exibida na tabela paginada.
 */
public enum EscopoExportacao {

    TODOS("Todos os registros"),
    SELECIONADOS("Somente os selecionados na tabela"),
    PAGINA_ATUAL("Somente a página atual da tabela");

    private final String descricao;

    EscopoExportacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
