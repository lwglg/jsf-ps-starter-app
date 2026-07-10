# RSData | CompanyMAN | Sequências principais

## TOC

<!-- TOC -->

- [RSData | CompanyMAN | Sequências principais](#rsdata--companyman--sequ%C3%AAncias-principais)
    - [TOC](#toc)
    - [Introdução](#introdu%C3%A7%C3%A3o)
    - [Requisitos funcionais](#requisitos-funcionais)
        - [UC1 - Cadastrar ramo de atividade](#uc1---cadastrar-ramo-de-atividade)
        - [UC3 - Remover ramo de atividade incluindo falha por integridade referencial](#uc3---remover-ramo-de-atividade-incluindo-falha-por-integridade-referencial)
        - [UC5 - Cadastrar empresa](#uc5---cadastrar-empresa)
        - [UC6 - Editar Empresa](#uc6---editar-empresa)
        - [UC7 - Remover Empresa](#uc7---remover-empresa)
        - [UC8 - Listar empresas](#uc8---listar-empresas)
    - [Requisitos não-funcionais](#requisitos-n%C3%A3o-funcionais)
        - [UCW - Inicialização da aplicação criação de schema e seed de dados](#ucw---inicializa%C3%A7%C3%A3o-da-aplica%C3%A7%C3%A3o-cria%C3%A7%C3%A3o-de-schema-e-seed-de-dados)
    - [O que deseja fazer?](#o-que-deseja-fazer)

<!-- /TOC -->
## Introdução

No que tange aos casos de uso já explorados seguem aqui os respectivos diagramas de sequência. As convenções adotadas em todos os diagramas são as seguintes:

- **View**: página Facelets/PrimeFaces (`empresa/index.xhtml` ou `ramoAtividade/index.xhtml`).
- **Bean**: managed bean CDI (`EmpresaBean` / `RamoAtividadeBean`).
- **Service**: camada de regras de negócio (`EmpresaService` / `RamoAtividadeService`).
- **DAO**: camada de acesso a dados (`EmpresaDAO` / `RamoAtividadeDAO`).
- **DB**: banco de dados PostgreSQL, acessado via Hibernate/JPA (`EntityManager`).

## Requisitos funcionais

### UC1 - Cadastrar ramo de atividade

Fluxo análogo ao de cadastro de Empresa, porém com verificação de duplicidade pela `descricao` (case-insensitive).

```mermaid
sequenceDiagram
    actor Usuario as Usuário
    participant View as ramoAtividade/index.xhtml
    participant Bean as RamoAtividadeBean
    participant Service as RamoAtividadeService
    participant DAO as RamoAtividadeDAO
    participant DB as PostgreSQL

    Usuario->>View: clique em "Novo Ramo de Atividade"
    View->>Bean: prepararNovo()
    Bean-->>View: novoRegistro = new RamoAtividade()
    View-->>Usuario: exibe p:dialog (dlgNovo)

    Usuario->>View: preenche "descrição" e clica em "Salvar"
    View->>Bean: salvar()
    Bean->>Service: salvar(novoRegistro)
    Service->>DAO: buscarPorDescricao(descricao)
    DAO->>DB: SELECT r FROM RamoAtividade r <br/>WHERE lower(r.descricao) = lower(:descricao)
    DB-->>DAO: registro existente ou vazio
    DAO-->>Service: RamoAtividade | null

    alt descrição já cadastrada
        Service-->>Bean: throw DuplicateEntityException
        Bean-->>View: p:growl (warn) "Registro duplicado"
    else descrição inédita
        Service->>DAO: salvar(novoRegistro)
        DAO->>DB: BEGIN#59; INSERT INTO ramo_atividade (...)#59#59; COMMIT
        DB-->>DAO: ok
        DAO-->>Service: RamoAtividade persistido
        Service-->>Bean: RamoAtividade persistido
        Bean->>Bean: lista = null (invalida cache)
        Bean-->>View: p:growl (info) "Ramo de atividade cadastrado com sucesso"
    end

    View-->>Usuario: fecha diálogo e atualiza tabela
```

### UC3 - Remover ramo de atividade (incluindo falha por integridade referencial)

Este caso de uso evidencia uma regra de negócio implícita, garantida pelo banco de dados: um ramo de atividade não pode ser removido enquanto existirem empresas vinculadas a ele.

```mermaid
sequenceDiagram
    actor Usuario as Usuário
    participant View as ramoAtividade/index.xhtml
    participant Bean as RamoAtividadeBean
    participant Service as RamoAtividadeService
    participant DAO as RamoAtividadeDAO
    participant DB as PostgreSQL

    Usuario->>View: clique no ícone de remover
    View-->>Usuario: p:confirmDialog "Deseja realmente remover?"
    Usuario->>View: confirma ("Sim")
    View->>Bean: remover(id)
    Bean->>Service: remover(id)
    Service->>DAO: buscarPorId(id)
    DAO->>DB: SELECT r FROM RamoAtividade r WHERE id=?
    DB-->>DAO: RamoAtividade | null

    alt registro não encontrado
        Service-->>Bean: throw EntityNotFoundException
    else registro encontrado
        Service->>DAO: remover(id)
        DAO->>DB: BEGIN#59; DELETE FROM ramo_atividade WHERE id=?#59; COMMIT
        alt existem empresas com ramo_atividade_id = id
            DB-->>DAO: erro (violação de FK: fk_empresa_ramo_atividade)
            DAO-->>Service: propaga RuntimeException (rollback automático)
            Service-->>Bean: propaga exceção
            Note over Bean,View: não há tratamento amigável específico <br/>na implementação atual (oportunidade de melhoria)
        else nenhuma empresa vinculada
            DB-->>DAO: ok
            DAO-->>Service: void
            Service-->>Bean: void
            Bean->>Bean: lista = null (invalida cache)
            Bean-->>View: p:growl (info) "Ramo de atividade removido com sucesso"
        end
    end

    View-->>Usuario: atualiza p:dataTable (ou exibe erro, se aplicável)
```

### UC5 - Cadastrar empresa

```mermaid
sequenceDiagram
    actor Usuario as Usuário
    participant View as empresa/index.xhtml
    participant Bean as EmpresaBean
    participant Service as EmpresaService
    participant DAO as EmpresaDAO
    participant DB as PostgreSQL

    Usuario->>View: clique em "Nova Empresa"
    View->>Bean: prepararNovo()
    Bean-->>View: novoRegistro = new Empresa()
    View-->>Usuario: exibe p:dialog (dlgNovo)

    Usuario->>View: preenche campos e clica em "Salvar"
    View->>Bean: salvar()
    Bean->>Service: salvar(novoRegistro)
    Service->>DAO: buscarPorCnpj(cnpj)
    DAO->>DB: SELECT e FROM Empresa e WHERE e.cnpj = :cnpj
    DB-->>DAO: registro existente ou vazio
    DAO-->>Service: Empresa | null

    alt CNPJ já cadastrado
        Service-->>Bean: throw DuplicateEntityException
        Bean-->>View: p:growl (warn) "Registro duplicado"
    else CNPJ inédito
        Service->>DAO: salvar(novoRegistro)
        DAO->>DB: BEGIN#59; INSERT INTO empresa (...)#59; COMMIT
        DB-->>DAO: ok
        DAO-->>Service: Empresa persistida (id gerado)
        Service-->>Bean: Empresa persistida
        Bean->>Bean: lista = null (invalida cache)
        Bean-->>View: p:growl (info) "Empresa cadastrada com sucesso"
    end

    View->>Bean: getLista() (re-render do :form:tabela)
    Bean-->>View: List<Empresa> atualizada
    View-->>Usuario: fecha diálogo e atualiza tabela
```

### UC6 - Editar Empresa

```mermaid
sequenceDiagram
    actor Usuario as Usuário
    participant View as empresa/index.xhtml
    participant Bean as EmpresaBean
    participant Service as EmpresaService
    participant DAO as EmpresaDAO
    participant DB as PostgreSQL

    Usuario->>View: clique no ícone de edição de uma linha
    View->>Bean: prepararEdicao(empresa)
    Bean-->>View: selecionado = empresa
    View-->>Usuario: exibe p:dialog (dlgEditar) preenchido

    Usuario->>View: altera campos e clica em "Atualizar"
    View->>Bean: atualizar()
    Bean->>Service: atualizar(selecionado)
    Service->>DAO: buscarPorCnpj(cnpj)
    DAO->>DB: SELECT e FROM Empresa e WHERE e.cnpj = :cnpj
    DB-->>DAO: registro existente ou vazio
    DAO-->>Service: Empresa | null

    alt CNPJ pertence a outra Empresa
        Service-->>Bean: throw DuplicateEntityException
        Bean-->>View: p:growl (warn) "Registro duplicado"
    else CNPJ livre ou inalterado
        Service->>DAO: atualizar(selecionado)
        DAO->>DB: BEGIN#59; UPDATE empresa SET ... WHERE id=?#59; COMMIT
        DB-->>DAO: ok
        DAO-->>Service: Empresa atualizada (merge)
        Service-->>Bean: Empresa atualizada
        Bean->>Bean: lista = null (invalida cache)
        Bean-->>View: p:growl (info) "Empresa atualizada com sucesso"
    end

    View-->>Usuario: fecha diálogo e atualiza tabela
```

### UC7 - Remover Empresa

```mermaid
sequenceDiagram
    actor Usuario as Usuário
    participant View as empresa/index.xhtml
    participant Bean as EmpresaBean
    participant Service as EmpresaService
    participant DAO as EmpresaDAO
    participant DB as PostgreSQL

    Usuario->>View: clique no ícone de remover
    View-->>Usuario: p:confirmDialog "Deseja realmente remover esta empresa?"
    Usuario->>View: confirma ("Sim")
    View->>Bean: remover(id)
    Bean->>Service: remover(id)
    Service->>DAO: buscarPorId(id)
    DAO->>DB: SELECT e FROM Empresa e WHERE id=?
    DB-->>DAO: Empresa | null

    alt registro não encontrado
        Service-->>Bean: throw EntityNotFoundException
    else registro encontrado
        Service->>DAO: remover(id)
        DAO->>DB: BEGIN#59; DELETE FROM empresa WHERE id=?#59; COMMIT
        DB-->>DAO: ok
        DAO-->>Service: void
        Service-->>Bean: void
        Bean->>Bean: lista = null (invalida cache)
        Bean-->>View: p:growl (info) "Empresa removida com sucesso"
    end

    View-->>Usuario: atualiza p:dataTable
```

### UC8 - Listar empresas

Fluxo executado ao carregar a tela `empresa/index.xhtml` (idêntico, com os nomes trocados, para `ramoAtividade/index.xhtml`).

```mermaid
sequenceDiagram
    actor Usuario as Usuário
    participant View as empresa/index.xhtml
    participant Bean as EmpresaBean
    participant Service as EmpresaService
    participant DAO as EmpresaDAO
    participant DB as PostgreSQL

    Usuario->>View: acessa a página
    View->>Bean: getLista()
    alt cache (lista) ainda não carregado
        Bean->>Service: listarTodos()
        Service->>DAO: listarTodos()
        DAO->>DB: SELECT e FROM Empresa e <br/>JOIN FETCH e.ramoAtividade <br/>ORDER BY e.nomeFantasia
        DB-->>DAO: linhas do resultado
        DAO-->>Service: List<Empresa>
        Service-->>Bean: List<Empresa>
    end
    Bean-->>View: List<Empresa>
    View-->>Usuario: renderiza p:dataTable paginado
```

## Requisitos não-funcionais

### UCW - Inicialização da aplicação (criação de schema e seed de dados)

Executado uma única vez, quando o contexto do Tomcat é iniciado.

```mermaid
sequenceDiagram
    participant Tomcat
    participant Listener as DataInitializerListener
    participant JPAUtil
    participant Hibernate
    participant DB as PostgreSQL

    Tomcat->>Listener: contextInitialized(ServletContextEvent)
    Listener->>JPAUtil: getEntityManagerFactory()
    JPAUtil->>JPAUtil: lê DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD <br/>(variáveis de ambiente do Docker Compose)
    JPAUtil->>Hibernate: Persistence.createEntityManagerFactory("CompanyManPU", overrides)
    Hibernate->>DB: valida/gera schema <br/>(hibernate.hbm2ddl.auto=update)
    DB-->>Hibernate: tabelas ramo_atividade e empresa prontas
    Hibernate-->>JPAUtil: EntityManagerFactory

    Listener->>DB: SELECT COUNT(r) FROM RamoAtividade r
    alt tabela ramo_atividade vazia
        Listener->>DB: INSERT de 5 RamoAtividade iniciais
    end

    Listener->>DB: SELECT COUNT(e) FROM Empresa e
    alt tabela empresa vazia
        Listener->>DB: INSERT de 5 Empresa iniciais <br/>(uma para cada TipoEmpresa, vinculadas aos ramos)
    end

    Listener-->>Tomcat: contexto inicializado#59; <br/>aplicação pronta para receber requisições
```

---

## O que deseja fazer?

- [Voltar ao topo](#toc)
- [Voltar à raíz](../../../README.md)
- [Regras de negócio](./01-regras-de-negocio.md)
- [Entidades de domínio](./02-entidades-dominio.md)
- [Casos de uso](./03-casos-de-uso.md)
- [Release notes](./05-release-notes.md)
