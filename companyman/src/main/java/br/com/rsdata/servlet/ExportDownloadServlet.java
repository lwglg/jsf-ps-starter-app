package br.com.rsdata.servlet;

import br.com.rsdata.controller.EmpresaBean;
import br.com.rsdata.controller.RamoAtividadeBean;
import br.com.rsdata.exception.ExportException;
import br.com.rsdata.export.EscopoExportacao;
import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.export.OrigemExportacao;
import br.com.rsdata.model.Empresa;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.service.EmpresaExportService;
import br.com.rsdata.service.EmpresaService;
import br.com.rsdata.service.RamoAtividadeExportService;
import br.com.rsdata.service.RamoAtividadeService;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet dedicado (fora do ciclo de vida do JSF) que gera e serve os bytes
 * do arquivo exportado, dados os parâmetros {@code origem}, {@code formato}
 * e {@code escopo}.
 *
 * Não é implementado como uma ação de managed bean porque o
 * JavaScript da tela precisa consumir a resposta via
 * {@code fetch().blob()} para poder repassar o conteúdo ao
 * {@code showSaveFilePicker()} (File System Access API) em navegadores
 * Chromium (Chrome, Brave, Edge) — abrindo o diálogo nativo "Salvar Como"
 * do sistema operacional. Em navegadores sem suporte a essa API (ex.:
 * Firefox), a mesma URL é usada diretamente em um {@code <a download>},
 * caindo no fluxo padrão de download do navegador.
 *
 * O escopo de exportação ("todos os registros" / "somente selecionados" /
 * "somente a página atual") depende do estado atual da tabela na tela de
 * origem — por isso {@link EmpresaBean} e {@link RamoAtividadeBean} (que
 * expõem a seleção e a posição de paginação do {@code p:dataTable}) são
 * injetados via CDI: o Weld (configurado em {@code web.xml}) permite
 * {@code @Inject} em servlets comuns, não apenas em managed beans JSF.
 */
@WebServlet("/export/download")
public class ExportDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ExportDownloadServlet.class);

    private final EmpresaService empresaService = new EmpresaService();
    private final RamoAtividadeService ramoAtividadeService = new RamoAtividadeService();
    private final EmpresaExportService empresaExportService = new EmpresaExportService();
    private final RamoAtividadeExportService ramoAtividadeExportService = new RamoAtividadeExportService();

    @Inject
    private EmpresaBean empresaBean;

    @Inject
    private RamoAtividadeBean ramoAtividadeBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OrigemExportacao origem;
        ExportFormat formato;
        EscopoExportacao escopo;

        try {
            origem = OrigemExportacao.valueOf(request.getParameter("origem"));
            formato = ExportFormat.valueOf(request.getParameter("formato"));
            escopo = parseEscopo(request.getParameter("escopo"));
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Parâmetros 'origem', 'formato' e/ou 'escopo' ausentes ou inválidos.");

            return;
        }

        byte[] conteudo;
        String prefixoPadrao;

        try {
            logger.info("Origem {} \t Escopo: {} \tFormato {} ", origem.toString(), escopo.toString(), formato.toString());

            if (origem == OrigemExportacao.RAMO_ATIVIDADE) {
                List<RamoAtividade> ramosAtividade = resolverRamosAtividade(escopo);

                logger.info("{} ramos de atividade resolvidos: {}", ramosAtividade.size(), ramosAtividade.toString());

                conteudo = ramoAtividadeExportService.exportar(ramosAtividade, formato);
                prefixoPadrao = EmpresaExportService.NOME_RELATORIO_FALLBACK;
            } else {
                List<Empresa> empresas = resolverEmpresas(escopo);

                logger.info("{} empresas resolvidas: {}", empresas.size(), empresas.toString());

                conteudo = empresaExportService.exportar(empresas, formato);
                prefixoPadrao = RamoAtividadeExportService.NOME_RELATORIO_FALLBACK;
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

    /**
     * O parâmetro "escopo" é opcional na URL (ausência = TODOS), diferente
     * de "origem"/"formato", que são sempre obrigatórios.
     */
    private EscopoExportacao parseEscopo(String valor) {
        if (valor == null || valor.isBlank()) {
            return EscopoExportacao.TODOS;
        }

        return EscopoExportacao.valueOf(valor);
    }

    private List<Empresa> resolverEmpresas(EscopoExportacao escopo) {
        switch (escopo) {
            case SELECIONADOS:
                return empresaBean.getSelecionados();
            case PAGINA_ATUAL:
                return empresaBean.getRegistrosDaPaginaAtual();
            default:
                return empresaService.listarTodos();
        }
    }

    private List<RamoAtividade> resolverRamosAtividade(EscopoExportacao escopo) {
        switch (escopo) {
            case SELECIONADOS:
                return ramoAtividadeBean.getSelecionados();
            case PAGINA_ATUAL:
                return ramoAtividadeBean.getRegistrosDaPaginaAtual();
            default:
                return ramoAtividadeService.listarTodos();
        }
    }
}
