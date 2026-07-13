# RSData | CompanyMAN | Release Notes

## TOC

<!-- TOC -->

- [RSData | CompanyMAN | Release Notes](#rsdata--companyman--release-notes)
    - [TOC](#toc)
    - [Issues associadas do RedMine](#issues-associadas-do-redmine)
    - [Evolução cronológica das entregas](#evolu%C3%A7%C3%A3o-cronol%C3%B3gica-das-entregas)
        - [Dia 06/07/2026:](#dia-06072026)
        - [Dia 07/07/2026](#dia-07072026)
        - [Dia 08/07/2026:](#dia-08072026)
        - [Dia 09/07/2026:](#dia-09072026)
        - [Dia 10/07/2026:](#dia-10072026)
        - [Dia 13/07/2026:](#dia-13072026)
    - [O que deseja fazer?](#o-que-deseja-fazer)

<!-- /TOC -->

## Issue(s) associada(s) do RedMine

- [#38828 - Trilha de Conhecimento – Onboarding Técnico](https://redmine.rsdata.com.br/issues/38828)

## Evolução cronológica das entregas

### Dia 06/07/2026:

- Intervalos de tempo dedicados:
    - **13:00 - 18:00**:
        - Estudo preliminar das documentações do JavaServer Faces e PrimeFaces de modo a relembrar conceitos-chave;
        - Criação ad-hoc de um repositório no Github, haja vista a carência de acessos no Gitlab corporativo;
        - Implementação da estrutura preliminar da infraestrutura do projeto, contendo as camadas de scripts de automação e virtualização via Docker.

### Dia 07/07/2026

- Intervalos de tempo dedicados:
    - **07:46 - 12:30**:
        - Orientações ao Juan e Noah no que tange aos bloqueios iniciais para configuração do ambiente de trabalho;
        - Tomada de decisão técnica concernente à estrutura preliminar do projeto sugerida no curso da AlgaWorks. Decidido que uma estrutura mais robusta para a aplicação seria implantada, ainda seguindo o padrão MVC.
        - Reestruturação da estrutura de dockerização e de scripts para comportar a implementação da aplicação propriamente dita;
        - Implementação da camada de domínio, i.e. **ramo de atividade** e **empresa**;
        - Implementação das camadas utilitárias do backend, i.e. `exception`, `util`, com atenção especial à carga de dados inicial nas duas tabelas do banco (`ramo_atividade` e `empresa`) e à replicação de configurações via camada de abstração `JPAUtil` com variáveis de ambiente (visíveis somente ao container) em `persistence.xml` e em outros pontos.
    - **13:40 - 18:15**:
        - Implementação da camada de persistência, i.e. `service`, `model`;
        - Implementação da camada de infraestrutura, i.e. `controller` com os respectivos beans para consumo da UI JSF;
        - Implementação de testes unitários (um para cada domínio) e um teste de integração, para o ciclo completo de CRUD de uma empresa;
        - Implementação da pipeline de uma pipeline de CI, para execução dos testes;

### Dia 08/07/2026:

- Intervalos de tempo dedicados:
    - **07:40 - 12:25**:
        - Condução de testes da pipeline de CI, com ênfase no job específico de execução do teste de integração implementado (ciclo de cadastro de empresa);
        - Condução de testes explorátórios do sistema propriamente dito, de modo a identificar falhas na camada de validação de campos no cadastro e edição de empresas e ramo de atividades;
        - Redação da documentação das diferentes camadas do sistema, i.e. **regras de negócio**, **entidades de domínio**, **casos de uso** e **sequências resultantes**
    - **13:32 - 18:00**:
        - Estudo da documentação do JSF/PrimeFaces, com ênfase em particular aos mecanismos de gerenciamento de estados via _Managed Beans_ e também aos componentes de controle de I/O em formulários;
        - Início da implementação do frontend JSF/PrimeFaces, priorizando os telas correspondentes aos casos de uso principais do sistema, e.g. CRUD de empresas e de ramos de atividade;
        - Testes iniciais das telas de gerenciamento de empresas e de ramos de atividades, em sua forma inicial, sem a camada de validação de campos encaixada, de forma a validar a integação entre _Managed Beans_ e os componentes JSF.

### Dia 09/07/2026:

- Intervalos de tempo dedicados:
    - **08:30 - 12:30**:
        - Implementação de um validador customizado para o CNPJ (`CNPJValidator`) e de uma constraint (decorador `@CNPJ`), baseado no algorítmo original, assim também como os testes unitários para cobertura dessa camada;
        - Integração do validador customizado e de outros pontos de validação na camada de modelos dos respectivos domínios do sistema;
        - Implementação no frontend PrimeFaces dos mecanismos de tratamento das mensagens de erros de validação com `p:growl` específicos para os componentes de entrada de dados;
    - **13:40 - 18:00**:
        - Depuração mais profunda da execução do encadeamento lógico dos testes em `EmpresaIntegrationTest.java`, no tocante à falha causada pela não-detecção do _daemon_ Docker durante a instância do `PostgreSQLContainer`;
        - Implementação de uma tela (modal) informativa acerca das informações do sistema, (_"About" dialog_);
        - Testes da aplicação nos navegadores Chrome, Edge, Brave e Firefox;

### Dia 10/07/2026:

- Intervalos de tempo dedicados:
    - **08:31 - 12:17**:
        - Correção do problema existente na execução do teste de integração `EmpresaIntegrationTest.java`;
        - Reconstrução da renderização do menu principal da aplicação, de modo a torná-lo vertical, colapsável e responsivo, com os seus submenus sendo criados programaticamente via controller/bean dedicado;
        - Definição da estratégia de exportação de dados (empresas e ramos de atividades), assim como os metadados associados ao processo de exportação como paginação, número de registros, data e hora e identidade visual da empresa no cabeçalho e rodapé dos formatos pertinentes;
    - **13:20 - 16:00**:
        - Início da implementação da camada de exportação de dados nos formatos PDF, ODT, XLS e CSV;
        - Expansão da cobertura de testes de unidade;
        - Condução de testes exploratórios da plataforma, em diferentes navegadores.

### Dia 13/07/2026:

- Intervalos de tempo dedicados:
    - **08:26 - 12:31**:
        - Incorporação da camada de metadados de exportação aos diferentes formatos de saída suportados;
        - Ajuste no layout principal (`layout.xhtml`), em particular na formação dinâmica do título da página através das informações extraídas de `sobreSistemaBean`;
        - Ajuste em `TabularExporter.java`, em particular na maneira como o monograma da empresa no cabeçalho do PDF de exportação estava sendo gerado;
        - Ajustes nos componentes de frotend e na sua folha de estilos (`app.css`);
        - Expansão da cobertura de testes de unidade;
        - Expansão da documentação do sistema;
    - **13:31- 18:00**:

---

## O que deseja fazer?

- [Voltar ao topo](#toc)
- [Voltar à raíz](../../../README.md)
- [Regras de negócio](./01-regras-de-negocio.md)
- [Entidades de domínio](./02-entidades-dominio.md)
- [Casos de uso](./03-casos-de-uso.md)
- [Sequências principais](./04-sequencias-principais.md)
- [Validação e exportação](./05-validacao-exportacao.md)
- [Referência rápida](./07-referencia-rapida.md)
