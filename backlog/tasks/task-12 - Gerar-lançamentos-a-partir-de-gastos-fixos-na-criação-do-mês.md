---
id: TASK-12
title: Gerar lançamentos a partir de gastos fixos na criação do mês
status: In Progress
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 03:41'
labels:
  - service
  - backend
dependencies:
  - TASK-7
  - TASK-10
  - TASK-11
priority: high
type: task
ordinal: 12000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a regra que, ao criar um mês novo, gera lançamentos de despesa (pago=false) a partir dos gastos fixos ativos do usuário (Ledger - Projeto.md, seção "Como o passado não muda"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Cada mês guarda seus próprios lançamentos concretos; os gastos fixos são um modelo que só gera cópias quando um mês novo é criado.
- Mudanças em gastos fixos não afetam lançamentos de meses já criados.
- A snapshot de parâmetros (incluindo o conjunto de gastos fixos usados como origem) é snapshotda no momento da criação do mês.
- Lançamentos gerados nascem com pago=false e descricao/valor copiados do gasto fixo; o usuário pode editá-los livremente depois.

Fora do escopo:
- Alternâncias (ficam na criação do mês, alteráveis depois em task dedicada).
- snapshot dos parâmetros em si (já tratado na task de migrations).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Criar mês sem gastos fixos não cria nenhum lançamento automático
- [x] #2 Criar mês com N gastos fixos ativos cria N lançamentos DESPESA com pago=false no mês novo
- [x] #3 Lançamento gerado tem descricao e valor iguais ao gasto fixo de origem
- [x] #4 Editar um gasto fixo depois da criação do mês não altera os lançamentos já gerados (teste integrado)
- [x] #5 Adicionar um gasto fixo depois da criação do mês não insere lançamentos no mês já criado (teste integrado)
- [x] #6 Marcar um gasto fixo como ativo=false antes da criação do mês impede que ele gere lançamento
- [x] #7 A geração é idempotente: chamar a rotina duas vezes para o mesmo mês não duplica lançamentos
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Domínio: método GastoFixo.paraLancamento() -> Lancamento.Despesa(descricao, valorPadrao, pago=false). 2. MesService: injetar GastoFixoRepository; criar passa a rejeitar mês com referência já existente do usuário (nova MesJaExisteException -> 409 via ApiExceptionHandler); após salvar o mês, chama gerarLancamentosDeGastosFixos(mesId, usuarioId): se o mês já tem lançamentos, não faz nada (idempotência); senão gera 1 lançamento por gasto fixo ativo via LancamentoRepository.criar. 3. Testes: unitário GastoFixoTest (cópia fiel, pago=false); integração MesCriacaoGastosFixosApiTest cobrindo AC #1-#7 + teste de 409 para mês duplicado; 4. Sem migration. Verificação: ./gradlew :server:test

5. Revisão do usuário: remover comentários triviais (GastoFixo.paraLancamento, MesJaExisteException); indicador de origem no lançamento (coluna lancamentos.de_gasto_fixo via migration V8, campo deGastoFixo em Lancamento.Despesa e LancamentoResponse.Despesa); guarda de idempotência passa a usar existeDeGastoFixo (mês só não regenera se já tem lançamentos de gasto fixo, permitindo gerar mesmo com lançamentos manuais).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implementação concluída: GastoFixo.paraLancamento(), geração em MesService.gerarLancamentosDeGastosFixos (guarda de idempotência), rejeição de mês duplicado com MesJaExisteException (409). Testes: GastoFixoTest (unitário) e MesCriacaoGastosFixosApiTest (8 integração, AC #1-#7 + 409). ./gradlew :server:test -> 122 testes, 0 falhas. Aguardando revisão do usuário antes do commit.

Ajustes da revisão aplicados: comentários triviais removidos; indicador de origem deGastoFixo no lançamento (migration V8, coluna lancamentos.de_gasto_fixo; campo em Lancamento.Despesa e LancamentoResponse.Despesa); guarda de idempotência agora usa existeDeGastoFixo, permitindo gerar mesmo com lançamentos manuais no mês. Novo teste de mês com lançamento manual + flag na API. ./gradlew :server:test -> 123 testes, 0 falhas. Aguardando nova revisão antes do commit.

2ª rodada de revisão: renomeado indicador de_gasto_fixo -> origem_gasto_fixo (coluna, campo origemGastoFixo em domínio/DTO/API, método existeOrigemGastoFixo, arquivo da migration V8 renomeado). ./gradlew :server:test -> 123 testes, 0 falhas. Atenção: se o banco local (docker) já aplicou a V8 antiga, rodar docker compose down -v antes de subir.
<!-- SECTION:NOTES:END -->
