package br.com.rsdata.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário responsável por criar e disponibilizar o EntityManagerFactory.
 * As propriedades de conexão do persistence.xml são sobrescritas em tempo de
 * execução com base em variáveis de ambiente, permitindo configurar o banco
 * de dados via Docker Compose sem alterar o artefato empacotado.
 */
public final class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "CompanyManPU";
    private static volatile EntityManagerFactory entityManagerFactory;

    private JPAUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            synchronized (JPAUtil.class) {
                if (entityManagerFactory == null) {
                    entityManagerFactory = Persistence.createEntityManagerFactory(
                            PERSISTENCE_UNIT_NAME, buildOverrides());
                }
            }
        }
        return entityManagerFactory;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    private static Map<String, Object> buildOverrides() {
        Map<String, Object> overrides = new HashMap<>();

        String host = EnvUtil.env("DB_HOSTNAME", "");
        String port = EnvUtil.env("DB_PORT", "");
        String name = EnvUtil.env("DB_NAME", "");
        String user = EnvUtil.env("DB_USERNAME", "");
        String password = EnvUtil.env("DB_PASSWORD", "");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + name;

        // Propriedades padrão JPA (usadas pelo provider DriverManager padrão do Hibernate).
        overrides.put("jakarta.persistence.jdbc.url", url);
        overrides.put("jakarta.persistence.jdbc.user", user);
        overrides.put("jakarta.persistence.jdbc.password", password);

        // Propriedades nativas do Hibernate (exigidas pelo HikariCPConnectionProvider,
        // que NÃO lê as propriedades padrão JPA acima). Sem isto, o Hikari tenta
        // conectar sem usuário/senha, causando:
        // "FATAL: no PostgreSQL user name specified in startup packet".
        overrides.put("hibernate.connection.url", url);
        overrides.put("hibernate.connection.username", user);
        overrides.put("hibernate.connection.password", password);

        return overrides;
    }

    /**
     * Permite reinicializar o EntityManagerFactory (usado em testes de integração
     * após configurar as system properties de conexão com o banco de testes).
     */
    public static synchronized void reset() {
        close();
        entityManagerFactory = null;
    }
}
