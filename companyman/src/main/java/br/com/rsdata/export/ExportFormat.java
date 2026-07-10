package br.com.rsdata.export;

/**
 * Formatos de exportação de dados suportados pelas telas de listagem
 * (Empresa e RamoAtividade).
 */
public enum ExportFormat {

    CSV("csv", "text/csv"),
    XLS("xls", "application/vnd.ms-excel"),
    ODT("odt", "application/vnd.oasis.opendocument.text"),
    PDF("pdf", "application/pdf");

    private final String extensao;
    private final String contentType;

    ExportFormat(String extensao, String contentType) {
        this.extensao = extensao;
        this.contentType = contentType;
    }

    public String getExtensao() {
        return extensao;
    }

    public String getContentType() {
        return contentType;
    }
}
