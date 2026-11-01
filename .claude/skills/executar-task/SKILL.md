---
name: executar-task
description: Executa uma task do Backlog.md do Ledger de ponta a ponta, seguindo o fluxo do AGENTS.md (ler task e dependências, plano aprovado, implementação, testes, finalização no Backlog e commit só com aprovação). Use quando o usuário pedir para implementar, executar ou trabalhar em uma TASK-N.
argument-hint: TASK-N
---

# Executar task do Backlog.md

Task alvo: `$ARGUMENTS`. Uma task por sessão: se nenhuma task foi informada, pergunte qual.

## 1. Entender

1. Leia `backlog instructions task-execution` se ainda não leu nesta conversa.
2. `backlog task view $ARGUMENTS --plain`: descrição, critérios de aceite, "Fora do escopo" e grafo de dependências.
3. Leia as tasks de que ela depende. Um `ambiguous task ID` no grafo exige `backlog doctor` antes de continuar.
4. Leia as seções do `Ledger - Projeto.md` citadas na task (vault em `~/Documents/Obsidian/Projetos/Projetos/Ledger/`).
5. Pesquise o código atual, os testes e as convenções. Não confie na abordagem sugerida quando a task foi criada.

Diante de ambiguidade ou decisão de negócio, pare e pergunte. Não decida.

## 2. Planejar

1. `backlog task edit $ARGUMENTS -s "In Progress" -a @brunorsch`
2. Monte um plano curto (arquivos e abordagem) em plan mode e espere a aprovação explícita do usuário.
3. Registre o plano aprovado: `backlog task edit $ARGUMENTS --plan "1. ..."`

## 3. Implementar

- Só o escopo da task; o que está em "Fora do escopo" não se faz. Trabalho extra descoberto: pergunte se entra na task ou vira task nova.
- Cada critério de aceite coberto por pelo menos um teste automatizado.
- Registre decisões e progresso: `backlog task edit $ARGUMENTS --append-notes "..."`

## 4. Verificar

- Backend: `./gradlew :server:test` (rode a classe afetada com `--tests` durante o ciclo e a suíte completa no fim).
- Front, se mudou: `pnpm --dir web lint` e `pnpm --dir web exec tsc -b`.
- Repita até passar e anote os comandos usados.

## 5. Finalizar

1. Leia `backlog instructions task-finalization` e siga o guia.
2. Marque cada critério só com evidência objetiva: `backlog task edit $ARGUMENTS --check-ac N`
3. `backlog task edit $ARGUMENTS --final-summary "..."` citando os comandos de verificação.
4. `backlog task edit $ARGUMENTS -s "Done"`

## 6. Commit

Pare e peça a revisão do usuário antes de commitar: resuma os arquivos alterados e o resultado dos testes. Com a aprovação, faça um único commit da task no padrão `tipo(escopo): descrição ($ARGUMENTS)`, por exemplo `feat(server): ... (TASK-20)`.
