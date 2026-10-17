---
id: TASK-9
title: API para alterar alternâncias de investir e lazer do mês
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:45'
labels:
  - api
  - backend
dependencies:
  - TASK-4
  - TASK-7
priority: high
type: task
ordinal: 9000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Expor endpoint REST para alterar as alternâncias de investir e lazer de um mês (Ledger - Projeto.md, seção "Toggles de investir e de lazer"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Alternâncias ficam gravadas no mês e afetam a distribuição das sobras.
- A alternância de gastos essenciais nunca pode ser desligada (constraint de domínio).
- A alteração das alternâncias reflete imediatamente no sumário e na distribuição (consultados em endpoint dedicado).

Endpoint:
- `PATCH /api/v1/meses/{id}/alternancias` — aceita `{investir: bool, lazer: bool}` e retorna 200 com as alternâncias atualizadas.

Fora do escopo:
- Edição de outros campos do mês (snapshot de parâmetros fica imutável para preservar o passado).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 `PATCH /api/v1/meses/{id}/alternancias` aceita `investir` e `lazer` e retorna 200
- [ ] #2 Tentativa de desligar essenciais é rejeitada com 400 (constraint de domínio)
- [ ] #3 Alterar as alternâncias reflete na distribuição obtida por `GET /api/v1/meses/{id}/sumario` (teste integrado)
- [ ] #4 Mês de outro usuário retorna 404 (autorização por usuario_id)
- [ ] #5 Mês inexistente retorna 404
<!-- AC:END -->
