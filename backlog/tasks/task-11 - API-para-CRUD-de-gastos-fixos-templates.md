---
id: TASK-11
title: API para CRUD de gastos fixos (templates)
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - api
  - backend
dependencies:
  - TASK-1
  - TASK-3
  - TASK-6
priority: high
type: task
ordinal: 11000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Expor endpoints REST para criar, listar, atualizar e remover gastos fixos que servem de modelo para popular os meses novos (Ledger - Projeto.md, seção "Como o passado não muda"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Gastos fixos são um modelo que só gera cópias quando um mês novo é criado (geração é tratada em task dedicada).
- Mudanças em um gasto fixo não alteram lançamentos de meses já existentes.
- O gasto fixo tem: descricao, valor_padrao (NUMERIC(14,2)) e ativo (bool, soft delete).

Endpoints:
- `POST /api/v1/gastos-fixos` — cria gasto fixo.
- `GET /api/v1/gastos-fixos` — lista gastos fixos ativos do usuário.
- `PATCH /api/v1/gastos-fixos/{id}` — atualiza descricao, valor_padrao ou ativo.
- `DELETE /api/v1/gastos-fixos/{id}` — marca ativo=false (soft delete).

Fora do escopo:
- Geração de lançamentos a partir de gastos fixos na criação de um mês (task dedicada).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 `POST` cria gasto fixo com valor_padrao >= 0 e ativo=true por padrão
- [ ] #2 `POST` com valor_padrao < 0 retorna 400
- [ ] #3 `GET` lista apenas gastos fixos do usuario_id da request com ativo=true
- [ ] #4 `PATCH` em valor_padrao < 0 retorna 400
- [ ] #5 `DELETE` faz soft delete (ativo=false) e o gasto sai da listagem
- [ ] #6 Mudança em gasto fixo não altera lançamentos de meses já criados (verificável em teste integrado)
- [ ] #7 Dinheiro serializado como string no JSON de request e resposta
<!-- AC:END -->
