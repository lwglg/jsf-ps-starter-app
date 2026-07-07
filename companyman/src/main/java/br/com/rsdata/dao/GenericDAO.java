package br.com.rsdata.dao;

import java.util.List;
import java.util.UUID;

/**
 * Contrato genérico de acesso a dados (padrão DAO) usado pelas entidades
 * do domínio (Empresa e RamoAtividade).
 */
public interface GenericDAO<T> {

    T salvar(T entidade);

    T atualizar(T entidade);

    void remover(UUID id);

    T buscarPorId(UUID id);

    List<T> listarTodos();
}
