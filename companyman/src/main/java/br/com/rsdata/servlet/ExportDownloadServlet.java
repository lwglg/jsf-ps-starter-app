package br.com.rsdata.servlet;

import br.com.rsdata.exception.ExportException;
import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.model.Empresa;
import br.com.rsdata.export.OrigemExportacao;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.service.EmpresaExportService;
import br.com.rsdata.service.EmpresaService;
import br.com.rsdata.service.RamoAtividadeExportService;
import br.com.rsdata.service.RamoAtividadeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet dedicado (fora do ciclo de vida do JSF) que gera e serve os bytes
 * do arquivo exportado, dados os parâmetros {@code origem} e {@code formato}.
 *
 * Não é implementado como uma ação de managed bean porque o
 * JavaScript da tela precisa consumir a resposta via
 * {@code fetch().blob()} para poder repassar o conteúdo ao
 * {@code showSaveFilePicker()} (File System Access API) em navegadores
 * Chromium (Chrome, Brave, Edge) — abrindo o diálogo nativo "Salvar Como"
 * do sistema operacional. Em navegadores sem suporte a essa API (ex.:
 * Firefox), a mesma URL é usada diretamente em um {@code <a download>},
 * caindo no fluxo padrão de download do navegador.
 */
@WebServlet("/export/download")
public class ExportDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final EmpresaService empresaService = new EmpresaService();
    private final RamoAtividadeService ramoAtividadeService = new RamoAtividadeService();
    private final EmpresaExportService empresaExportService = new EmpresaExportService();
    private final RamoAtividadeExportService ramoAtividadeExportService = new RamoAtividadeExportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OrigemExportacao origem;
        ExportFormat formato;
        try {
            origem = OrigemExportacao.valueOf(request.getParameter("origem"));
            formato = ExportFormat.valueOf(request.getParameter("formato"));
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Parâmetros 'origem' e/ou 'formato' ausentes ou inválidos.");
            return;
        }

        byte[] conteudo;
        String prefixoPadrao;

        try {
            if (origem == OrigemExportacao.RAMO_ATIVIDADE) {
                List<RamoAtividade> ramosAtividade = ramoAtividadeService.listarTodos();
            
                conteudo = ramoAtividadeExportService.exportar(ramosAtividade, formato);
                prefixoPadrao = RamoAtividadeExportService.NOME_RELATORIO_FALLBACK;
            } else {
                List<Empresa> empresas = empresaService.listarTodos();
            
                conteudo = empresaExportService.exportar(empresas, formato);
                prefixoPadrao = EmpresaExportService.NORE_RELATORIO_FALLBACK;
            }
        } catch (ExportException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        // Usado apenas como nome padrão de fallback no cabeçalho Content-Disposition
        // (caminho do <a download> no Firefox); no fluxo com File System Access API
        // (Chrome/Brave/Edge), o nome sugerido ao showSaveFilePicker() é definido
        // inteiramente no JavaScript da tela, a partir do campo "Nome do arquivo".
        String nomeArquivoPadrao = prefixoPadrao + "." + formato.getExtensao();

        response.setContentType(formato.getContentType());
        response.setContentLength(conteudo.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivoPadrao + "\"");
        response.getOutputStream().write(conteudo);
        response.getOutputStream().flush();
    }
}
