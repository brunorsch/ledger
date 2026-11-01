@AGENTS.md

# Claude Code

## Fontes

- Requisitos do domain expert: `~/Documents/Obsidian/Projetos/Projetos/Ledger/` (`Ledger - Projeto.md`, `Exemplo planilha.md`). Somente leitura: nunca altere.
- Tasks: Backlog.md no diretório `backlog/` deste repositório, sempre pela CLI `backlog`. A pasta `backlog/` dentro do vault é uma cópia antiga; ignore-a.

## Fluxo

- Para executar uma task, use a skill `/executar-task TASK-N`.
- O passo "mostre um plano curto e espere aprovação" é feito em plan mode.
- O hook `SessionStart` (`.claude/settings.json`) injeta o `backlog instructions overview` no início da sessão. Se ele não aparecer no contexto, rode o comando manualmente.

## Verificação

- Backend: `./gradlew :server:test` (precisa do Docker para o Testcontainers). Uma classe só: `./gradlew :server:test --tests '*NomeDaClasseTest'`.
- Front: `pnpm --dir web lint` e `pnpm --dir web exec tsc -b`.
