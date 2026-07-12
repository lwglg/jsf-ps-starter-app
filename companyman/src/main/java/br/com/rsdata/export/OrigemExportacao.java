package br.com.rsdata.export;

/**
 * Origem dos dados a serem exportados no modal unificado de exportação.
 */
public enum OrigemExportacao {

    EMPRESA("Empresas"),
    RAMO_ATIVIDADE("Ramos de Atividade");

    private final String descricao;

    OrigemExportacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
