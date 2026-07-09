package br.com.rsdata.dao;

import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class RamoAtividadeDAO extends GenericDAOImpl<RamoAtividade> {

    public RamoAtividadeDAO() {
        super(RamoAtividade.class);
    }

    /**
     * Busca um ramo de atividade pela descrição (case-insensitive),
     * usado para impedir cadastro de itens repetidos.
     */
    public RamoAtividade buscarPorDescricao(String descricao) {
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery(
                "SELECT r FROM RamoAtividade r WHERE lower(r.descricao) = lower(:descricao)",
                RamoAtividade.class)
            .setParameter("descricao", descricao)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
