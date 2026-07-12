package br.com.rsdata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultSubMenu;

import br.com.rsdata.model.MenuPrincipal;
import br.com.rsdata.model.SubMenu;


public class MenuPrincipalService {

    private MenuPrincipal.SubMenuRecord geraDadosSubmenuEmpresa() {
        MenuPrincipal.SubMenuRecord dados = new MenuPrincipal.SubMenuRecord(
            SubMenu.CADASTRO.getTitulo(),
            SubMenu.CADASTRO.getIcone(),
            new ArrayList<>(List.of(
                new MenuPrincipal.SubMenuItemRecord(
                    "Empresas",
                    "pi pi-building",
                    Optional.of("/empresa/index.xhtml"),
                    Optional.empty()
                ),
                new MenuPrincipal.SubMenuItemRecord(
                    "Ramos de atividade",
                    "pi pi-sitemap",
                    Optional.of("/ramoAtividade/index.xhtml"),
                    Optional.empty()
                )
            ))
        );

        return dados;
    }

    private MenuPrincipal.SubMenuRecord geraDadosSubmenuOperacoes() {
        MenuPrincipal.SubMenuRecord dados = new MenuPrincipal.SubMenuRecord(
            SubMenu.OPERACOES.getTitulo(),
            SubMenu.OPERACOES.getIcone(),
            new ArrayList<>(List.of(
                new MenuPrincipal.SubMenuItemRecord(
                    "Exportar dados",
                    "pi pi-download",
                    Optional.empty(),
                    Optional.of("PF('dlgExportarDados').show(); return false;")
                )
            ))
        );

        return dados;
    }

    private MenuPrincipal.SubMenuRecord geraDadosSubmenuSistema() {
        MenuPrincipal.SubMenuRecord dados = new MenuPrincipal.SubMenuRecord(
            SubMenu.SISTEMA.getTitulo(),
            SubMenu.SISTEMA.getIcone(),
            new ArrayList<>(List.of(
                new MenuPrincipal.SubMenuItemRecord(
                    "Sobre o sistema",
                    "pi pi-info-circle",
                    Optional.empty(),
                    Optional.of("PF('dlgSobreSistema').show(); return false;")
                )
            ))
        );

        return dados;
    }

    public List<MenuPrincipal.SubMenuRecord> geraDadosSubmenus() {
        List<MenuPrincipal.SubMenuRecord> dados = new ArrayList<>(List.of(
            geraDadosSubmenuEmpresa(),
            geraDadosSubmenuOperacoes(),
            geraDadosSubmenuSistema()
        ));

        return dados;
    }

    public DefaultSubMenu criaSubmenu(MenuPrincipal.SubMenuRecord submenuData) {
        DefaultSubMenu submenu = DefaultSubMenu.builder()
            .label(submenuData.titulo())
            .icon(submenuData.icone())
            .build();

        for (MenuPrincipal.SubMenuItemRecord item : submenuData.items()) {
            submenu.getElements().add(DefaultMenuItem.builder()
                .value(item.titulo())
                .icon(item.icone())
                .outcome(item.outcome().orElse(null))
                .onclick(item.onClickHandler().orElse(null))
                .build());
        }

        return submenu;
    }
}
