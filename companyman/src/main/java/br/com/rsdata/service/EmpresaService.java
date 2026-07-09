package br.com.rsdata.service;

import br.com.rsdata.dao.EmpresaDAO;
import br.com.rsdata.exception.DuplicateEntityException;
import br.com.rsdata.exception.EntityNotFoundException;
import br.com.rsdata.model.Empresa;
import br.com.rsdata.util.EntityValidator;

import java.util.List;
import java.util.UUID;

/**
 * Camada de serviço (regras de negócio) para Empresa.
 */
public class EmpresaService {

    private final EmpresaDAO dao = new EmpresaDAO();

    public Empresa salvar(Empresa empresa) {
        EntityValidator.validar(empresa);

        Empresa existente = dao.buscarPorCnpj(empresa.getCnpj());
        
        if (existente != null) {
            throw new DuplicateEntityException(
                    "Já existe uma empresa cadastrada com o CNPJ '" + empresa.getCnpj() + "'.");
        }
        
        return dao.salvar(empresa);
    }

    public Empresa atualizar(Empresa empresa) {
        EntityValidator.validar(empresa);

        Empresa existente = dao.buscarPorCnpj(empresa.getCnpj());
        
        if (existente != null && !existente.getId().equals(empresa.getId())) {
            throw new DuplicateEntityException(
                    "Já existe uma empresa cadastrada com o CNPJ '" + empresa.getCnpj() + "'.");
        }
        
        return dao.atualizar(empresa);
    }

    public void remover(UUID id) {
        if (dao.buscarPorId(id) == null) {
            throw new EntityNotFoundException("Empresa não encontrada: " + id);
        }
        
        dao.remover(id);
    }

    public Empresa buscarPorId(UUID id) {
        return dao.buscarPorId(id);
    }

    public List<Empresa> listarTodos() {
        return dao.listarTodos();
    }
}
