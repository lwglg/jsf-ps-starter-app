package br.com.rsdata.controller;

import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.service.RamoAtividadeService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Managed bean (Controller) responsável pela tela de gerenciamento de
 * Ramos de Atividade.
 */
@Named("ramoAtividadeBean")
@SessionScoped
public class RamoAtividadeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RamoAtividadeService service = new RamoAtividadeService();

    private List<RamoAtividade> lista;
    private RamoAtividade selecionado;
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
            // TODO: remove this check after proper bean validation layer is integrated
            if (novoRegistro.getDescricao() == null) return;

            service.salvar(novoRegistro);
            lista = null;
            novoRegistro = new RamoAtividade();
            addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Ramo de atividade cadastrado com sucesso.");
        } catch (DuplicateEntityException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Registro duplicado", e.getMessage());
        }
    }

    public void atualizar() {
        try {
            // Failsafe checking when rendering page for the first time
            if (selecionado == null) return;
            
            service.atualizar(selecionado);
            lista = null;
            addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Ramo de atividade atualizado com sucesso.");
        } catch (DuplicateEntityException e) {
            addMensagem(FacesMessage.SEVERITY_WARN, "Registro duplicado", e.getMessage());
        }
    }

    public void remover(String id) {
        service.remover(id);
        lista = null;
        addMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Ramo de atividade removido com sucesso.");
    }

    private void addMensagem(FacesMessage.Severity severidade, String titulo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, titulo, detalhe));
    }
}
