---
id: TASK-26
title: Corrigir falha no primeiro login por colisão com o id do usuário semente
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - backend
  - auth
dependencies: []
references:
  - server/src/main/resources/db/migration/V5__inserir_usuario_unico.sql
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/service/UsuarioAutenticadoService.kt
priority: high
type: bug
ordinal: 26000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
A migração V5 insere o usuário único do MVP com `id = 1` usando `OVERRIDING SYSTEM VALUE`, o que não avança a sequência identity de `usuarios`. Num banco novo, o primeiro usuário real autenticado pelo Firebase tenta receber `id = 1`, viola a chave primária e a requisição responde 500: em produção, o primeiro login falha. A suíte atual só passa porque a ordem de execução dos testes consome a sequência antes. Reproduzido em um PostgreSQL descartável em 2026-11-01.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Em um banco novo com todas as migrações aplicadas, o primeiro usuário autenticado é criado sem erro e recebe id diferente do usuário semente
- [ ] #2 Em um banco que já tem usuários além do semente, a correção não altera dados e novos usuários continuam sendo criados
- [ ] #3 Teste de integração com Testcontainers cobre o primeiro login em banco recém-migrado e passa independentemente da ordem dos testes
<!-- AC:END -->
