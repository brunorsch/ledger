# Ledger: instruções para agentes de desenvolvimento

## Contexto

App web de controle financeiro pessoal, centrado no mês. Substitui uma planilha. As tasks vivem no Backlog.md, no vault de planejamento (Obsidian), e são consultadas por `<como acessar: integração/MCP/caminho>`. Os documentos de requisitos são do domain expert: nunca os altere.

## Estrutura

```
ledger/
├── web/          # React + Vite + TypeScript (pnpm)
├── server/      # Spring Boot, Kotlin, Spring Data JDBC, PostgreSQL, Flyway
├── compose.yaml  # PostgreSQL local (porta 65432)
└── settings.gradle.kts, build.gradle.kts, gradlew
```

Gradle multiprojeto (módulos `web` e `server`). O artefato final é um único JAR: o backend serve o front, e a API fica sob `/api`.

## Comandos

- Banco local: `docker compose up -d` (para zerar os dados: `docker compose down -v`)
- Backend: `SPRING_PROFILES_ACTIVE=local ./gradlew :backend:bootRun`
- Testes do backend: `./gradlew :backend:test`
- Front em desenvolvimento: `cd web && pnpm dev` (proxy de `/api` para o backend)
- Build do front: `./gradlew :web:pnpmBuild`
- JAR final: `./gradlew :backend:bootJar`

## Regras de código

- Código limpo, legível por humanos e sem abstração especulativa. Faça só o que a task pede.
- O código limpo também deve ser considerado para testes. Evitando duplicações, usando nomes claros, setup/teardown, etc.
- Domínio puro: o cálculo do sumário e da distribuição das sobras não depende do Spring.
- Dinheiro: tipo `Money` (value class sobre `BigDecimal`, escala 2), `NUMERIC(14,2)` no banco. Nunca `Double` ou `Float`. Um único modo de arredondamento, aplicado no domínio (o banco não arredonda). A API envia e recebe valores como string. Reservas = restante − essenciais − lazer.
- Persistência: Spring Data JDBC, sem JPA/Hibernate. Domínio imutável (`data class` e `copy`). Agregados pequenos, com referência entre agregados por id (`AggregateReference`). Telas de leitura usam SQL ou projeção, sem montar agregados.
- Schema só por migração Flyway em `backend/src/main/resources/db/migration` (`V<n>__descricao.sql`). Nunca edite uma migração já aplicada: crie outra.
- Toda tabela de dados do usuário tem `user_id`.
- O snapshot de parâmetros de cada mês tem `schemaVersion`.
- O passado não muda: cada mês guarda seus lançamentos concretos, o modelo de gastos fixos só gera cópias quando um mês é criado, e os parâmetros ficam gravados no mês na criação.
- Não monte pilha de observabilidade além do que a task pedir.
- Utilizar métodos de classes ao invés de funções top-level quando o comportamento estiver relacionado a uma classe específica. Funções top-level são permitidas para utilitários gerais, mas evite criar funções globais desnecessárias.
- Criar uma classe por arquivo, com o mesmo nome do arquivo. Evitar arquivos com múltiplas classes, exceto para classes internas ou pequenas classes auxiliares.
- O código deve ser escrito com a terminologia em português, exceto quando se tratar de nomenclatura padrão de mercado. 
- Ex: Packages `domain`, `controller`, `repository`, `service`, `dto`, `exception`, `config`, etc. podem ser mantidos em inglês
- Sufixo de classes como `Controller`, `Repository`, `Service`, `DTO`, `Exception` podem ser mantidos em inglês 
- Terminologia em inglês que são padrão como `get`, `set`, `unwrap`, `toString`, `equals`, `hashCode`, `compareTo`, `copy`, etc. podem ser mantidos em inglês
- Separação de testes em Arrange, Act e Assert, mas sem explicitar com comentários. Deixando uma linha em branco para separar cada etapa.

## Frontend

- React com TypeScript estrito e pnpm.
- Não faz conta de dinheiro: recebe strings da API e só formata para exibição em BRL.
- Sem testes de frontend por enquanto. Não instale Vitest.

## Testes

- Cada critério de aceite da task é coberto por pelo menos um teste automatizado.
- Unitários para os cenários. Integração da API com pelo menos o caminho feliz, usando PostgreSQL real (Testcontainers), nunca banco em memória.
- Compare valores monetários com `compareTo`, não com `equals`.

## Fluxo de trabalho

1. Uma task por sessão. Leia a task e as tasks de que ela depende.
2. Antes de codar, mostre um plano curto (arquivos e abordagem) e espere aprovação.
3. Implemente só o escopo da task. O que está em "fora do escopo" não se faz.
4. Rode os testes até passarem e informe o comando usado.
5. Siga `backlog instructions task-execution` e `backlog instructions task-finalization`, atualize o status da task e faça um commit por task, citando o id dela.
6. Diante de ambiguidade ou decisão de negócio, pare e pergunte. Não decida.

## Segurança

- Não commite segredos. Em produção a conexão vem de variáveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`). As credenciais `ledger/ledger` valem só no ambiente local.
- Dados são financeiros: não registre valores nem dados pessoais em log.

## Glossário

gasto, receita, restante, a pagar, essenciais, lazer, reservas, mês (projetado, aberto ou fechado). 
Idioma dos identificadores no código: português.

<!-- BACKLOG.MD GUIDELINES START -->
<!-- backlog.md-instructions-version: 1.53.0 -->
<CRITICAL_INSTRUCTION>

## Backlog.md Workflow

This project uses Backlog.md for task and project management.

**At the beginning of each conversation in this project, run `backlog instructions overview` before answering or taking action. Re-read it only if you have not read it yet in the current conversation.**

Use the overview to decide whether to search, read, create, or update Backlog tasks.

Before task lifecycle actions, read the matching detailed guide:
- `backlog instructions task-creation` before creating or splitting tasks
- `backlog instructions task-execution` before planning, changing status or assignee, adding a plan or implementation notes, or implementing task work
- `backlog instructions task-finalization` before checking acceptance criteria, writing final summaries, or moving tasks to terminal statuses

Use `backlog <command> --help` before running unfamiliar commands. Help shows options, fields, and examples.

Do not edit Backlog task, draft, document, decision, or milestone markdown files directly. Use the `backlog` CLI so metadata, relationships, and history stay consistent.

</CRITICAL_INSTRUCTION>
<!-- BACKLOG.MD GUIDELINES END -->
