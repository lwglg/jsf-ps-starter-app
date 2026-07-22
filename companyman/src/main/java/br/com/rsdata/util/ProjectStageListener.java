package br.com.rsdata.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class ProjectStageListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(ProjectStageListener.class.getName());

    @Override
    /**
     * Intercept ServletContextEvent via a listener.
     * This overrides the web.xml at runtime.
     */
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String projectStage = EnvUtil.env("APP_ENVIRONMENT", "Development");

            sce.getServletContext().setInitParameter("jakarta.faces.PROJECT_STAGE", projectStage);

            logger.info("Aplicação rodando em ambiente de " + projectStage.toUpperCase());
        } catch (Exception exc) {
            logger.severe("Ambiente não pode ser definido. Confira o valor de $APP_ENVIRONMENT");

            throw new RuntimeException("Falha ao definir ambiente de runtime para a aplicação", exc);
        }
    }
}
