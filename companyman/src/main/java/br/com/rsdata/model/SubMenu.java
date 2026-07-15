package br.com.rsdata.model;

/**
 * Enumeração dos ítens de menu suportados pelo menu na
 * barra lateral do layout principal.
 */
public enum SubMenu {

    CADASTRO("Cadastros", "pi pi-folder"),
    OPERACOES("Operações", "pi pi-cog"),
    SISTEMA("Sistema", "pi pi-info-circle");

    private final String titulo;
    private final String icone;

    SubMenu(String titulo, String icone) {
        this.titulo = titulo;
        this.icone = icone;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getIcone() {
        return icone;
    }
}
