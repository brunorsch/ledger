---
id: TASK-19
title: 'Adicionar observabilidade mínima: health check e logs estruturados'
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-18 19:44'
labels:
  - infra
dependencies:
  - TASK-1
modified_files:
  - server/build.gradle.kts
  - server/src/main/resources/application.yaml
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/config/CorrelationIdFilter.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/controller/PingController.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/exception/ApiExceptionHandler.kt
  - server/src/test/kotlin/br/dev/brunorsch/ledger/server/HealthCheckApiTest.kt
  - >-
    server/src/test/kotlin/br/dev/brunorsch/ledger/server/HealthCheckDownApiTest.kt
  - server/src/test/kotlin/br/dev/brunorsch/ledger/server/PingApiTest.kt
  - server/src/test/kotlin/br/dev/brunorsch/ledger/server/ErroInternoApiTest.kt
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
- [x] #1 `/actuator/health` retorna 200 com `status: UP` quando o PostgreSQL está acessível
- [x] #2 `/actuator/health` retorna 503 quando o PostgreSQL está inacessível (verificável em teste integrado com Testcontainers derrubado)
- [x] #3 Logs estruturados em JSON incluem timestamp, level, message, traceId e usuarioId quando aplicável
- [x] #4 Erro 5xx na API gera log com stacktrace e correlationId retornado ao cliente
- [x] #5 Endpoint placeholder `/api/v1/ping` responde 200 para teste de disponibilidade externo
- [x] #6 Teste integrado valida o health check em ambos os cenários (UP e DOWN)
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Logs estruturados JSON nativos (logging.structured.format.console=json) + micrometer-tracing-bridge-brave (aprovada) para traceId no MDC.
2. CorrelationIdFilter (padrão de mercado): header X-Correlation-Id (gera se ausente), MDC, header na resposta; usuarioId do header X-Usuario-Id em MDC quando aplicável.
3. ApiExceptionHandler: handler genérico -> 500 com log.error e correlationId no corpo; NoResourceFoundException -> 404.
4. PingController: GET /api/v1/ping -> 200.
5. Testes integrados: health UP/DOWN (container Postgres próprio no teste DOWN), ping, correlationId no 5xx.
6. Rodar ./gradlew :server:test e validar ACs.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test verde (135 testes, após correção à parte dos testes de migration). Logs ECS nativos do Boot 4.1 (logging.structured.format.console=ecs, sem lib extra); traceId/spanId via spring-boot-micrometer-tracing-brave + micrometer-tracing-bridge-brave (aprovadas), sem exporter. CorrelationIdFilter (X-Correlation-Id -> MDC + resposta; usuarioId no MDC quando X-Usuario-Id presente). Handler genérico 500 loga stacktrace e devolve correlationId; NoResourceFoundException -> 404 e 4xx do framework preservados (ErrorResponse 4xx re-lançado + handlers para HttpMessageNotReadable/TypeMismatch). HealthCheckDownApiTest usa container Postgres próprio (não derruba o compartilhado) com timeouts Hikari reduzidos a 1s para o cenário DOWN responder rápido. Ping: GET /api/v1/ping -> 200.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
TASK-19: observabilidade mínima. /actuator/health valida o Postgres (200 UP; 503 DOWN com banco parado, coberto por testes integrados UP/DOWN). Logs estruturados ECS com @timestamp, log.level, message e MDC (traceId, spanId, correlationId, usuarioId). CorrelationIdFilter propaga X-Correlation-Id (gera UUID se ausente) no MDC e na resposta. Erro 5xx loga stacktrace e devolve correlationId no corpo; NoResourceFoundException e demais 4xx do framework preservados. Endpoint /api/v1/ping responde 200. Verificado com suíte completa do backend (135 testes verdes).
<!-- SECTION:FINAL_SUMMARY:END -->
