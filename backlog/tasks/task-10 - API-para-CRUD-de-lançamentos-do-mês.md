---
id: TASK-10
title: API para CRUD de lançamentos do mês
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - api
  - backend
dependencies:
  - TASK-3
  - TASK-6
  - TASK-7
priority: high
type: task
ordinal: 10000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Expor endpoints REST para criar, listar, atualizar e remover lançamentos (despesas e receitas) de um mês (Ledger - Projeto.md, seções "A experiencia basica precisa ser mantida" e "Definições do sumário do mês"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Um lançamento tem: tipo (DESPESA|RECEITA), descricao, valor, pago (bool, apenas para DESPESA) e pago_em (timestamp opcional).
- O lançamento pertence a um mês e ao usuario_id do request.
- Dinheiro serializado como string no JSON.

Endpoints:
- `POST /api/v1/meses/{mesId}/lancamentos` — cria lançamento (despesa ou receita) no mês.
- `GET /api/v1/meses/{mesId}/lancamentos` — lista lançamentos do mês.
- `PATCH /api/v1/lancamentos/{id}` — atualiza descricao, valor, pago e pago_em.
- `DELETE /api/v1/lancamentos/{id}` — remove lançamento.

Fora do escopo:
- Edição de gastos fixos (task dedicada).
- Geração automática a partir de gastos fixos (task dedicada).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 `POST` cria lançamento DESPESA com pago=false por padrão e retorna 201
- [ ] #2 `POST` cria lançamento RECEITA com pago ignorado e retorna 201
- [ ] #3 `POST` com valor <= 0 retorna 400
- [ ] #4 `POST` em mês de outro usuário retorna 404
- [ ] #5 `GET` lista apenas lançamentos do mês e do usuario_id da request
- [ ] #6 `PATCH` em pago=true preenche pago_em automaticamente quando ausente
- [ ] #7 `PATCH` em pago=false limpa pago_em
- [ ] #8 `DELETE` remove o lançamento e retorna 204
- [ ] #9 Mudanças refletem no sumário (verificável em teste integrado contra `GET /api/v1/meses/{id}/sumario`)
- [ ] #10 Dinheiro serializado como string no JSON de request e resposta
<!-- AC:END -->
