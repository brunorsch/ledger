---
id: TASK-26
title: Corrigir falha no primeiro login por colisão com o id do usuário semente
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-11-01 06:52'
updated_date: '2026-11-02 22:59'
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
- [x] #1 Em um banco novo com todas as migrações aplicadas, o primeiro usuário autenticado é criado sem erro e recebe id diferente do usuário semente
- [x] #2 Em um banco que já tem usuários além do semente, a correção não altera dados e novos usuários continuam sendo criados
- [x] #3 Teste de integração com Testcontainers cobre o primeiro login em banco recém-migrado e passa independentemente da ordem dos testes
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Nova migração V10 que ajusta a sequência identity de usuarios para max(id), sem alterar linhas.
2. PrimeiroLoginApiTest com container próprio (banco recém-migrado): primeira requisição autenticada retorna 200 e cria usuário com id diferente de 1 (AC#1, AC#3).
3. Teste em MigracoesPostgresTest: Flyway até V9 em database novo, usuários com ids 2 e 3, migra até a última versão, linhas inalteradas e próximo id gerado é 4 (AC#2).
4. Verificar com ./gradlew :server:test.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Antes da V10, PrimeiroLoginApiTest falhou com 500 e o teste de migração com DuplicateKeyException em Key (id)=(1). Depois da V10, os dois passam. PrimeiroLoginApiTest usa um container próprio para que a requisição seja de fato o primeiro login. Validação: ./gradlew :server:test com 144 testes, 0 falhas.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
A nova migração V10 posiciona a sequência identity de usuarios em max(id) e não altera nenhuma linha. Com isso, o primeiro usuário autenticado deixa de colidir com o id 1 do seed da V5. Testes: PrimeiroLoginApiTest faz o primeiro login via API num banco recém-migrado, com container próprio (AC#1, AC#3). Um teste novo em MigracoesPostgresTest migra até a V9, insere usuários 2 e 3, migra até o fim e confirma que os dados não mudaram e que o próximo id é 4 (AC#2). Os dois falhavam antes da correção. Verificado com ./gradlew :server:test (144 testes, 0 falhas).
<!-- SECTION:FINAL_SUMMARY:END -->
