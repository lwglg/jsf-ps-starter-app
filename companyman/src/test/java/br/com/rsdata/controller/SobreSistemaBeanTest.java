package br.com.rsdata.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SobreSistemaBean - informações do modal 'Sobre o sistema'")
class SobreSistemaBeanTest {

    private static final String[] CHAVES = {
        "APP_NAME",
        "APP_DESCRICAO",
        "APP_VERSION",
        "APP_SYSADMIN_NAME",
        "APP_SYSADMIN_EMAIL",
        "APP_COMPANY_NAME",
        "APP_COMPANY_URL",
        "APP_LOGO_PATH"
    };

    @AfterEach
    void limparSystemProperties() {
        for (String chave : CHAVES) {
            System.clearProperty(chave);
        }
    }

    @Test
    @DisplayName("Deve usar valores padrão quando nenhuma variável de ambiente/system property está definida")
    void deveUsarValoresPadraoQuandoNaoConfigurado() {
        SobreSistemaBean bean = new SobreSistemaBean();
        bean.init();

        assertEquals("CompanyMAN", bean.getNomeSistema());
        assertEquals("1.0.0", bean.getVersao());
        assertEquals("rsdata_logo.svg", bean.getLogoPath());
        assertEquals("https://www.rsdata.com.br", bean.getEmpresaUrl());
    }

    @Test
    @DisplayName("Deve priorizar system properties sobre os valores padrão")
    void devePriorizarSystemProperties() {
        System.setProperty("APP_NAME", "Sistema de Teste");
        System.setProperty("APP_VERSION", "9.9.9");
        System.setProperty("APP_SYSADMIN_NAME", "Fulano de Tal");
        System.setProperty("APP_SYSADMIN_EMAIL", "sysadmin@teste.com");

        SobreSistemaBean bean = new SobreSistemaBean();
        bean.init();

        assertEquals("Sistema de Teste", bean.getNomeSistema());
        assertEquals("9.9.9", bean.getVersao());
        assertEquals("Fulano de Tal", bean.getResponsavelNome());
        assertEquals("sysadmin@teste.com", bean.getResponsavelEmail());
    }
}
