---
id: TASK-19
title: 'Adicionar observabilidade mínima: health check e logs estruturados'
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - infra
dependencies:
  - TASK-1
priority: medium
type: task
ordinal: 19000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a base de observabilidade do Ledger conforme pilar do projeto (Ledger - Projeto.md, seção "Observabilidade como pilar"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- A recomendação é priorizar, na release inicial, alertas automatizados em caso de problemas, degradação e métricas de erros — mas com escopo enxuto para um app pessoal.
- O esqueleto do backend já expõe `/actuator/health` (task de setup); esta task amplia o mínimo viável.
- Sugestão de melhoria aceita: health check, logs estruturados, alerta simples de erro por e-mail ou Telegram e teste de disponibilidade externo.

Fora do escopo:
- Painéis de métricas e dashboards elaborados (ficam para depois).
- Tracing distribuído.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 `/actuator/health` retorna 200 com `status: UP` quando o PostgreSQL está acessível
- [ ] #2 `/actuator/health` retorna 503 quando o PostgreSQL está inacessível (verificável em teste integrado com Testcontainers derrubado)
- [ ] #3 Logs estruturados em JSON incluem timestamp, level, message, traceId e usuarioId quando aplicável
- [ ] #4 Erro 5xx na API gera log com stacktrace e correlationId retornado ao cliente
- [ ] #5 Endpoint placeholder `/api/v1/ping` responde 200 para teste de disponibilidade externo
- [ ] #6 Teste integrado valida o health check em ambos os cenários (UP e DOWN)
<!-- AC:END -->
