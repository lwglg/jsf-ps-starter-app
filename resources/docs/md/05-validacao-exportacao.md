# RSData | CompanyMAN | Validação e exportação de dados

## TOC

<!-- TOC -->

- [RSData | CompanyMAN | Validação e exportação de dados](#rsdata--companyman--valida%C3%A7%C3%A3o-e-exporta%C3%A7%C3%A3o-de-dados)
    - [TOC](#toc)
    - [Camada de validação de campos](#camada-de-valida%C3%A7%C3%A3o-de-campos)
    - [Camada de exportação de dados](#camada-de-exporta%C3%A7%C3%A3o-de-dados)
    - [O que deseja fazer?](#o-que-deseja-fazer)

<!-- /TOC -->

## Camada de validação de campos

Além das regras de negócio (unicidade), as entidades possuem restrições de **Jakarta Bean Validation** (Hibernate Validator), aplicadas em duas camadascomplementares:

1. **Camada de apresentação (JSF/PrimeFaces)**: os componentes ligados diretamente aos atributos das entidades (ex.: `value="#{empresaBean.novoRegistro.cnpj}"`) disparam automaticamente essas validações durante o processamento do formulário, exibindo a mensagem correspondente em `<p:message for="...">`.
2. **Camada de serviço**: `EntityValidator.validar(entidade)` é chamado explicitamente em `EmpresaService`/`RamoAtividadeService` antes de qualquer verificação de duplicidade, garantindo a validação mesmo para chamadas que não passem pela interface web (ex.: testes, uma futura API).

| Entidade      | Atributo        | Restrições                                                                    |
|---------------|-----------------|-------------------------------------------------------------------------------|
| RamoAtividade | `descricao`     | `@NotBlank`, `@Size(min=3, max=150)`                                          |
| Empresa       | `nomeFantasia`  | `@NotBlank`, `@Size(max=80)`                                                  |
| Empresa       | `razaoSocial`   | `@NotBlank`, `@Size(max=120)`                                                 |
| Empresa       | `cnpj`          | `@NotBlank`, `@Size(max=18)`, `@CNPJ` (dígito verificador, módulo 11, custom) |
| Empresa       | `dataFundacao`  | `@NotNull`, `@PastOrPresent`                                                  |
| Empresa       | `ramoAtividade` | `@NotNull`                                                                    |
| Empresa       | `tipoEmpresa`   | `@NotNull`                                                                    |
| Empresa       | `faturamento`   | `@NotNull`, `@DecimalMin("0.00")`, `@Digits(integer=8, fraction=2)`           |

A restrição `@Digits(integer=8, fraction=2)` do `faturamento` reflete exatamente a coluna `precision=10, scale=2` do banco — evitando que um valor fora da faixa suportada chegue ao PostgreSQL e gere um erro de banco de dados pouco amigável (`... must round to an absolute value less than 10^8`), substituindo-o por uma mensagem de validação clara na interface.

Violações de validação são agregadas em `ValidationException` (uma por requisição), listando todas as mensagens de uma só vez, e tratadas pelos managed beans (`EmpresaBean`/`RamoAtividadeBean`) da mesma forma que `DuplicateEntityException` — exibidas via `p:growl`.

## Camada de exportação de dados

A exportação de dados de **Empresas** e de **Ramos de Atividade** é
convergida em um único modal ("Exportar Dados"), acessível pelo menu
superior em qualquer tela, no qual o usuário escolhe:

1. **Origem dos dados** — Empresas ou Ramos de Atividade;
2. **Formato** — CSV, XLS, ODT ou PDF;
3. **Registros a exportar (escopo)** — Todos os registros, Somente os selecionados na tabela, ou Somente a página atual da tabela;
4. **Nome do arquivo** — opcional; se não informado, é gerado um nome padrão (`<origem>_<data-hora>.<extensão>`).

> [!IMPORTANT]
>
> **Como o escopo "selecionados"/"página atual" é resolvido:** os `p:dataTable` de `empresa/index.xhtml` e `ramoAtividade/index.xhtml` ganharam uma coluna de checkbox (`selectionMode="multiple"`) vinculada a `EmpresaBean.selecionados`/`RamoAtividadeBean.selecionados`, e o atributo `first` vinculado a `primeiroRegistro` (posição de paginação). Como o modal "Exportar Dados" é compartilhado por todas as telas (e o `ExportDownloadServlet` é um servlet comum, fora do ciclo do JSF), o escopo escolhido é resolvido consultando esse estado diretamente nos managed beans `@SessionScoped` — injetados no servlet via CDI (`@Inject`, habilitado pelo Weld já configurado em `web.xml`). Isso implica uma consequência prática: o escopo "selecionados"/"página atual" reflete o que estava marcado/exibido **na última vez que aquela tela foi visitada nesta sessão**, mesmo que o modal seja aberto a partir de outra tela.

Todo arquivo gerado, independentemente do formato, inclui:

- o **total de registros** exportados (`Total de registros: N`) — já refletindo o escopo escolhido, não necessariamente o total geral;
- a **data/hora de geração** (`Gerado em dd/MM/yyyy às HH:mm:ss`).

Em **PDF** e **ODT** — onde o conceito de "página" existe de fato — o relatório também inclui um **cabeçalho** (logo da empresa + nome) e um **rodapé com paginação** (`Página X de Y`) repetidos em todas as páginas. Em CSV e XLS esses elementos não fazem sentido (não há "páginas" nesses formatos), por isso ficam de fora, conforme pedido.

> [!IMPORTANT]
>
> - **Sobre o logo no cabeçalho de PDF/ODT:** como `TabularExporter` e os serviços de exportação são POJOs simples, sem acesso ao `ServletContext` (necessário para localizar o arquivo `rsdata_logo.svg` da webapp em disco) e sem uma biblioteca de rasterização de SVG no classpath, o cabeçalho usa um **monograma gerado programaticamente** (`LogoMonogramGenerator`, via Java2D) com as iniciais de `APP_COMPANY_NAME` — e não o arquivo `rsdata_logo.svg` de fato usado no modal "Sobre o sistema". Trocar por um PNG/JPEG real é um ponto de extensão simples (bastaria ler os bytes do arquivo a partir do `ServletContext`, disponível em `ExportDownloadServlet`, no lugar de chamar `LogoMonogramGenerator`).
> - **Contraste no cabeçalho da tabela em PDF:** as células de cabeçalho da tabela de dados usam fundo azul-escuro (`#1F2937`) — a fonte dessas células é branca (antes era preta, quase ilegível sobre esse fundo), garantindo contraste adequado.

A exportação é uma operação somente leitura — não altera o estado do domínio — e por isso não participa das regras de duplicidade/validação descritas nas seções anteriores.

> [!IMPORTANT]
>
> **Como o "Salvar Como" nativo é aberto, e por que o Firefox se comporta diferente:** o servidor não tem como forçar o navegador a exibir uma caixa de diálogo (isso normalmente depende de uma configuração do próprio usuário). A exceção é a **File System Access API** (`window.showSaveFilePicker()`), suportada nativamente pelo **Chrome**, **Brave** e **Edge** (todos baseados em Chromium): ela abre o diálogo "Salvar Como" real do sistema operacional, sob comando da página. O **Firefox não implementa essa API** — para ele, o modal cai em um link `<a download>` padrão, cujo comportamento (baixar direto vs. perguntar onde salvar) depende da configuração de downloads do próprio Firefox. Essa lógica de detecção/fallback vive inteiramente no JavaScript de `dialogs/exportar-dados/index.xhtml`.

| Classe                       | Responsabilidade                                                                                                                |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `TabularExporter`            | Converte cabeçalhos + linhas de texto em bytes, para cada formato; desenha cabeçalho/rodapé/paginação em PDF e ODT              |
| `MetadadosExportacao`        | Título, total de registros, data/hora de geração e nome da empresa — comuns a todo relatório                                    |
| `LogoMonogramGenerator`      | Gera programaticamente (Java2D) o monograma usado no cabeçalho de PDF/ODT                                                       |
| `EmpresaExportService`       | Converte `List<Empresa>` em linhas de texto e delega ao `TabularExporter`                                                       |
| `RamoAtividadeExportService` | Converte `List<RamoAtividade>` em linhas de texto e delega ao `TabularExporter`                                                 |
| `ExportDownloadServlet`      | Servlet dedicado (`/export/download`) que gera os bytes do arquivo, consumido via `fetch()` pelo JavaScript da tela             |
| `OrigemExportacao`           | Enumeração de qual conjunto de dados exportar (`EMPRESA`, `RAMO_ATIVIDADE`)                                                     |
| `EscopoExportacao`           | Enumeração do escopo de registros a exportar (`TODOS`, `SELECIONADOS`, `PAGINA_ATUAL`)                                          |
| `ExportFormat`               | Enumeração dos formatos suportados (`CSV`, `XLS`, `ODT`, `PDF`), com extensão e `Content-Type` de cada um                       |
| `ExportBean`                 | Managed bean que apenas expõe as opções (`origens`/`formatos`/`escopos`) para os `<select>` do modal — sem estado de requisição |

Nenhuma biblioteca de terceiros conhece diretamente as entidades de domínio — `TabularExporter` trabalha apenas com `String[]` (cabeçalhos e linhas), o que mantém `Empresa`/`RamoAtividade` desacopladas do formato de exportação e permite adicionar um novo formato no futuro sem tocar nos serviços de exportação por entidade.

> [!IMPORTANT]
> 
> **Por que um servlet, e não uma ação de managed bean:** o JavaScript precisa consumir a resposta via `fetch().blob()` para poder repassar o conteúdo ao `showSaveFilePicker()` — algo que um postback JSF tradicional não oferece. Por isso a geração do arquivo foi extraída para um servlet simples (`@WebServlet("/export/download")`), fora do ciclo de vida do JSF, mas reaproveitando os mesmos serviços de domínio e de exportação.

---

## O que deseja fazer?

- [Voltar ao topo](#toc)
- [Voltar à raíz](../../../README.md)
- [Regras de negócio](./01-regras-de-negocio.md)
- [Entidades de domínio](./02-entidades-dominio.md)
- [Casos de uso](./03-casos-de-uso.md)
- [Sequências principais](./04-sequencias-principais.md)
- [Sequências principais](./06-release-notes.md)
- [Referência rápida](./07-referencia-rapida.md)
