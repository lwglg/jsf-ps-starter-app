package br.com.rsdata.controller;

import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.exception.ValidationException;
import br.com.rsdata.model.Empresa;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.model.TipoEmpresa;
import br.com.rsdata.service.EmpresaService;
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

/**
 * Managed bean (Controller) responsável pela tela de gerenciamento de Empresas.
 *
 * A exportação de dados (CSV/XLS/ODT/PDF) foi convergida para um único
 * modal compartilhado — ver {@link ExportBean} e
 * {@code dialogs/exportar-dados/index.xhtml}. Este bean expõe a seleção de
 * linhas ({@link #selecionados}) e a posição de paginação atual
 * ({@link #primeiroRegistro}) do {@code p:dataTable} de
 * {@code empresa/index.xhtml}, lidas por
 * {@code br.com.rsdata.servlet.ExportDownloadServlet} (via injeção CDI) para
 * resolver o escopo de exportação escolhido pelo usuário (todos os
 * registros, apenas os selecionados, ou apenas a página atual).
 */
@Named
@SessionScoped
public class EmpresaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Deve ser mantido igual ao atributo {@code rows} do p:dataTable em empresa/index.xhtml. */
    public static final int TAMANHO_PAGINA = 10;

    private final EmpresaService empresaService = new EmpresaService();
    private final RamoAtividadeService ramoAtividadeService = new RamoAtividadeService();

    private List<Empresa> lista;
    private Empresa selecionado = new Empresa();
    private Empresa novoRegistro = new Empresa();
    private List<Empresa> selecionados = new ArrayList<>();
    private int primeiroRegistro = 0;

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

    public List<Empresa> getSelecionados() {
        return selecionados;
    }

    public void setSelecionados(List<Empresa> selecionados) {
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
    public List<Empresa> getRegistrosDaPaginaAtual() {
        List<Empresa> todos = getLista();
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
