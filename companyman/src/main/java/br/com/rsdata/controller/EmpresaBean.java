package br.com.rsdata.controller;

import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.exception.ExportException;
import br.com.rsdata.exception.ValidationException;
import br.com.rsdata.export.ExportFormat;
import br.com.rsdata.export.ExportResponseWriter;
import br.com.rsdata.model.Empresa;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.model.TipoEmpresa;
import br.com.rsdata.service.EmpresaExportService;
import br.com.rsdata.service.EmpresaService;
import br.com.rsdata.service.RamoAtividadeService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Managed bean (Controller) responsável pela tela de gerenciamento de Empresas.
 */
@Named
@SessionScoped
public class EmpresaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EmpresaService empresaService = new EmpresaService();
    private final RamoAtividadeService ramoAtividadeService = new RamoAtividadeService();
    private final EmpresaExportService empresaExportService = new EmpresaExportService();

    private List<Empresa> lista;
    private Empresa selecionado = new Empresa();
    private Empresa novoRegistro = new Empresa();

    public List<Empresa> getLista() {
        if (lista == null) {
            lista = empresaService.listarTodos();
        }
        return lista;
    }

    public List<RamoAtividade> getRamosAtividade() {
        return ramoAtividadeService.listarTodos();
    }

    public TipoEmpresa[] getTiposEmpresa() {
        return TipoEmpresa.values();
    }

    public Empresa getSelecionado() {
        return selecionado;
    }

    public void setSelecionado(Empresa selecionado) {
        this.selecionado = selecionado;
    }

    public Empresa getNovoRegistro() {
        return novoRegistro;
    }

    public void setNovoRegistro(Empresa novoRegistro) {
        this.novoRegistro = novoRegistro;
    }

    public void prepararNovo() {
        this.novoRegistro = new Empresa();
    }

    public void prepararEdicao(Empresa empresa) {
        this.selecionado = empresa;
    }

    public void salvar() {
        try {
            empresaService.salvar(novoRegistro);
            lista = null;
            novoRegistro = new Empresa();
            addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Empresa cadastrada com sucesso.");
        } catch (DuplicateEntityException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Registro duplicado", e.getMessage());
        } catch (ValidationException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Dados inválidos", String.join(" | ", e.getViolacoes()));
        }
    }

    public void atualizar() {
        try {
            empresaService.atualizar(selecionado);
            lista = null;
            addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Empresa atualizada com sucesso.");
        } catch (DuplicateEntityException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Registro duplicado", e.getMessage());
        } catch (ValidationException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Dados inválidos", String.join(" | ", e.getViolacoes()));
        }
    }

    public void remover(UUID id) {
        empresaService.remover(id);
        lista = null;
        addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Empresa removida com sucesso.");
    }

    /**
     * Exporta a listagem completa de empresas (não apenas a página atual da
     * tabela) no formato solicitado, iniciando o download do arquivo.
     * Deve ser chamado a partir de um componente com {@code ajax="false"}.
     */
    public void exportar(ExportFormat formato) {
        try {
            byte[] conteudo = empresaExportService.exportar(getLista(), formato);
            String nomeArquivo = EmpresaExportService.NORE_RELATORIO_FALLBACK;
            ExportResponseWriter.escreverDownload(conteudo, nomeArquivo, EmpresaExportService.NORE_RELATORIO_FALLBACK, formato);
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
