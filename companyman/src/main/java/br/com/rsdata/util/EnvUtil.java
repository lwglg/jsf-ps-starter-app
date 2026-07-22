package br.com.rsdata.util;

public class EnvUtil {
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
}
