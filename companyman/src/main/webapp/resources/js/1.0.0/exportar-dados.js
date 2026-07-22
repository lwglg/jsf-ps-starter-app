/**
 * Lógica do modal "Exportar dados" (dialogs/exportar-dados/index.xhtml).
 *
 * Gera o arquivo (via com.empresa.export.ExportDownloadServlet) e dispara
 * o "Salvar Como" nativo do navegador:
 *   - Chrome / Brave / Edge (Chromium): usa a File System Access API
 *     (window.showSaveFilePicker) para abrir o diálogo nativo real de
 *     "Salvar Como" do sistema operacional.
 *   - Firefox e demais navegadores sem suporte a essa API: cai para o
 *     fluxo padrão de download via <a download>, que só abre "Salvar Como"
 *     se o usuário tiver essa opção habilitada nas configurações de
 *     download do próprio navegador (limitação da plataforma web, não
 *     controlável pelo servidor).
 *
 * Depende da variável global COMPANYMAN_EXPORT_CONTEXT_PATH, definida
 * inline em dialogs/exportar-dados/index.xhtml (único trecho que precisa
 * de EL do Facelets, já que este arquivo é um recurso estático e não passa
 * pelo processamento do Facelets).
 *
 * O parâmetro "escopo" (TODOS / SELECIONADOS / PAGINA_ATUAL) é lido do
 * <select id="expEscopo"> e repassado ao servlet, que resolve os
 * registros a exportar consultando o estado atual (seleção/paginação) do
 * p:dataTable da tela de origem, via EmpresaBean/RamoAtividadeBean.
 */

function exportarDadosSelecionados() {
    var origem = document.getElementById('expOrigem').value;
    var formato = document.getElementById('expFormato').value;
    var escopo = document.getElementById('expEscopo').value;
    var nomeInformado = document.getElementById('expNomeArquivo').value.trim();

    var extensoesPorFormato = {
        CSV: 'csv',
        XLS: 'xls',
        ODT: 'odt',
        PDF: 'pdf',
    };

    var extensao = extensoesPorFormato[formato] || 'dat';

    var nomeArquivo = nomeInformado;

    if (!nomeArquivo) {
        var prefixoPadrao =
            origem === 'RAMO_ATIVIDADE' ? 'ramos-de-atividade' : 'empresas';
        nomeArquivo = prefixoPadrao + '_' + gerarTimestamp();
    }

    if (
        nomeArquivo.toLowerCase().indexOf('.' + extensao) !==
        nomeArquivo.length - extensao.length - 1
    ) {
        nomeArquivo += '.' + extensao;
    }

    var contextPath = window.COMPANYMAN_EXPORT_CONTEXT_PATH || '';
    var url =
        window.location.protocol +
        '//' +
        window.location.host +
        contextPath +
        '/export/download' +
        '?origem=' +
        encodeURIComponent(origem) +
        '&formato=' +
        encodeURIComponent(formato) +
        '&escopo=' +
        encodeURIComponent(escopo);

    exibirStatus('Gerando arquivo...', false);

    if (window.showSaveFilePicker) {
        // Chrome, Brave, Edge (Chromium): File System Access API —
        // abre o diálogo nativo "Salvar Como" do sistema operacional.
        fetch(url)
            .then(function (resposta) {
                if (!resposta.ok) {
                    throw new Error(
                        'Falha ao gerar o arquivo (HTTP ' +
                            resposta.status +
                            ').',
                    );
                }
                return resposta.blob();
            })
            .then(function (blob) {
                return window
                    .showSaveFilePicker({ suggestedName: nomeArquivo })
                    .then(function (handle) {
                        return handle
                            .createWritable()
                            .then(function (writable) {
                                return writable.write(blob).then(function () {
                                    return writable.close();
                                });
                            });
                    });
            })
            .then(function () {
                exibirStatus('Arquivo exportado com sucesso.', false);
                PF('dlgExportarDados').hide();
            })
            .catch(function (erro) {
                if (erro && erro.name === 'AbortError') {
                    // Usuário cancelou o diálogo "Salvar Como" — não é um erro real.
                    exibirStatus('', true);
                    return;
                }
                exibirStatus('Erro ao exportar: ' + erro.message, false, true);
            });
    } else {
        // Firefox e demais navegadores sem suporte à File System Access API:
        // download padrão do navegador.
        var link = document.createElement('a');

        link.href = url;
        link.download = nomeArquivo;

        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);

        exibirStatus('Download iniciado.', false);
        PF('dlgExportarDados').hide();
    }
}

function gerarTimestamp() {
    var agora = new Date();
    var pad = function (numero) {
        return (numero < 10 ? '0' : '') + numero;
    };

    return (
        agora.getFullYear().toString() +
        pad(agora.getMonth() + 1) +
        pad(agora.getDate()) +
        '_' +
        pad(agora.getHours()) +
        pad(agora.getMinutes()) +
        pad(agora.getSeconds())
    );
}

function exibirStatus(mensagem, ocultar, erro) {
    var elemento = document.getElementById('statusExportacao');

    if (!elemento) {
        return;
    }

    if (ocultar || !mensagem) {
        elemento.style.display = 'none';
        return;
    }

    elemento.textContent = mensagem;
    elemento.style.display = 'block';
    elemento.className =
        'status-exportacao' + (erro ? ' status-exportacao-erro' : '');
}
