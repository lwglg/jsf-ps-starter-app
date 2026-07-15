package br.com.rsdata.service;

import br.com.rsdata.dao.RamoAtividadeDAO;
import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.exception.EntityNotFoundException;
import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.util.EntityValidator;
import java.util.List;
import java.util.UUID;

/**
 * Camada de serviço (regras de negócio) para RamoAtividade.
 */
public class RamoAtividadeService {

    private final RamoAtividadeDAO dao = new RamoAtividadeDAO();

    public RamoAtividade salvar(RamoAtividade ramoAtividade) {
        EntityValidator.validar(ramoAtividade);

        RamoAtividade existente = dao.buscarPorDescricao(ramoAtividade.getDescricao());

        if (existente != null) {
            throw new DuplicateEntityException(
                    "Já existe um ramo de atividade cadastrado com a descrição '"
                            + ramoAtividade.getDescricao() + "'.");
        }
        return dao.salvar(ramoAtividade);
    }

    public RamoAtividade atualizar(RamoAtividade ramoAtividade) {
        EntityValidator.validar(ramoAtividade);

        RamoAtividade existente = dao.buscarPorDescricao(ramoAtividade.getDescricao());

        if (existente != null && !existente.getId().equals(ramoAtividade.getId())) {
            throw new DuplicateEntityException(
                    "Já existe um ramo de atividade cadastrado com a descrição '"
                            + ramoAtividade.getDescricao() + "'.");
        }
        return dao.atualizar(ramoAtividade);
    }

    public void remover(UUID id) {
        if (dao.buscarPorId(id) == null) {
            throw new EntityNotFoundException("Ramo de atividade não encontrado: " + id);
        }

        dao.remover(id);
    }

    public RamoAtividade buscarPorId(UUID id) {
        return dao.buscarPorId(id);
    }

    public List<RamoAtividade> listarTodos() {
        return dao.listarTodos();
    }
}
