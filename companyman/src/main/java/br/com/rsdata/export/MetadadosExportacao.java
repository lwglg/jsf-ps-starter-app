package br.com.rsdata.export;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Metadados comuns a todo relatório exportado, independentemente do
 * formato: título, quantidade de registros, data/hora de geração e o
 * nome da empresa (usado no cabeçalho de PDF/ODT, junto com o logo).
 */
public final class MetadadosExportacao {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");

    private final String titulo;
    private final int totalRegistros;
    private final LocalDateTime dataHoraGeracao;
    private final String nomeEmpresa;

    private MetadadosExportacao(String titulo, int totalRegistros, LocalDateTime dataHoraGeracao,
                                 String nomeEmpresa) {
        this.titulo = titulo;
        this.totalRegistros = totalRegistros;
        this.dataHoraGeracao = dataHoraGeracao;
        this.nomeEmpresa = nomeEmpresa;
    }

    /**
     * Cria os metadados no momento da exportação: a data/hora é sempre
     * "agora", e o nome da empresa vem da mesma variável de ambiente usada
     * pelo modal "Sobre o sistema" ({@code APP_COMPANY_NAME}).
     */
    public static MetadadosExportacao criar(String titulo, int totalRegistros) {
        return new MetadadosExportacao(titulo, totalRegistros, LocalDateTime.now(), lerNomeEmpresa());
    }

    private static String lerNomeEmpresa() {
        String fromProperty = System.getProperty("APP_COMPANY_NAME");
        
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        
        String value = System.getenv("APP_COMPANY_NAME");
        
        return (value == null || value.isBlank()) ? "Empresa" : value;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getTotalRegistros() {
        return totalRegistros;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    /** Ex.: "Gerado em 12/07/2026 às 14:35:22". */
    public String getTextoGeracao() {
        return "Gerado em " + dataHoraGeracao.format(FORMATO_DATA_HORA);
    }

    /** Ex.: "Total de registros: 5". */
    public String getTextoTotalRegistros() {
        return "Total de registros: " + totalRegistros;
    }
}
