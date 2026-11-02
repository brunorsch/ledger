---
id: TASK-34
title: Remover o campo deficit sem uso do mês
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - backend
  - frontend
dependencies: []
references:
  - server/src/main/kotlin/br/dev/brunorsch/ledger/server/dto/MesResponse.kt
priority: low
type: bug
ordinal: 34000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
A tabela `meses` tem a coluna `deficit`, e o `MesResponse` a expõe, mas o `MesService` sempre grava zero e nenhum fluxo a atualiza. O déficit real é calculado na distribuição das sobras e já vem no sumário, que é o que o front exibe. Um campo fixo em zero no mês engana quem consome a API e pode acabar sendo usado no lugar do valor calculado. Os requisitos não preveem déficit persistido.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 A resposta da API do mês não tem mais o campo deficit, e o déficit continua disponível na distribuição do sumário
- [ ] #2 A coluna deficit da tabela meses é removida por nova migração
- [ ] #3 O tipo do mês no front não declara mais deficit, e o front continua exibindo o déficit do sumário
- [ ] #4 Os testes de integração da API do mês passam sem o campo
<!-- AC:END -->
