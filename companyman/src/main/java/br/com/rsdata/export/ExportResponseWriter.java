package br.com.rsdata.export;

import br.com.rsdata.exception.ExportException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Escreve um arquivo gerado em memória diretamente na resposta HTTP da
 * requisição JSF atual, como um download (Content-Disposition: attachment),
 * encerrando o ciclo de vida do Faces logo em seguida com
 * {@code responseComplete()}.
 *
 * Deve ser chamado a partir de uma ação/actionListener disparada por um
 * componente com {@code ajax="false"} (ex.: {@code p:commandButton
 * ajax="false"}), já que a resposta precisa substituir a página inteira
 * pelo conteúdo binário do arquivo.
 */
public final class ExportResponseWriter {

    private ExportResponseWriter() {
    }

    public static String constroiNomeArquivoFinal(String nomeArquivo, String nomeArquivoFallback, ExportFormat formato) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            nomeArquivo = nomeArquivoFallback;
        }

        LocalDateTime dataHora = LocalDateTime.of(LocalDate.now(), LocalTime.now());

        DateTimeFormatter formatoSufixoDataHora = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

        String sufixoDataHora = dataHora.format(formatoSufixoDataHora);
        String nomeArquivoFinal = nomeArquivo +  "_" +  sufixoDataHora + "." + formato.getExtensao();

        return nomeArquivoFinal;
    }

    public static void escreverDownload(byte[] conteudo, String nomeArquivo, String nomeArquivoFallback, ExportFormat formato) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        try {
            String nomeArquivoFinal = ExportResponseWriter.constroiNomeArquivoFinal(
                nomeArquivo,
                nomeArquivoFallback,
                formato
            );

            externalContext.responseReset();
            externalContext.setResponseContentType(formato.getContentType());
            externalContext.setResponseCharacterEncoding(StandardCharsets.UTF_8.name());
            externalContext.setResponseContentLength(conteudo.length);
            externalContext.setResponseHeader(
                "Content-Disposition", "attachment; filename=\"" + nomeArquivoFinal + "\"");

            try (OutputStream saida = externalContext.getResponseOutputStream()) {
                saida.write(conteudo);
                saida.flush();
            }
        } catch (IOException e) {
            throw new ExportException("Falha ao escrever o arquivo de exportação na resposta HTTP.", e);
        } finally {
            facesContext.responseComplete();
        }
    }
}
