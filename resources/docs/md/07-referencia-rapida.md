# RSData | CompanyMAN | Referência rápida

## TOC

<!-- TOC -->

- [RSData | CompanyMAN | Referência rápida](#rsdata--companyman--refer%C3%AAncia-r%C3%A1pida)
    - [TOC](#toc)
    - [Introdução](#introdu%C3%A7%C3%A3o)
    - [Preliminares](#preliminares)
        - [WSL](#wsl)
        - [Ferramentas](#ferramentas)
    - [Incialização](#incializa%C3%A7%C3%A3o)
        - [Java/Maven](#javamaven)
        - [Plataforma](#plataforma)
            - [Banco de dados](#banco-de-dados)
            - [Servidor de aplicação](#servidor-de-aplica%C3%A7%C3%A3o)
    - [Primeiro acesso](#primeiro-acesso)
    - [O que deseja fazer?](#o-que-deseja-fazer)

<!-- /TOC -->

## Introdução

Aqui é apresentação uma sequência útil de interação com a plataforma de software em questão, desde o processo de build até a utilização da mesma como usuário.

> [!IMPORTANT]
>
> A estrutura do `Makefile` foi implementada tendo em vista que os comandos e scripts associados serão executados em um ambiente com suporte ao `bash`. Em um ambiente Windows, isso pode ser obtido, via Powershell, através da instalação do **Subsistema de Windows para Linux (WSL - _Windows Subsystem for Linux_)**. Para maiores informações, acesse a [documentação](https://learn.microsoft.com/pt-br/windows/wsl/install). Para este ambiente, foi usada a distribuição `Ubuntu` na instalação.

## Preliminares

### WSL

Com o Powershell aberto, execute os seguintes comandos:

```powershell
wsl --install -d Ubuntu     # Instala o WSL para a distro específicada
wsl -d Ubuntu               # Inicia uma sessão do WSL no terminal para a distro especificada
```

### Ferramentas

No que tange às ferramentas utilizadas na construção dessa plataforma, abaixo segue uma listagem das mesmas e suas versões, tendo em mente que as mesmas estão sendo usada no contexto do WSL:

- [SDKMAN](https://sdkman.io/) (v5.23.9 (script), v.0.7.34 (native) ou superior): Gerenciador de versões do Java Development Kit (JDK) e das ferramentas que gravitam em torno do mesmo. Já utilizo há algum tempo e considero muito versátil. A mesma foi utilizada para instalar o seguinte:
    - [Java (java/javac)](https://www.java.com/pt-BR/download/): v21.0.2 ou superior;
    - [Apache Maven](https://maven.apache.org/install.html): v3.9.6 ou superior;
- [Docker](https://docs.docker.com/engine/install/): v29.6.1 ou superior;
    - [Docker Compose](https://docs.docker.com/compose/): v5.3.1 ou superior;
- [GNU Make](https://web.mit.edu/gnu/doc/html/make_15.html): v4.4.1 ou superior.

> [!NOTE]
>
> Concernindo a instalação do Docker, muito provavelmente você vai operar este sistema de um ambiente Windows como Docker Desktop já instalado, i.e. com o Daemon Docker já sendo iniciado em Windows. Com efeito, para assegurar que a execução do Daemon será replicada ao ambiente do WSL, siga essas [instruções](https://docs.docker.com/desktop/features/wsl/#turn-on-docker-desktop-wsl-2).

## Incialização

### Java/Maven

Como mencionado anteriormente, o sistema propriamente dito com construído usando Java (JDK v21) e o gerenciador de pacotes Maven (v3.9.6). A mesma está concentrada na pasta [`companyman`](../../../companyman/) e, associado ao [pom.xml](../../../companyman/pom.xml) do projeto, estão configurados os plugins de compilação de análise estática do código-fonte. De modo a testar a configuração destes plugins, execute esses comandos na raíz do projeto:

```bash
cd companyman           # Pasta da aplicação, onde está o pom.xml
mvn clean               # Remove, caso exista, a pasta `target` na qual os resultados de compilação (packaging) do projeto são publicados
mvn package             # Realiza a compilação (empacotamento) do projeto, executando testes unitários
mvn spotless:apply      # Aplicação da ferramenta de formatação e linting do código-fonte, aplicando correções onde for necessário
mvn spotless:check      # Aplicação da ferramenta de formatação e linting do código-fonte, sem aplicação de correções
mvn verify              # Compila o projeto e executa todos os testes automatizados (unitários de integração)
```

> [!NOTE]
>
> - Os mesmos comandos acima podem ser também executados em modo não-interativo no terminal, e.g. como é feito nas pipelines de CI em [.github/workflows](../../../.github/workflows/). Esse modo de execução pode ser testado através de ``mvn -B -ntp <comando>`.
>
> - Caso você não queria sair da pasta-raíz do projeto, basta passar o argumento `-f` ao `mvn`, e.g. `mvn -f companyman/pom.xml <comando>`.

### Plataforma

Similarmente ao exposto na raíz dessa documentação, a inicialização dos serviços necessários, i.e. servidores de banco de dados e de aplicação (PostgreSQL e Apache Tomcat), é realizada através de scripts do `make`. Com efeito:

#### Banco de dados

```bash
make build c=database       # Faz a build da imagem Docker
make init c=database        # Inicia o contêiner com logs
```

Caso tudo ocorra sem erros, Caso tudo ocorra sem erros, o servidor PostgreSQL estará aceitando conexões na porta 5001, com os logs apresentando:

![Contêineres iniciados](../images/01-logs-conteiner-postgresql.png)

#### Servidor de aplicação

```bash
make build c=tomcat       # Faz a build da imagem Docker
make init c=tomcat        # Inicia o contêiner com logs
```

Caso tudo ocorra sem erros, Caso tudo ocorra sem erros, o servidor Tomcat estará aceitando conexões na porta 8080, com os logs apresentando:

![Contêineres iniciados](../images/02-logs-conteiner-tomcat.png)

> [!NOTE]
>
> Equivalentemente, maiores informações acerca dos contêineres podem ser obtidas através do comando `make ps`:
>
> ![Contêineres iniciados](../images/03-conteineres-iniciados.png)

## Primeiro acesso

De posse de um navegador web, o sistema CompanyMAN pode agora ser acessado através de [http://localhost:8080](http://localhost:8080). A tela inicial do sistema, nesta versão, correspondo ao CRUD de empresas:

![Página inicial](../images/04-pagina-inicial-companyman.png)

> [!NOTE]
>
> Os navegadores nos quais o sistema foi testado foram:
> - Google Chrome
> - Brave Browser
> - Microsoft Edge
> - Mozilla Firefox
>
> Caso anormalidades sejam detectadas, favor [entrar em contato](mailto:gulherme.goncalves@rsdata.inf.br) com o desenvolvedor do sistema.

---

## O que deseja fazer?

- [Voltar ao topo](#toc)
- [Voltar à raíz](../../../README.md)
- [Regras de negócio](./01-regras-de-negocio.md)
- [Entidades de domínio](./02-entidades-dominio.md)
- [Casos de uso](./03-casos-de-uso.md)
- [Sequências principais](./04-sequencias-principais.md)
- [Validação e exportação](./05-validacao-exportacao.md)
- [Release notes](./06-release-notes.md)
