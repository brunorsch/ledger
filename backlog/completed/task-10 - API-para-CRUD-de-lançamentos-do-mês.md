---
id: TASK-10
title: API para CRUD de lançamentos do mês
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 02:28'
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
- [x] #1 `POST` cria lançamento DESPESA com pago=false por padrão e retorna 201
- [x] #2 `POST` cria lançamento RECEITA com pago ignorado e retorna 201
- [x] #3 `POST` com valor <= 0 retorna 400
- [x] #4 `POST` em mês de outro usuário retorna 404
- [x] #5 `GET` lista apenas lançamentos do mês e do usuario_id da request
- [x] #6 `PATCH` em pago=true preenche pago_em automaticamente quando ausente
- [x] #7 `PATCH` em pago=false limpa pago_em
- [x] #8 `DELETE` remove o lançamento e retorna 204
- [x] #9 Mudanças refletem no sumário (verificável em teste integrado contra `GET /api/v1/meses/{id}/sumario`)
- [x] #10 Dinheiro serializado como string no JSON de request e resposta
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Domínio: estender Lancamento com identidade persistida (id, idPublico, pagoEm na Despesa), criar enum TipoLancamento e regras puras de validação (valor > 0) e normalização de pago/pagoEm (pago=true sem pagoEm preenche agora; pago=false limpa pagoEm).
2. Repositório: estender LancamentoRepository (JdbcTemplate) com criar, buscarPorIdPublicoEUsuario, atualizar e remover; listarPorMes passa a mapear id, idPublico e pagoEm.
3. API: LancamentoService + LancamentoController (POST/GET em /api/v1/meses/{mesId}/lancamentos; PATCH/DELETE em /api/v1/lancamentos/{id}), LancamentoNaoEncontradoException (404) no handler; constante do header X-Usuario-Id movida para UsuarioAutenticadoService.
4. DTOs: CriarLancamentoRequest, AlterarLancamentoRequest e LancamentoResponse (fromDomain), dinheiro como string.
5. Testes: unitários de domínio (validação e pago/pagoEm) e LancamentoApiTest (MockMvc + Testcontainers) cobrindo os 10 critérios de aceite, incluindo reflexo no sumário.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implementado: domínio (Lancamento com identidade/pagoEm + TipoLancamento + validações), repository (criar/buscar/atualizar/remover/listar), LancamentoService, LancamentoController, DTOs (CriarLancamentoRequest, AlterarLancamentoRequest, LancamentoResponse sealed com fromDomain), LancamentoNaoEncontradoException (404). Header X-Usuario-Id movido para UsuarioAutenticadoService (MesController e 3 testes ajustados). Receita omite pago/pagoEm (decisão do usuário). Testes: LancamentoTest (domínio) e LancamentoApiTest (10 ACs). Testes rodando com ./gradlew :server:test.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
API de CRUD de lançamentos: POST/GET /api/v1/meses/{mesId}/lancamentos e PATCH/DELETE /api/v1/lancamentos/{id}, com escopo por usuario_id, regras de pago/pagoEm no domínio (pago=true preenche pagoEm quando ausente; pago=false limpa), receita sem pago/pagoEm no JSON (decisão do usuário), 404 padronizado e dinheiro como string. Header X-Usuario-Id movido para UsuarioAutenticadoService. Verificado com ./gradlew :server:test: 92 testes, 0 falhas (14 em LancamentoApiTest cobrindo os 10 critérios de aceite, incluindo reflexo no sumário, e 7 em LancamentoTest).
<!-- SECTION:FINAL_SUMMARY:END -->
