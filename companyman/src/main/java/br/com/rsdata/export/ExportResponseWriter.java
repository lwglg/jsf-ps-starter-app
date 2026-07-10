package br.com.rsdata.export;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import br.com.rsdata.exception.ExportException;

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

    public static void escreverDownload(byte[] conteudo, String nomeArquivo, String contentType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        try {
            externalContext.responseReset();
            externalContext.setResponseContentType(contentType);
            externalContext.setResponseCharacterEncoding(StandardCharsets.UTF_8.name());
            externalContext.setResponseContentLength(conteudo.length);
            externalContext.setResponseHeader(
                "Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"");

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
