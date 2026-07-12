package br.com.rsdata.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.rsdata.service.MenuPrincipalService;
import br.com.rsdata.model.MenuPrincipal;

import java.io.Serializable;
import java.util.List;


@Named("mainMenuBean")
@ApplicationScoped
public class MainMenuBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MainMenuBean.class);

    private MenuPrincipalService menuPrincipalService = new MenuPrincipalService();

    private MenuModel model;

    @PostConstruct
    public void init() {
        try {
            logger.info("Construção do MenuModel iniciada...");

            model = new DefaultMenuModel();

            List<MenuPrincipal.SubMenuRecord> dadosSubmenus = menuPrincipalService.geraDadosSubmenus();

            for (MenuPrincipal.SubMenuRecord dadosSubmenu : dadosSubmenus) {
                DefaultSubMenu submenu = menuPrincipalService.criaSubmenu(dadosSubmenu);

                if (!submenu.getElements().isEmpty()) {
                    logger.info("submenu" + submenu.toString() + " is NOT EMPTY!");

                    model.getElements().add(submenu);
                } else {
                    logger.info("submenu" + submenu.toString() + " is EMPTY!");
                }
            }
        } catch(Exception exc) {
            logger.error("Erro ao construir MenuModel", exc);
        }
    }

    public MenuModel getModel() {
        logger.debug("getMenuPrincipal() chamado - elementos: {}",
                    (model != null ? model.getElements().size() : 0));

        return model;
    }
}
