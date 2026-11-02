---
id: TASK-29
title: Validar parâmetros de cálculo na criação do mês
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - backend
dependencies: []
references:
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/domain/ParametrosCalculo.kt
priority: high
type: bug
ordinal: 29000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
A API aceita qualquer `parametrosCalculo` ao criar um mês. Um percentual negativo, um percentual acima de 100% ou essenciais e lazer somando mais de 100% são gravados no snapshot e fazem o GET do sumário do mês responder 500. Como o snapshot não pode ser alterado depois da criação, o mês fica quebrado para sempre. O front valida esses campos, mas é a API que protege o snapshot.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Criar mês com percentual de essenciais ou de lazer negativo ou acima de 100% (acima de 1 na API) retorna 400 com mensagem que identifica o campo
- [ ] #2 Criar mês com essenciais + lazer acima de 100% retorna 400
- [ ] #3 Criar mês com teto negativo retorna 400
- [ ] #4 Nenhum mês é gravado quando a validação falha
- [ ] #5 Valores nos limites continuam aceitos: percentual 0% e 100%, soma exatamente 100% e teto zero
- [ ] #6 Testes unitários cobrem as regras, e um teste de integração da API cobre a rejeição com 400
<!-- AC:END -->
