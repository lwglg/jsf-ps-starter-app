package br.com.rsdata.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.model.menu.Submenu;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MainMenuBean - construção programática do menu da sidebar")
class MenuBeanTest {

    private MainMenuBean menuBean;

    @BeforeEach
    void setUp() {
        menuBean = new MainMenuBean();
        menuBean.init();
    }

    @Test
    @DisplayName("Deve conter exatamente três submenus: Cadastros, Operações e Sistema")
    void deveConterTresSubmenusNaOrdemCorreta() {
        MenuModel model = menuBean.getModel();
        List<MenuElement> elementos = model.getElements();

        assertEquals(3, elementos.size());
        assertEquals("Cadastros", ((Submenu) elementos.get(0)).getLabel());
        assertEquals("Operações", ((Submenu) elementos.get(1)).getLabel());
        assertEquals("Sistema", ((Submenu) elementos.get(2)).getLabel());
    }

    @Test
    @DisplayName("Submenu Cadastros deve conter Empresas e Ramos de Atividade, navegando via outcome")
    void submenuCadastrosDeveConterOsDoisItensDeNavegacao() {
        Submenu cadastros = (Submenu) menuBean.getModel().getElements().get(0);
        List<MenuElement> itens = cadastros.getElements();

        assertEquals(2, itens.size());

        MenuItem empresas = (MenuItem) itens.get(0);
        assertEquals("Empresas", empresas.getValue());
        assertEquals("/empresa/index.xhtml", empresas.getOutcome());

        MenuItem ramosAtividade = (MenuItem) itens.get(1);
        assertEquals("Ramos de atividade", ramosAtividade.getValue());
        assertEquals("/ramoAtividade/index.xhtml", ramosAtividade.getOutcome());
    }

    @Test
    @DisplayName("Submenu Operações deve conter apenas 'Exportar Dados', abrindo o modal via onclick")
    void submenuOperacoesDeveConterExportarDados() {
        Submenu operacoes = (Submenu) menuBean.getModel().getElements().get(1);
        List<MenuElement> itens = operacoes.getElements();

        assertEquals(1, itens.size());
        MenuItem exportarDados = (MenuItem) itens.get(0);
        assertEquals("Exportar dados", exportarDados.getValue());
        assertTrue(exportarDados.getOnclick().contains("dlgExportarDados"));
    }

    @Test
    @DisplayName("Submenu Sistema deve conter apenas 'Sobre o Sistema', abrindo o modal via onclick")
    void submenuSistemaDeveConterSobreOSistema() {
        Submenu sistema = (Submenu) menuBean.getModel().getElements().get(2);
        List<MenuElement> itens = sistema.getElements();

        assertEquals(1, itens.size());
        MenuItem sobreSistema = (MenuItem) itens.get(0);
        assertEquals("Sobre o sistema", sobreSistema.getValue());
        assertTrue(sobreSistema.getOnclick().contains("dlgSobreSistema"));
    }
}
