---
id: TASK-11
title: API para CRUD de gastos fixos (templates)
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 03:11'
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
- [x] #1 `POST` cria gasto fixo com valor_padrao >= 0 e ativo=true por padrão
- [x] #2 `POST` com valor_padrao < 0 retorna 400
- [x] #3 `GET` lista apenas gastos fixos do usuario_id da request com ativo=true
- [x] #4 `PATCH` em valor_padrao < 0 retorna 400
- [x] #5 `DELETE` faz soft delete (ativo=false) e o gasto sai da listagem
- [x] #6 Mudança em gasto fixo não altera lançamentos de meses já criados (verificável em teste integrado)
- [x] #7 Dinheiro serializado como string no JSON de request e resposta
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Dominio GastoFixo (imutavel): id, idPublico (UUID), descricao, valorPadrao (Dinheiro, >= 0), ativo.
2. DTOs: CriarGastoFixoRequest, AlterarGastoFixoRequest (campos opcionais), GastoFixoResponse com fromDomain no companion object.
3. GastoFixoRepository (JdbcTemplate): criar, listar ativos do usuario, buscar por idPublico+usuario, atualizar, desativar (soft delete).
4. GastoFixoService: criar (ativo=true), listar, alterar, remover; nao encontrado vira GastoFixoNaoEncontradoException (404).
5. GastoFixoController em /api/v1/gastos-fixos (POST/GET) e /api/v1/gastos-fixos/{id} (PATCH/DELETE), resolvendo usuario pelo header X-Usuario-Id.
6. ApiExceptionHandler: tratar GastoFixoNaoEncontradoException.
7. GastoFixoApiTest: integracao cobrindo os 7 criterios de aceite (valores como string, valor negativo 400, listagem filtrada, soft delete, mudanca em gasto fixo nao altera lancamentos).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implementado CRUD de gastos fixos (dominio, DTOs, repository, service, controller, exception 404). Validacao: ./gradlew :server:test — 113 testes, 0 falhas (GastoFixoApiTest: 11 testes cobrindo os 7 ACs).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
CRUD de gastos fixos em /api/v1/gastos-fixos seguindo o padrao do CRUD de lancamentos: POST cria ativo=true, GET lista ativos do usuario, PATCH altera parcialmente, DELETE faz soft delete (ativo=false). Valor negativo rejeitado na desserializacao de Dinheiro (400). Sem migracao nova (tabela V4 ja existia). Verificado com GastoFixoApiTest (11 testes de integracao, Testcontainers/PostgreSQL) e suíte completa :server:test (113 testes, 0 falhas).
<!-- SECTION:FINAL_SUMMARY:END -->
