# RSData | CompanyMAN | Release Notes

## TOC

<!-- TOC -->

- [RSData | CompanyMAN | Release Notes](#rsdata--companyman--release-notes)
    - [TOC](#toc)
    - [Issues associadas do RedMine](#issues-associadas-do-redmine)
    - [Evolução cronológica das entregas](#evolu%C3%A7%C3%A3o-cronol%C3%B3gica-das-entregas)
    - [O que deseja fazer?](#o-que-deseja-fazer)

<!-- /TOC -->

## Issue(s) associada(s) do RedMine

- [#38828 - Trilha de Conhecimento – Onboarding Técnico](https://redmine.rsdata.com.br/issues/38828)

## Evolução cronológica das entregas

- **06/07/2026**:
    - Intervalo de tempo dedicado: **13:00 - 18:00**:
        - Estudo preliminar das documentações do JavaServer Faces e PrimeFaces de modo a relembrar conceitos-chave;
        - Criação ad-hoc de um repositório no Github, haja vista a carência de acessos no Gitlab corporativo;
        - Implementação da estrutura preliminar da infraestrutura do projeto, contendo as camadas de scripts de automação e virtualização via Docker.
- **07/07/2026**
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
- **08/07/2026**:
    - Intervalos de tempo dedicados:
        - **07:40 - 12:25**:
            - Condução de testes da pipeline de CI, com ênfase no job específico de execução do teste de integração implementado (ciclo de cadastro de empresa);
            - Condução de testes explorátórios do sistema propriamente dito, de modo a identificar falhas na camada de validação de campos no cadastro e edição de empresas e ramo de atividades;
            - Redação da documentação das diferentes camadas do sistema, i.e. **regras de negócio**, **entidades de domínio**, **casos de uso** e **sequências resultantes**
        - **13:32 - 18:00**:
---

## O que deseja fazer?

- [Voltar ao topo](#toc)
- [Voltar à raíz](../../../README.md)
- [Regras de negócio](./01-regras-de-negocio.md)
- [Entidades de domínio](./02-entidades-dominio.md)
- [Casos de uso](./03-casos-de-uso.md)
- [Sequências principais](./04-sequencias-principais.md)
