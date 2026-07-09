package br.com.rsdata.controller;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

import org.primefaces.PrimeFaces;

import br.com.rsdata.util.JPAUtil;

@Named("sobreSistemaBean")
@ViewScoped
public class SobreSistemaBean implements Serializable {
    private String nomeSistema;
    private String urlPaginaEmpresa;
    private String descricaoSistema;
    private String versaoSistema;
    private String nomeResponsavelTecnico;
    private String emailResponsavelTecnico;

    public void carregaConteudo() {
        this.nomeSistema = JPAUtil.env("SYSTEM_NAME","");
        this.versaoSistema = JPAUtil.env("SYSTEM_VERSION", "");
        this.urlPaginaEmpresa = JPAUtil.env("COMPANY_PAGE_URL", "");
        this.descricaoSistema = JPAUtil.env("SYSTEM_DESCRIPTION", "");
        this.nomeResponsavelTecnico = JPAUtil.env("SYSADMIN_NAME", "");
        this.emailResponsavelTecnico = JPAUtil.env("SYSADMIN_EMAIL", "");
    }

    public void setTituloPagina() {
        carregaConteudo();
        
        String cmdJavaScript = "document.title = '" + this.nomeSistema + "|" + this.descricaoSistema + "';";
        PrimeFaces.current().executeScript(cmdJavaScript);
    }

    public String getNomeSistema() { return nomeSistema; }
    public String getVersaoSistema() { return versaoSistema; }
    public String getUrlPaginaEmpresa() { return urlPaginaEmpresa; }
    public String getDescricaoSistema() { return descricaoSistema; }
    public String getNomeResponsavelTecnico() { return nomeResponsavelTecnico; }
    public String getEmailResponsavelTecnico() { return emailResponsavelTecnico; }
}
