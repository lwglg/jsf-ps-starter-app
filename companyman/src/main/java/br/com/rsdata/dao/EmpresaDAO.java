package br.com.rsdata.dao;

import br.com.rsdata.model.Empresa;
import br.com.rsdata.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class EmpresaDAO extends GenericDAOImpl<Empresa> {

    public EmpresaDAO() {
        super(Empresa.class);
    }

    /**
     * Busca uma empresa pelo CNPJ, usado para impedir cadastro de itens repetidos.
     */
    public Empresa buscarPorCnpj(String cnpj) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                        "SELECT e FROM Empresa e WHERE e.cnpj = :cnpj", Empresa.class)
                    .setParameter("cnpj", cnpj)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Lista todas as empresas já com o ramo de atividade carregado (evita
     * LazyInitializationException ao renderizar a tela após o fechamento do EntityManager).
     */
    @Override
    public List<Empresa> listarTodos() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                        "SELECT e FROM Empresa e JOIN FETCH e.ramoAtividade ORDER BY e.nomeFantasia",
                        Empresa.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
