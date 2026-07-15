package br.com.rsdata.controller;

import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.exception.ValidationException;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.service.RamoAtividadeService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean (Controller) responsável pela tela de gerenciamento de
 * Ramos de Atividade.
 *
 * A exportação de dados (CSV/XLS/ODT/PDF) foi convergida para um único
 * modal compartilhado — ver {@link ExportBean} e
 * {@code dialogs/exportar-dados/index.xhtml}. Este bean expõe a seleção de
 * linhas ({@link #selecionados}) e a posição de paginação atual
 * ({@link #primeiroRegistro}) do {@code p:dataTable} de
 * {@code ramoAtividade/index.xhtml}, lidas por
 * {@code br.com.rsdata.servlet.ExportDownloadServlet} (via injeção CDI) para
 * resolver o escopo de exportação escolhido pelo usuário (todos os
 * registros, apenas os selecionados, ou apenas a página atual).
 */
@Named
@SessionScoped
public class RamoAtividadeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Deve ser mantido igual ao atributo {@code rows} do p:dataTable em ramoAtividade/index.xhtml. */
    public static final int TAMANHO_PAGINA = 10;

    private final RamoAtividadeService service = new RamoAtividadeService();

    private static final Logger logger = LoggerFactory.getLogger(RamoAtividadeBean.class);

    private List<RamoAtividade> lista;
    private RamoAtividade selecionado;
    private RamoAtividade novoRegistro = new RamoAtividade();
    private List<RamoAtividade> selecionados = new ArrayList<>();
    private int primeiroRegistro = 0;

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

    public List<RamoAtividade> getSelecionados() {
        logger.info("{} ramos de atividade sendo lidos: {}", selecionados.size(), selecionados.toString());

        return selecionados;
    }

    public void setSelecionados(List<RamoAtividade> selecionados) {
        logger.info("{} ramos de atividade sendo setados: {}", selecionados.size(), selecionados.toString());

        this.selecionados = selecionados;
    }

    /**
     * Exposto como getter (e não acesso direto ao campo estático
     * {@link #TAMANHO_PAGINA}) porque expressões EL só resolvem
     * propriedades via métodos get/is, nunca campos públicos diretamente.
     */
    public int getTamanhoPagina() {
        return TAMANHO_PAGINA;
    }

    public int getPrimeiroRegistro() {
        return primeiroRegistro;
    }

    public void setPrimeiroRegistro(int primeiroRegistro) {
        this.primeiroRegistro = primeiroRegistro;
    }

    /**
     * Retorna apenas os registros da página atualmente exibida na tabela
     * (com base em {@link #primeiroRegistro} e {@link #TAMANHO_PAGINA}),
     * usado pelo escopo de exportação "Somente a página atual".
     */
    public List<RamoAtividade> getRegistrosDaPaginaAtual() {
        List<RamoAtividade> todos = getLista();
        if (todos.isEmpty()) {
            return Collections.emptyList();
        }

        int inicio = Math.min(primeiroRegistro, todos.size());
        int fim = Math.min(inicio + TAMANHO_PAGINA, todos.size());

        return todos.subList(inicio, fim);
    }

    private void addMensagem(FacesMessage.Severity severidade, String titulo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, titulo, detalhe));
    }
}
