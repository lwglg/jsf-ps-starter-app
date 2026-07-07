package br.com.rsdata.util;

import br.com.rsdata.model.Empresa;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.model.TipoEmpresa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Ao iniciar a aplicação, força a criação/atualização do schema (via Hibernate
 * hbm2ddl.auto=update, disparado na primeira chamada ao EntityManagerFactory)
 * e popula as tabelas ramo_atividade e empresa com dados iniciais válidos,
 * caso ainda estejam vazias.
 */
@WebListener
public class DataInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(DataInitializerListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Força a inicialização do EntityManagerFactory, o que cria/atualiza o schema.
        JPAUtil.getEntityManagerFactory();

        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Long totalRamos = em.createQuery("SELECT COUNT(r) FROM RamoAtividade r", Long.class)
                    .getSingleResult();

            Map<String, RamoAtividade> ramos = new HashMap<>();
            if (totalRamos == 0) {
                LOGGER.info("Populando dados iniciais de RamoAtividade...");
                List<String> descricoes = List.of(
                        "Tecnologia da Informação",
                        "Comércio Varejista",
                        "Indústria Alimentícia",
                        "Construção Civil",
                        "Serviços de Consultoria"
                );
                for (String descricao : descricoes) {
                    RamoAtividade ramo = new RamoAtividade(descricao);
                    em.persist(ramo);
                    ramos.put(descricao, ramo);
                }
                em.flush();
            } else {
                List<RamoAtividade> existentes = em.createQuery(
                        "SELECT r FROM RamoAtividade r", RamoAtividade.class).getResultList();
                for (RamoAtividade r : existentes) {
                    ramos.put(r.getDescricao(), r);
                }
            }

            Long totalEmpresas = em.createQuery("SELECT COUNT(e) FROM Empresa e", Long.class)
                    .getSingleResult();

            if (totalEmpresas == 0) {
                LOGGER.info("Populando dados iniciais de Empresa...");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                em.persist(criarEmpresa("Padaria Pão Quente", "Pão Quente Panificadora LTDA",
                        "12.345.678/0001-90", sdf.parse("2015-03-10"),
                        ramos.get("Indústria Alimentícia"), TipoEmpresa.MEI, new BigDecimal("8100.00")));

                em.persist(criarEmpresa("TechNova Soluções", "TechNova Soluções em TI EIRELI",
                        "23.456.789/0001-01", sdf.parse("2018-07-22"),
                        ramos.get("Tecnologia da Informação"), TipoEmpresa.EIRELI, new BigDecimal("3600.50")));

                em.persist(criarEmpresa("Construtora Horizonte", "Horizonte Construções e Incorporações LTDA",
                        "34.567.890/0001-12", sdf.parse("2010-01-15"),
                        ramos.get("Construção Civil"), TipoEmpresa.LTDA, new BigDecimal("4500.00")));

                em.persist(criarEmpresa("Mega Varejo", "Mega Varejo Comércio de Produtos S.A.",
                        "45.678.901/0001-23", sdf.parse("2005-11-30"),
                        ramos.get("Comércio Varejista"), TipoEmpresa.SA, new BigDecimal("12500.00")));

                em.persist(criarEmpresa("Prisma Consultoria", "Prisma Consultoria Empresarial LTDA",
                        "56.789.012/0001-34", sdf.parse("2020-05-04"),
                        ramos.get("Serviços de Consultoria"), TipoEmpresa.LTDA, new BigDecimal("21000.75")));
            }

            tx.commit();
            LOGGER.info("Inicialização de dados concluída com sucesso.");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Erro ao inicializar dados: " + e.getMessage());
            throw new RuntimeException("Falha ao inicializar dados da aplicação", e);
        } finally {
            em.close();
        }
    }

    private Empresa criarEmpresa(String nomeFantasia, String razaoSocial, String cnpj,
                                  java.util.Date dataFundacao, RamoAtividade ramoAtividade,
                                  TipoEmpresa tipoEmpresa, BigDecimal faturamento) {
        Empresa empresa = new Empresa();
        empresa.setNomeFantasia(nomeFantasia);
        empresa.setRazaoSocial(razaoSocial);
        empresa.setCnpj(cnpj);
        empresa.setDataFundacao(dataFundacao);
        empresa.setRamoAtividade(ramoAtividade);
        empresa.setTipoEmpresa(tipoEmpresa);
        empresa.setFaturamento(faturamento);
        return empresa;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.close();
    }
}
