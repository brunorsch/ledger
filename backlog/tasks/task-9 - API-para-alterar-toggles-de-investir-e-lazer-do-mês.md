---
id: TASK-9
title: API para alterar alternâncias de investir e lazer do mês
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 02:06'
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
- [x] #1 `PATCH /api/v1/meses/{id}/alternancias` aceita `investir` e `lazer` e retorna 200
- [x] #2 Tentativa de desligar essenciais é rejeitada com 400 (constraint de domínio)
- [x] #3 Alterar as alternâncias reflete na distribuição obtida por `GET /api/v1/meses/{id}/sumario` (teste integrado)
- [x] #4 Mês de outro usuário retorna 404 (autorização por usuario_id)
- [x] #5 Mês inexistente retorna 404
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. DTOs: AlterarAlternanciasRequest (alternarInvestir e alternarLazer obrigatórios, chaves JSON consistentes com POST /meses e decision-1) e AlternanciasResponse (alternâncias atualizadas). 2. MesService.alterarAlternancias(idPublico, usuarioId, request): busca o mês por id_publico + usuario_id (404 se ausente), aplica copy alterando apenas os dois booleanos e salva; snapshot e demais campos preservados. 3. MesController: PATCH /api/v1/meses/{id}/alternancias com header X-Usuario-Id, 200 + AlternanciasResponse. 4. Constraint de essenciais estrutural (decisão do usuário): payload sem campo de essenciais, alinhado à resolução do AC #8 da TASK-4; AC #2 coberto por teste comportamental (com investir=false e lazer=false, essenciais continua recebendo o restante inteiro). 5. MesAlternanciasApiTest (MockMvc + Testcontainers) cobrindo ACs #1-#5.

6. Revisão aprovada pelo usuário: campos do PATCH são opcionais — o que não for enviado mantém o valor atual do mês (atualização parcial; teste dedicado cobre envio de apenas um dos campos).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test -> BUILD SUCCESSFUL, 81 testes (0 falhas) em PostgreSQL real via Testcontainers; 6 novos em MesAlternanciasApiTest (PATCH com valores persistidos, atualização parcial com campo ausente, essenciais nunca desligada, reflexo no sumario, 404 para outro usuário e para mês inexistente). Implementação: AlterarAlternanciasRequest com campos nullable (atualização parcial aprovada pelo usuário), AlternanciasResponse.de, MesService.alterarAlternancias (copy + save, snapshot preservado) e PATCH /api/v1/meses/{id}/alternancias no MesController com header X-Usuario-Id.

Review aplicado: AlternanciasResponse.de renomeado para fromDomain (regra nova no AGENTS.md: mapeamentos de domínio para DTO usam fromDomain, nunca 'de'). Suíte completa revalidada: ./gradlew :server:test -> BUILD SUCCESSFUL, 81 testes (0 falhas).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
PATCH /api/v1/meses/{id}/alternancias com escopo por usuario_id (header X-Usuario-Id): aceita alternarInvestir e alternarLazer (ambos opcionais — campo não enviado mantém o valor atual) e retorna 200 com as alternâncias atualizadas (AlternanciasResponse.fromDomain). Constraint de essenciais é estrutural (payload sem campo, mesma resolução do AC #8 da TASK-4): com investir e lazer desligadas, essenciais continua recebendo o restante inteiro. MesService.alterarAlternancias aplica copy + save preservando snapshot e demais campos; 404 para mês inexistente ou de outro usuário. AGENTS.md ganhou a regra de usar fromDomain em mapeamentos de domínio para DTO (nunca 'de'). Verificado com ./gradlew :server:test (81 testes, 0 falhas, Testcontainers postgres:17): os 5 ACs cobertos em MesAlternanciasApiTest (200 persistido, atualização parcial, essenciais nunca desligada, reflexo no sumario, 404 x2).
<!-- SECTION:FINAL_SUMMARY:END -->
