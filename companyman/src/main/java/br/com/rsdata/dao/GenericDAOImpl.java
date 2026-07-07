package br.com.rsdata.dao;

import br.com.rsdata.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.UUID;

/**
 * Implementação genérica do DAO, cuidando da abertura/fechamento do
 * EntityManager e do controle transacional (RESOURCE_LOCAL).
 */
public abstract class GenericDAOImpl<T> implements GenericDAO<T> {

    private final Class<T> classe;

    protected GenericDAOImpl(Class<T> classe) {
        this.classe = classe;
    }

    @Override
    public T salvar(T entidade) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entidade);
            tx.commit();
            return entidade;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public T atualizar(T entidade) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entidade);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void remover(UUID id) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entidade = em.find(classe, id);
            if (entidade != null) {
                em.remove(entidade);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public T buscarPorId(UUID id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.find(classe, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<T> listarTodos() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM " + classe.getSimpleName() + " e", classe)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
