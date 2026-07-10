package br.com.rsdata.controller;

import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.exception.ExportException;
import br.com.rsdata.exception.ValidationException;
import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.export.ExportResponseWriter;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.service.RamoAtividadeExportService;
import br.com.rsdata.service.RamoAtividadeService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Managed bean (Controller) responsável pela tela de gerenciamento de
 * Ramos de Atividade.
 */
@Named
@SessionScoped
public class RamoAtividadeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RamoAtividadeService service = new RamoAtividadeService();
    private final RamoAtividadeExportService exportService = new RamoAtividadeExportService();

    private List<RamoAtividade> lista;
    private RamoAtividade selecionado = new RamoAtividade();
    private RamoAtividade novoRegistro = new RamoAtividade();

    public List<RamoAtividade> getLista() {
        if (lista == null) {
            lista = service.listarTodos();
        }
        return lista;
    }

    public RamoAtividade getSelecionado() {
        return selecionado;
    }

    public void setSelecionado(RamoAtividade selecionado) {
        this.selecionado = selecionado;
    }

    public RamoAtividade getNovoRegistro() {
        return novoRegistro;
    }

    public void setNovoRegistro(RamoAtividade novoRegistro) {
        this.novoRegistro = novoRegistro;
    }

    public void prepararNovo() {
        this.novoRegistro = new RamoAtividade();
    }

    public void prepararEdicao(RamoAtividade ramoAtividade) {
        this.selecionado = new RamoAtividade();
        this.selecionado.setId(ramoAtividade.getId());
        this.selecionado.setDescricao(ramoAtividade.getDescricao());
    }

    public void salvar() {
        try {
            service.salvar(novoRegistro);
            lista = null;
            novoRegistro = new RamoAtividade();
            addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Ramo de atividade cadastrado com sucesso.");
        } catch (DuplicateEntityException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Registro duplicado", e.getMessage());
        } catch (ValidationException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Dados inválidos", String.join(" | ", e.getViolacoes()));
        }
    }

    public void atualizar() {
        try {
            service.atualizar(selecionado);
            lista = null;
            addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Ramo de atividade atualizado com sucesso.");
        } catch (DuplicateEntityException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Registro duplicado", e.getMessage());
        } catch (ValidationException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Dados inválidos", String.join(" | ", e.getViolacoes()));
        }
    }

    public void remover(UUID id) {
        service.remover(id);
        lista = null;
        addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Ramo de atividade removido com sucesso.");
    }

    /**
     * Exporta a listagem completa de ramos de atividade (não apenas a
     * página atual da tabela) no formato solicitado, iniciando o download
     * do arquivo. Deve ser chamado a partir de um componente com
     * {@code ajax="false"}.
     */
    public void exportar(ExportFormat formato) {
        try {
            byte[] conteudo = exportService.exportar(getLista(), formato);
            String nomeArquivo = "ramos-de-atividade." + formato.getExtensao();
            ExportResponseWriter.escreverDownload(conteudo, nomeArquivo, formato.getContentType());
        } catch (ExportException e) {
            addMensagem(FacesMessage.SEVERITY_ERROR, "Erro ao exportar", e.getMessage());
        }
    }

    public ExportFormat[] getFormatosExportacao() {
        return ExportFormat.values();
    }

    private void addMensagem(FacesMessage.Severity severidade, String titulo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, titulo, detalhe));
    }
}
