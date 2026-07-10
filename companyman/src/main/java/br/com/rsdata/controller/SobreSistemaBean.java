package br.com.rsdata.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Managed bean responsável por expor as informações exibidas no modal
 * "Sobre o Sistema" (nome, descrição, versão, responsável técnico e logo
 * da empresa). Todos os valores são lidos de variáveis de ambiente,
 * permitindo configurá-los via Docker Compose sem recompilar a aplicação.
 *
 * É {@code @ApplicationScoped} porque o conteúdo é o mesmo para todos os
 * usuários e não muda durante a execução da aplicação — não há necessidade
 * de recarregar as variáveis de ambiente a cada sessão/requisição.
 */
@Named
@ApplicationScoped
public class SobreSistemaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nomeSistema;
    private String descricao;
    private String versao;
    private String responsavelNome;
    private String responsavelEmail;
    private String empresaNome;
    private String empresaUrl;
    private String logoPath;

    @PostConstruct
    public void init() {
        nomeSistema = env("APP_NAME", "CompanyMAN");
        descricao = env("APP_DESCRIPTION",
                "Sistema de gerenciamento de empresas e seus ramos de atividade.");
        versao = env("APP_VERSION", "1.0.0");
        responsavelNome = env("APP_SYSADMIN_NAME", "Guilherme Gonçalves");
        responsavelEmail = env("APP_SYSADMIN_EMAIL", "guilherme.goncalves@rsdata.inf.br");
        empresaNome = env("APP_COMPANY_NAME", "Empresa");
        empresaUrl = env("APP_COMPANY_URL", "https://www.rsdata.com.br");
        logoPath = env("APP_LOGO_PATH", "rsdata_logo.png");
    }

    /**
     * Resolve a configuração dando prioridade a system properties (útil em
     * testes/depuração local) e, na ausência destas, a variáveis de
     * ambiente (usadas em produção via Docker Compose).
     */
    private String env(String key, String defaultValue) {
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public String getNomeSistema() {
        return nomeSistema;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getVersao() {
        return versao;
    }

    public String getResponsavelNome() {
        return responsavelNome;
    }

    public String getResponsavelEmail() {
        return responsavelEmail;
    }

    public String getEmpresaNome() {
        return empresaNome;
    }

    public String getEmpresaUrl() {
        return empresaUrl;
    }

    public String getLogoPath() {
        return logoPath;
    }
}
