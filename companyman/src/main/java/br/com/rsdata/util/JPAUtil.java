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

        String host = env("DB_HOSTNAME", "");
        String port = env("DB_PORT", "");
        String name = env("DB_NAME", "");
        String user = env("DB_USERNAME", "");
        String password = env("DB_PASSWORD", "");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + name;

        overrides.put("jakarta.persistence.jdbc.url", url);
        overrides.put("jakarta.persistence.jdbc.user", user);
        overrides.put("jakarta.persistence.jdbc.password", password);

        return overrides;
    }

    /**
     * Resolve a configuração dando prioridade a system properties (útil em testes,
     * ex.: System.setProperty("DB_HOST", ...) com Testcontainers) e, na ausência
     * destas, a variáveis de ambiente (usadas em produção via Docker Compose).
     */
    public static String env(String key, String defaultValue) {
        String fromProperty = System.getProperty(key);
        
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        
        String value = System.getenv(key);
        
        return (value == null || value.isBlank()) ? defaultValue : value;
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
