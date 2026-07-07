package br.com.rsdata.model;

/**
 * Enumeração dos tipos de empresa suportados pelo sistema.
 * Persistido como STRING (EnumType.STRING) na coluna tipo_empresa.
 */
public enum TipoEmpresa {

    MEI("Microempreendedor Individual"),
    EIRELI("Empresa Individual de Responsabilidade Limitada"),
    LTDA("Sociedade Limitada"),
    SA("Sociedade Anônima");

    private final String descricao;

    TipoEmpresa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
